import graphqlHTTP from 'express-graphql';
import { Client as IEsClient, RequestParams } from '@elastic/elasticsearch';
import { limitResults } from '../utils';
import { GraphQLSchema } from 'graphql';
import { makeExecutableSchema } from 'graphql-tools';
import { GraphQLLong } from '../graphqlTypes';

const ES_PAYLOAD_COMMON = { index: 'benchmark*', type: 'span' };

export interface ISpanController {
    esClient: IEsClient;
    getGraphqlServer: () => graphqlHTTP.Middleware;
}

export function getSpanController(esClient: IEsClient): ISpanController {
    return {
        esClient,
        getGraphqlServer
    };
}

function getGraphqlServer(): graphqlHTTP.Middleware {
    const options: graphqlHTTP.OptionsData = {
        schema,
        context: { client: this.esClient }
    };
    return graphqlHTTP(options);
}

const typeDefs = `
  scalar Long

  type Query {
    querySpans(
        parentId: String,
        offset: Int,
        limit: Int): [QuerySpan]

    executionSpans(
        executionName: String,
        graphType: String,
        offset: Int,
        limit: Int
    ): [ExecutionSpan]

    childrenSpans(
        parentId: [String],
        offset: Int,
        limit: Int
    ): [ChildSpan]
  }

  type ChildSpan {
    id: String!
    duration: Long!
    name: String!
    timestamp: String
    tags: ChildSpanTag
    parentId: String
  }

  type ChildSpanTag {
      childNumber: Int
  }

  type ExecutionSpan {
    id: String!
    duration: Long!
    name: String!
    tags: ExecutionSpanTag
  }

  type ExecutionSpanTag {
    configurationName: String,
    description: String,
    executionName: String,
    concurrentClient: Int,
    graphType: String,
    queryRepetitions: Int,
    graphScale: Int
  }

  type QuerySpan {
    id: String!
    parentId: String
    duration: Long!
    name: String!
    tags: QuerySpanTag
  }

  type QuerySpanTag {
    type: String,
    query: String,
    repetition: Int,
    repetitions: Int
  }
`;

const resolvers = {
    Long: GraphQLLong,
    Query: {
        querySpans: async (object, args, context, ) => {
            const filterResults = (args) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const must: any = [{ term: { name: "query" } }];

                const { parentId } = args;
                if (parentId) must.push({ match: { parentId } });

                return { query: { bool: { must } } };
            };

            let body = {};

            const { offset, limit } = args;
            Object.assign(body, limitResults(offset, limit));

            Object.assign(body, filterResults(args));

            const payload: RequestParams.Search = { ...ES_PAYLOAD_COMMON, body };
            const results = await context.client.search(payload).catch((error) => { throw error });

            const spans = results.body.hits.hits.map((hit) => {
                const span = hit._source;
                return span;
            });

            return spans;
        },

        executionSpans: async (object, args, context) => {
            const filterResults = (args) => {
                // eslint-disable-next-line @typescript-eslint/no-explicit-any
                const must: any[] = [{ term: { name: "concurrent-execution" } }];

                const { graphType } = args;
                if (graphType) must.push({ match: { "tags.graphType": graphType } });

                const { executionName } = args;
                if (executionName) must.push({ match: { "tags.executionName": executionName } });

                return { query: { bool: { must } } };
            }

            let body = {};

            const { offset, limit } = args;
            Object.assign(body, limitResults(offset, limit));

            Object.assign(body, filterResults(args));

            const payload: RequestParams.Search = { ...ES_PAYLOAD_COMMON, body };
            const results = await context.client.search(payload).catch((error) => { throw error });

            const spans = results.body.hits.hits.map((hit) => {
                const span = hit._source;
                return span;
            });

            return spans;
        },

        childrenSpans: async (object, args, context) => {
            const filterResults = (args) => {
                let should = [];

                const { parentId } = args;
                if (parentId) should = parentId.map((parentId) => ({ match: { parentId }}));

                return { query: { bool: { should } } };
            }

            let body = {};

            const { offset, limit } = args;
            Object.assign(body, limitResults(offset, limit));

            Object.assign(body, filterResults(args));

            const payload: RequestParams.Search = { ...ES_PAYLOAD_COMMON, body };
            const results = await context.client.search(payload).catch((error) => { throw error });

            const spans = results.body.hits.hits.map((hit) => {
                const span = hit._source;
                return span;
            });

            return spans;
        }
    }
}

const schema: GraphQLSchema = makeExecutableSchema({ typeDefs, resolvers });
