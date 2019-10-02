/* eslint-disable @typescript-eslint/camelcase */
import graphqlHTTP from 'express-graphql';
import { makeExecutableSchema, IResolvers } from 'graphql-tools';
import { Client as IEsClient, RequestParams } from '@elastic/elasticsearch';
import { getVMClient, IVMClient } from './vmClient';
import { IExecution, TStatus, TStatuses } from './types';
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

    watchPR: (req, res) => Promise<void>;
    create: (req, res) => Promise<void>;
    createInternal: (execution: IExecution) => Promise<void>;
    updateStatus: (req, res, status: TStatus) => Promise<void>;
    destroy: (req, res) => Promise<void>;
    getGraphqlServer: () => graphqlHTTP.Middleware;
}

export function getExecutionController(esClient: IEsClient): IExecutionController {
    return {
        esClient,

        watchPR,
        create,
        createInternal,
        updateStatus,
        destroy,
        getGraphqlServer
    };
}

async function watchPR(req, res) {
    const {
        body: {
            pull_request: { merge_commit_sha, merged_at, number },
            repository: { html_url}
        }
    } = req;

    const execution: IExecution = {
        id: merge_commit_sha + Date.now(),
        commit: merge_commit_sha,
        repoUrl: html_url,
        prMergedAt: merged_at,
        prUrl: html_url,
        prNumber: number,
        executionInitialisedAt: new Date().toISOString(),
        status: 'INITIALISING',
        vmName: `benchmark-executor-${merge_commit_sha.trim()}`,
    };

    try {
        await this.createInternal(execution);
        res.status(200).json({ triggered: true});
    } catch (error) {
        console.error(error);
        res.status(500).json({ triggered: false, error });
    }
}

async function create(req, res) {
    const { pull_request: { merge_commit_sha: commit }, repository: { html_url: repoUrl } } = req.body;

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
        await this.createInternal(execution);
        res.status(200).json({ triggered: true});
    } catch (error) {
        console.error(error);
        res.status(500).json({ triggered: false, error });
    }
}

async function createInternal(execution) {
    const { id, ...body } = execution;
    const payload: RequestParams.Create<Omit<IExecution, 'id'>> = { ...ES_PAYLOAD_COMMON, id, body };
    await this.esClient.create(payload);

    console.log(`New execution ${execution.id} added to ES.`);

    const vm: IVMClient = getVMClient(execution);
    vm.start().then(() => { vm.execute(); });
}

async function updateStatus(req, res, status: TStatus) {
    const execution = req.body.execution;

    try {
        const payload: RequestParams.Update<{ doc: Partial<IExecution> }> = {
            ...ES_PAYLOAD_COMMON, id: execution.id, body: { doc: { status } },
        };

        if (status === statuses.CANCELLED) payload.body.doc.executionCompletedAt = new Date().toISOString();

        await this.esClient.update(payload).catch((error) => { console.log(error); });

        console.log(`Execution ${execution.id} marked as ${status}.`);
        res.status(200).json({});

        const vmDeletionStatuses: TStatuses = ['COMPLETED', 'FAILED', 'CANCELLED'];
        if (vmDeletionStatuses.includes(status)) {
            const vm: IVMClient = getVMClient(execution);
            vm.downloadLogs()
                .then(() => { vm.terminate().catch((error) => { console.log(error); }); })
                .catch((error) => { throw error; });
        }
    } catch (error) {
        console.error(error);
        res.status(500).json({ error });
    }
}

async function destroy(req, res) {
    try {
        const execution: IExecution = req.body;
        const id = execution.id;
        const payload: RequestParams.Delete = { ...ES_PAYLOAD_COMMON, id };
        await this.esClient.delete(payload);

        console.log(`Execution ${execution.id} deleted.`);

        const vm: IVMClient = getVMClient(execution);
        await vm.terminate();

        res.status(200).json({});
    } catch (error) {
        console.error(error);
        res.status(500).json({});
    }
}

function getGraphqlServer(): graphqlHTTP.Middleware {
    const options: graphqlHTTP.OptionsData = {
        schema,
        context: { client: this.esClient }
    };
    return graphqlHTTP(options);
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
            const filterResults = (args) => {
                let should = [];

                const { status } = args;
                if (status) should = status.map(status => ({ match: { status } }));

                return { query: { bool: { should } } };
            };

            const body = {};

            const { offset, limit } = args;
            Object.assign(body, limitResults(offset, limit));

            const { orderBy, order } = args;
            if (orderBy) Object.assign(body, sortResults(orderBy, order));

            Object.assign(body, filterResults(args));

            const payload: RequestParams.Search = { ...ES_PAYLOAD_COMMON, body };

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