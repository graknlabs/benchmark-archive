import * as graphqlHTTP from 'express-graphql';
import {
  makeExecutableSchema, IResolvers,
} from 'graphql-tools';
import { Client as EsClient, RequestParams } from '@elastic/elasticsearch';
import { InstanceController } from './instance';
import { IExecution, TStatus, TStatuses } from '../types';
import { GraphQLSchema } from 'graphql/type';

export class ExecutionController {
  client: EsClient;

  constructor(esClient: EsClient) {
    this.client = esClient;
  }

  async create(req, res): Promise<void> {
    const { commit, repoUrl } = req.body;

    const execution: IExecution = {
      commit,
      repoUrl,
      id: commit + Date.now(),
      executionInitialisedAt: new Date().toISOString(),
      status: 'INITIALISING',
      vmName: `'benchmark-executor-${commit.trim()}`,
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

      const instanceController = new InstanceController(execution);
      instanceController.start();

      console.log('New execution added to ES.');
      res.status(200).json({ triggered: true });
    } catch (error) {
      console.error(error);
      res.status(500).json({ triggered: false, error: true });
    }
  }

  async destroy(req, res): Promise<void> {
    try {
      const execution: IExecution = req.body;

      await this.client.delete({
        ... this.getDefaultEsClientPayload,
        id: execution.id,
      } as RequestParams.Delete);

      const instanceController = new InstanceController(execution);
      instanceController.delete();

      console.log('Execution deleted.');
      res.status(200).json({});
    } catch (error) {
      res.status(500).json({});
      console.error(error);
    }
  }

  async updateStatus(req, res): Promise<void> {
    const { execution } = req.body;
    const { newStatus }: { newStatus: TStatus } = req.locals;

    try {
      await this.client.update({
        ... this.getDefaultEsClientPayload(),
        id: execution.id,
        body: {
          status: newStatus,
          executionCompletedAt: new Date().toISOString(),
        },
      });

      const deleteRequiringStatuses: TStatuses = ['COMPLETED', 'FAILED', 'STOPPED'];
      if (deleteRequiringStatuses.includes(newStatus)) {
        const instanceController = new InstanceController(execution);
        instanceController.delete();
      }

      console.log(`Execution marked as ${newStatus}.`);
      res.status(200).json({});
    } catch (error) {
      console.error(error);
      res.status(500).json({});
    }
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
        // eslint-disable-next-line no-unused-vars
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
        executionById: (object, args, context) => {
          return context.client.get(Object.assign({}, this.getDefaultEsClientPayload(), { id: args.id }))
            .then(res => Object.assign(res._source, { id: res._id }));
        },
      },
    };

    const schema: GraphQLSchema = makeExecutableSchema({ typeDefs, resolvers });
    return schema;
    // console.log(schema);
  }

  getGraphqlServer = () => {
    console.log(this.getSchema());

    return graphqlHTTP({
      schema: this.getSchema(),
      context: { client: this.client },
    });
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
