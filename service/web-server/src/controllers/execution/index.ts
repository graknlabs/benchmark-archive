import * as graphqlHTTP from 'express-graphql';
import { makeExecutableSchema, IResolvers } from 'graphql-tools';
import { Client as IEsClient, RequestParams } from '@elastic/elasticsearch';
import { VmController } from '../vm';
import { IExecution, TStatus, TStatuses } from '../../types';
import { GraphQLSchema } from 'graphql/type';
import { getEsClient } from '../../utils';

const esClient = getEsClient();

const ES_PAYLOAD_COMMON = { index: 'grakn-benchmark', type: 'execution' };

const statuses: { [key: string]: TStatus; } = {
  INITIALISING: 'INITIALISING',
  CANCELED: 'CANCELED',
  COMPLETED: 'COMPLETED',
  FAILED: 'FAILED',
  RUNNING: 'RUNNING',
};

const create = async (req, res) => {
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
    await esClient.create(payload);

    console.log('New execution added to ES.');

    const vm = new VmController(execution);
    const operation = await vm.start();

    operation.on('complete', () => {
      // the executor VM has been launched and is ready to be benchmarked

      // the need for setTimeout is due to a bug that has been issued here:
      // https://github.com/googleapis/nodejs-compute/issues/336
      setTimeout(() => {
        vm.setUp(execution, async () => {
          vm.runZipkin(execution, async () => {
            try {
              await updateStatusInternal(execution, statuses.RUNNING);
            } catch (error) {
              throw error.body.error;
            }

            vm.runBenchmark(execution, async () => {
              try {
                // await updateStatusInternal(execution, statuses.COMPLETED);
                operation.removeAllListeners();
                res.status(200).json({ triggered: true });
              } catch (error) {
                throw error.body.error;
              }
            });
          });
        });
      },         2000);
    });

    operation.on('error', async (error) => {
      try {
        await updateStatusInternal(execution, statuses.FAILED);
        throw error;
      } catch (error) {
        throw error.body.error;
      }
    });
  } catch (error) {
    try {
      await updateStatusInternal(execution, statuses.FAILED);
      throw error;
    } catch (error) {
      throw error.body.error;
    }
  }
};

const updateStatus = async (req, res, status: TStatus) => {
  try {
    await updateStatusInternal(req.body.execution, status);
    res.status(200).json({});
  } catch (error) {
    console.error(error);
    res.status(500).json({});
  }
};

// since updating the status of an execution needs to be done both internally (in the process of running the benchmark)
// and externally (via the dashboard), we need this method which is called directly for internal use, and through
// its wrapper for external use
const updateStatusInternal = async (execution: IExecution, status: TStatus): Promise<void> => {
  const payload: RequestParams.Update<{ doc: Partial<IExecution>; }> = {
    ...ES_PAYLOAD_COMMON, id: execution.id, body: { doc: { status } },
  };

  if (status === 'CANCELED' as TStatus) payload.body.doc.executionCompletedAt = new Date().toISOString();

  await esClient.update(payload);

  console.log(`Execution marked as ${status}.`);

  const vmDeletionStatuses: TStatuses = ['COMPLETED', 'FAILED', 'CANCELED'];
  if (vmDeletionStatuses.includes(status)) {
    const vm = new VmController(execution);
    vm.delete();
  }
};

const destroy = async (req, res) => {
  try {
    const execution: IExecution = req.body;
    const id = execution.id;
    const payload: RequestParams.Delete = { ...ES_PAYLOAD_COMMON, id };
    await esClient.delete(payload);
    console.log('Execution deleted.');

    const vm = new VmController(execution);
    await vm.delete();
    console.log('VM instance deleted.');

    res.status(200).json({});
  } catch (error) {
    res.status(500).json({});
    console.error(error);
  }
};

const getGraphqlServer = () => {
  return graphqlHTTP({
    schema,
    context: { client: esClient },
  });
};

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
    executions: async (object, args, context, info) => {
      const body = {};

      const { offset, limit } = args;
      Object.assign(body, limitResults(offset, limit));

      const { status, orderBy, order } = args;
      if (status) Object.assign(body, filterResultsByStatus(status));
      if (orderBy) Object.assign(body, sortResults(orderBy, order));

      const payload: RequestParams.Search<any> = { ...ES_PAYLOAD_COMMON, body };
      const esClient: IEsClient = context.client;

      try {
        const results = await esClient.search(payload);

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
        const result = await esClient.get(payload);
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

const sortResults = (orderBy: keyof IExecution, orderMethod: 'desc' | 'asc') => {
  return { sort: [{ [orderBy]: orderMethod || 'desc' }] };
};

const limitResults = (offset: number, limit: number) => {
  return { from: offset || 0, size: limit || 50 };
};

export const ExecutionController = {
  create,
  updateStatus,
  destroy,
  getGraphqlServer,
};
