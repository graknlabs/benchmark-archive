import * as graphqlHTTP from 'express-graphql';
import { makeExecutableSchema, IResolvers } from 'graphql-tools';
import { Client as IEsClient, RequestParams } from '@elastic/elasticsearch';
import { VMController, IVMController } from '../vm';
import { IExecution, TStatus, TStatuses } from '../../types';
import { GraphQLSchema } from 'graphql/type';
import { limitResults, sortResults } from '../utils';

const ES_PAYLOAD_COMMON = { index: 'grakn-benchmark', type: 'execution' };

const statuses: { [key: string]: TStatus } = {
    INITIALISING: 'INITIALISING',
    CANCELLED: 'CANCELLED',
    COMPLETED: 'COMPLETED',
    FAILED: 'FAILED',
    RUNNING: 'RUNNING',
};

export interface IExecutionController {
    esClient: IEsClient;
    create: (req, res) => {};
    updateStatus: (req, res, status) => {};
    destroy: (req, res) => {};
    getGraphqlServer: () => {};
    updateStatusInternal: (execution: IExecution, status: TStatus) => Promise<void>;
}

// tslint:disable-next-line: function-name
export function ExecutionController(this: IExecutionController, client: IEsClient) {
    this.esClient = client;

    this.create = create.bind(this);
    this.updateStatus = updateStatus.bind(this);
    this.destroy = destroy.bind(this);
    this.getGraphqlServer = getGraphqlServer.bind(this);
    this.updateStatusInternal = updateStatusInternal.bind(this);
}

async function create(this: IExecutionController, req, res) {
    const { commit, repoUrl } = req.body;
    const execution: IExecution = {
        commit,
        repoUrl,
        id: commit + Date.now(),
        executionInitialisedAt: new Date().toISOString(),
        status: statuses.INITIALISING as TStatus,
        vmName: `benchmark-executor-${commit.trim()}`,
        prMergedAt: undefined,
        prUrl: undefined,
        prNumber: undefined,
        executionStartedAt: undefined,
        executionCompletedAt: undefined,
    };

    try {
        const { id, ...body } = execution;
        const payload: RequestParams.Create<Omit<IExecution, 'id'>> = { ...ES_PAYLOAD_COMMON, id, body };
        await this.esClient.create(payload);

        res.status(200).json({ triggered: true });

        console.log(`New execution ${execution.id} added to ES.`);

        const vm: IVMController = new VMController(execution);
        await vm.start();
        await vm.execute();

    } catch (error) {
        console.log(error);
        await this.updateStatusInternal(execution, statuses.FAILED).catch((error) => { console.log(error); });
    }
}

async function updateStatus(this: IExecutionController, req, res, status: TStatus) {
    const execution = req.body;

    try {
        await this.updateStatusInternal(execution, status);
        res.status(200).json({});
    } catch (error) {
        console.error(error);
        res.status(500).json({ error });
    }
}

// since updating the status of an execution needs to be done both internally (in the process of running the benchmark)
// and externally (via the dashboard), we need this method which is called directly for internal use, and through
// its wrapper for external use
async function updateStatusInternal(this: IExecutionController, execution: IExecution, status: TStatus): Promise<void> {
    const payload: RequestParams.Update<{ doc: Partial<IExecution> }> = {
        ...ES_PAYLOAD_COMMON, id: execution.id, body: { doc: { status } },
    };

    if (status === statuses.CANCELLED) payload.body.doc.executionCompletedAt = new Date().toISOString();

    await this.esClient.update(payload).catch((error) => { console.log(error); });

    console.log(`Execution ${execution.id} marked as ${status}.`);

    const vmDeletionStatuses: TStatuses = ['COMPLETED', 'FAILED', 'CANCELLED'];

    if (vmDeletionStatuses.includes(status)) {
        const vm: IVMController = new VMController(execution);
        await vm.downloadLogs().catch((error) => { throw error; });
        vm.terminate().catch((error) => { console.log(error); });
    }
}

async function destroy(this: IExecutionController, req, res) {
    try {
        const execution: IExecution = req.body;
        const id = execution.id;
        const payload: RequestParams.Delete = { ...ES_PAYLOAD_COMMON, id };
        await this.esClient.delete(payload);

        console.log(`Execution ${execution.id} deleted.`);

        const vm: IVMController = new VMController(execution);
        await vm.terminate();

        res.status(200).json({});
    } catch (error) {
        res.status(500).json({});
        console.error(error);
    }
}

function getGraphqlServer(this: IExecutionController) {
    return graphqlHTTP({
        schema,
        context: { client: this.esClient },
    });
}

const typeDefs = `
  type Query {
      executions (
        status: [String],
        orderBy: String,
        order: String,
        offset: Int,
        limit: Int
      ): [Execution]

      executionById(id: String!): Execution
  }

  type Execution {
      id: String!,
      commit: String!
      repoUrl: String!
      prMergedAt: String,
      prUrl: String,
      prNumber: String,
      executionInitialisedAt: String
      executionStartedAt: String
      executionCompletedAt: String
      status: String!
      vmName: String
  }`;

const resolvers: IResolvers = {
    Query: {
        executions: async (object, args, context) => {
            const body = {};

            const { offset, limit } = args;
            Object.assign(body, limitResults(offset, limit));

            const { status, orderBy, order } = args;
            if (status) Object.assign(body, filterResultsByStatus(status));
            if (orderBy) Object.assign(body, sortResults(orderBy, order));

            // eslint-disable-next-line @typescript-eslint/no-explicit-any
            const payload: RequestParams.Search<any> = { ...ES_PAYLOAD_COMMON, body };

            try {
                const results = await context.client.search(payload);

                const executions = results.body.hits.hits.map((hit) => {
                    const execution = hit._source;
                    return { ...execution, id: hit._id };
                });

                return executions;
            } catch (error) {
                // Return empty response as this exception only means there are no Executions in ES yet.
                if (error.body.error.type === 'index_not_found_exception') return [];
                throw error;
            }
        },

        executionById: async (object, args, context) => {
            try {
                const payload: RequestParams.Get = { ...ES_PAYLOAD_COMMON, id: args.id };
                const result = await context.client.get(payload);
                const execution = result.body._source;
                return { ...execution, id: result.body._id };
            } catch (error) {
                throw error;
            }
        },
    },
};

const schema: GraphQLSchema = makeExecutableSchema({ typeDefs, resolvers });

const filterResultsByStatus = (statuses: TStatuses) => {
    const should = statuses.map(status => ({ match: { status } }));
    return { query: { bool: { should } } };
};