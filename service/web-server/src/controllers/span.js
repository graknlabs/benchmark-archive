const graphqlHTTP = require('express-graphql');
const { makeExecutableSchema } = require('graphql-tools');
const Utils = require('../Utils');

const esClient = Utils.getEsClient();

const typeDefs = `
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
    duration: Int!
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
    duration: Int!
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
    duration: Int!
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


const filterExecutionSpans = (args) => {
    const must = [{ term: { name: "concurrent-execution" } }];
    if (args.graphType) {
        must.push({ match: { "tags.graphType": args.graphType } });
    }
    if (args.executionName) {
        must.push({ match: { "tags.executionName": args.executionName } });
    }
    return { query: { bool: { must } } };
};

const limitQuery = (offset, limit) => {
    return { from: offset || 0, size: limit || 50 };
};

const filterQuerySpans = (args) => {
    const must = [{ term: { name: "query" } }];
    if (args.parentId) {
        must.push({ match: { "parentId": args.parentId } });
    }
    return { query: { bool: { must } } };
};

const filterChildrenSpans = (args) => {
    let should = [];
    if (args.parentId) {
        should = args.parentId.map(parentId => ({ match: { parentId } }));
    }
    return { query: { bool: { should } } };
};

const resolvers = {
    Query: {
        // eslint-disable-next-line no-unused-vars
        querySpans: (object, args, context, info) => {
            let body = {};
            Object.assign(body, limitQuery(args.offset, args.limit));
            Object.assign(body, filterQuerySpans(args));
            const queryObject = Object.assign({ index: "benchmark*", type: "span" }, { body });
            return context.client.search(queryObject).then(result => result.hits.hits.map(res => res._source));
        },
        // eslint-disable-next-line no-unused-vars
        executionSpans: (object, args, context, info) => {
            let body = {};
            Object.assign(body, limitQuery(args.offset, args.limit));
            Object.assign(body, filterExecutionSpans(args));
            const queryObject = Object.assign({ index: "benchmark*", type: "span" }, { body });
            return context.client.search(queryObject).then(result => result.hits.hits.map(res => res._source));
        },
        // eslint-disable-next-line no-unused-vars
        childrenSpans: (object, args, context, info) => {
            let body = {};
            Object.assign(body, limitQuery(args.offset, args.limit));
            Object.assign(body, filterChildrenSpans(args));
            const queryObject = Object.assign({ index: "benchmark*", type: "span" }, { body });
            return context.client.search(queryObject).then(result => result.hits.hits.map(res => res._source));
        }
    }
}

const schema = makeExecutableSchema({ typeDefs, resolvers })

const query = () => {
    return graphqlHTTP({
        schema,
        context: { client: esClient },
    });
}

module.exports = query;