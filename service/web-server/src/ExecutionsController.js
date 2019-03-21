const graphqlHTTP = require('express-graphql');
const { makeExecutableSchema } = require('graphql-tools');

const GRAKN_BENCHMARK_INDEX = 'grakn-benchmark';
const BENCHMARK_EXECUTION_TYPE = 'execution';
const SEARCH_EXECUTIONS_OBJECT = { index: GRAKN_BENCHMARK_INDEX, type: BENCHMARK_EXECUTION_TYPE};

function ExecutionsController(client){
    this.client = client
    this.addExecution = addExecution;
    this.deleteExecution = deleteExecution;
    this.updateExecutionStatus = updateExecutionStatus;
    this.queryExecutions = queryExecutions;
}

module.exports = ExecutionsController;


function addExecution(execution) {
    return this.client.create({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
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
        }
      });
}

function deleteExecution(execution) {
    return this.client.delete({
        index: GRAKN_BENCHMARK_INDEX,
        type: BENCHMARK_EXECUTION_TYPE,
        id: execution.id
      });
}

function updateExecutionStatus(execution, doc) {
  return this.client.update({
      index: GRAKN_BENCHMARK_INDEX,
      type: BENCHMARK_EXECUTION_TYPE,
      id: execution.executionId || execution.id,
      body: { doc }
    });
}

// ----------------- GraphQL Schema, Methods and Middleware ---------------- //

// Construct a schema, using GraphQL schema language
const typeDefs = `
  type Query {
    executions(
      status: [String], 
      orderBy: String, 
      order: String,
      offset: Int, 
      limit: Int): [Execution]

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
  }
`;


function queryFilteredByStatus(statusArray){
  const should = statusArray.map(status => ({ match: { status}}));
  return { query: { bool: { should } } } ;
}

function sortResults(orderBy, order){
  const sortingOrder = (order) ? order : "desc";
  return { sort: [ {[orderBy]: sortingOrder} ]};
}

function limitQuery(offset, limit){
  return {from : offset || 0 , size : limit || 50};
}

// The root provides a resolver function for each API endpoint
const root = {
  executions: (object, args, context, info) => {
    let body = {};
    // If a status is provided, only retrieve executions with given status/es
    if(args.status) {
      Object.assign(body, queryFilteredByStatus(args.status));
    } 
    // If ordeBy is provided, order results by field
    if(args.orderBy){
      Object.assign(body, sortResults(args.orderBy, args.order));
    }
    Object.assign(body, limitQuery(args.offset, args.limit));
    const queryObject = Object.assign({}, SEARCH_EXECUTIONS_OBJECT, { body });
    return context.client.search(queryObject).then(result => result.hits.hits.map(res => Object.assign(res._source, {id: res._id})))
    .catch(e => {
      if(e.body.error.type === "index_not_found_exception"){
        return []; // Return empty response as this exception only means there are no Executions in ES yet.
      }
      else {
        throw e;
      }
    });
  },
  executionById: (object, args, context) => {
    return context.client.get(Object.assign({}, SEARCH_EXECUTIONS_OBJECT, { id: args.id})).then(res => Object.assign(res._source, {id: res._id}));
  }
};


function queryExecutions() {
  return graphqlHTTP({
    schema: makeExecutableSchema({ typeDefs , resolvers:{ Query: root}}),
    context: { client: this.client},
  });
}