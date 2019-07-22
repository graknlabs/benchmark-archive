import * as graphqlHTTP from 'express-graphql';
import { makeExecutableSchema, IResolvers } from 'graphql-tools';
import { Client as IEsClient, RequestParams } from '@elastic/elasticsearch';
import { VmController } from '../vm';
import { IExecution, TStatus, TStatuses } from '../../types';
import { GraphQLSchema } from 'graphql/type';

export class ExecutionController {
  private client: IEsClient;

  constructor(esClient: IEsClient) {
    this.client = esClient;
  }

  create = async (req, res): Promise<void> => {
    const { commit, repoUrl } = req.body;

    const execution: IExecution = {
      commit,
      repoUrl,
      id: commit + Date.now(),
      // id: 'a0d7349e809b1b56b59245d561033c68e00c6d5c1563382456916',
      executionInitialisedAt: new Date().toISOString(),
      status: 'INITIALISING',
      vmName: `benchmark-executor-${commit.trim()}`,
    };

    try {
      await this.client.create({
        ... this.getDefaultEsClientPayload(),
        id: execution.id,
        body: {
          commit: execution.commit,
          prMergedAt: execution.prMergedAt,
          prUrl: execution.prUrl,
          prNumber: execution.prNumber,
          repoUrl: execution.repoUrl,
          executionInitialisedAt: execution.executionInitialisedAt,
          executionStartedAt: execution.executionStartedAt,
          executionCompletedAt: execution.executionCompletedAt,
          status: execution.status,
          vmName: execution.vmName,
        },
      });

      console.log('New execution added to ES.');

      const vmController = new VmController(execution);

      const operation = await vmController.start();

      operation.on('complete', () => {
        // the executor VM has been launched and is ready to be benchmarked

        vmController.runZipkin(execution.vmName, async () => {
          try {
            await this.updateStatus(execution, 'RUNNING');
          } catch (error) {
            throw error.body.error;
          }

          vmController.runBenchmark(execution.vmName, execution.id, async () => {
            try {
              await this.updateStatus(execution, 'COMPLETED');
              operation.removeAllListeners();
              res.status(200).json({ triggered: true });
            } catch (error) {
              throw error.body.error;
            }
          });
        });

        operation.on('error', async (error) => {
          try {
            await this.updateStatus(execution, 'FAILED');
            throw error;
          } catch (error) {
            throw error.body.error;
          }
        });
      });
    } catch (error) {
      res.status(500).json({ error, triggered: false });
    }
  }

  destroy = async (req, res): Promise<void> => {
    try {
      const execution: IExecution = req.body;

      await this.client.delete({
        ... this.getDefaultEsClientPayload(),
        id: execution.id,
      } as RequestParams.Delete);

      const vmController = new VmController(execution);
      await vmController.delete();

      console.log('Execution deleted.');
      res.status(200).json({});
    } catch (error) {
      res.status(500).json({});
      console.error(error);
    }
  }

  updateStatusRouteHandler = async (req, res) => {
    try {
      const newStatus: TStatus = req.local.newStatus;
      await this.updateStatus(req.body.execution, newStatus);
      res.status(200).json({});
    } catch (error) {
      console.error(error);
      res.status(500).json({});
    }
  }

  // since updating the status of an execution needs to be done both internally (in the process of running the benchmark)
  // and externally, we need this method which is called directly for internal use, and through updateStatusRouteHandler
  // for external use
  updateStatus = async (execution: IExecution, newStatus: TStatus): Promise<void> => {
    await this.client.update({
      ... this.getDefaultEsClientPayload(),
      id: execution.id,
      body: {
        doc: {
          status: newStatus,
          executionCompletedAt: new Date().toISOString(),
        },
      },
    });

    const deleteRequiringStatuses: TStatuses = ['COMPLETED', 'FAILED', 'STOPPED'];
    if (deleteRequiringStatuses.includes(newStatus)) {
      const vmController = new VmController(execution);
      vmController.delete();
    }

    console.log(`Execution marked as ${newStatus}.`);
  }

  getGraphqlServer = () => {
    return graphqlHTTP({
      schema: this.getSchema(),
      context: { client: this.client },
    });
  }

  private getSchema = (): GraphQLSchema => {
    const typeDefs: string = `
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
          if (args.status) Object.assign(body, this.filterResultsByStatus(args.status));
          if (args.orderBy) Object.assign(body, this.sortResults(args.orderBy, args.order));
          Object.assign(body, this.limitResults(args.offset, args.limit));
          const queryObject = Object.assign({}, this.getDefaultEsClientPayload(), { body });

          try {
            const results = await context.client.search(queryObject);
            return results.body.hits.hits.map(hit => Object.assign(hit._source, { id: hit._id }));
          } catch (error) {
            // Return empty response as this exception only means there are no Executions in ES yet.
            if (error.body.error.type === 'index_not_found_exception') return [];
            throw error;
          }
        },
        executionById: async (object, args, context) => {
          try {
            const result = await context.client.get(Object.assign({}, this.getDefaultEsClientPayload(), { id: args.id }));
            return Object.assign(result.body._source, { id: result.body._id });
          } catch (error) {
            throw error;
          }
        },
      },
    };

    const schema: GraphQLSchema = makeExecutableSchema({ typeDefs, resolvers });
    return schema;
  }

  private getDefaultEsClientPayload(): { index: string, type: string } {
    return { index: 'grakn-benchmark', type: 'execution' };
  }

  private filterResultsByStatus(statuses): { query: { bool: { should: { match: { status: TStatus }[] } } } } {
    const should = statuses.map(status => ({ match: { status } }));
    return { query: { bool: { should } } };
  }

  private sortResults(orderBy, orderMethod): { sort: [{ [key: string]: 'desc' | 'asc' }] } {
    return { sort: [{ [orderBy]: orderMethod || 'desc' }] };
  }

  private limitResults(offset, limit): { from: number, size: number } {
    return { from: offset || 0, size: limit || 50 };
  }
}
