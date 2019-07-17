"use strict";
var __awaiter = (this && this.__awaiter) || function (thisArg, _arguments, P, generator) {
    return new (P || (P = Promise))(function (resolve, reject) {
        function fulfilled(value) { try { step(generator.next(value)); } catch (e) { reject(e); } }
        function rejected(value) { try { step(generator["throw"](value)); } catch (e) { reject(e); } }
        function step(result) { result.done ? resolve(result.value) : new P(function (resolve) { resolve(result.value); }).then(fulfilled, rejected); }
        step((generator = generator.apply(thisArg, _arguments || [])).next());
    });
};
Object.defineProperty(exports, "__esModule", { value: true });
const graphqlHTTP = require("express-graphql");
const graphql_tools_1 = require("graphql-tools");
const instance_1 = require("./instance");
class ExecutionController {
    constructor(esClient) {
        this.getSchema = () => {
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
            const resolvers = {
                Query: {
                    // eslint-disable-next-line no-unused-vars
                    executions: (object, args, context, info) => __awaiter(this, void 0, void 0, function* () {
                        console.log('executions ..........');
                        const body = {};
                        if (args.status)
                            Object.assign(body, this.filterResultsByStatus(args.status));
                        if (args.orderBy)
                            Object.assign(body, this.sortResults(args.orderBy, args.order));
                        Object.assign(body, this.limitResults(args.offset, args.limit));
                        const queryObject = Object.assign({}, this.getDefaultEsClientPayload(), { body });
                        console.log(queryObject);
                        // return context.client.search(queryObject).then(result => result.hits.hits.map(res => Object.assign(res._source, { id: res._id })))
                        try {
                            const results = yield context.client.search(queryObject);
                            console.log('success');
                            return results.body.hits.hits.map(res => Object.assign(res._source, { id: res._id }));
                        }
                        catch (error) {
                            // Return empty response as this exception only means there are no Executions in ES yet.
                            if (error.body.error.type === 'index_not_found_exception')
                                return [];
                            throw error;
                        }
                        return false;
                        // return context.client.search(queryObject)
                        //   .then(result => result.hits.hits.map(res => Object.assign(res._source, { id: res._id })))
                        //   .catch((error) => {
                        //     // Return empty response as this exception only means there are no Executions in ES yet.
                        //     // if (error.body.error.type === 'index_not_found_exception') return [];
                        //     throw error;
                        //   });
                    }),
                    executionById: (object, args, context) => {
                        return context.client.get(Object.assign({}, this.getDefaultEsClientPayload(), { id: args.id }))
                            .then(res => Object.assign(res._source, { id: res._id }));
                    },
                },
            };
            const schema = graphql_tools_1.makeExecutableSchema({ typeDefs, resolvers });
            return schema;
            // console.log(schema);
        };
        this.getGraphqlServer = () => {
            console.log(this.getSchema());
            return graphqlHTTP({
                schema: this.getSchema(),
                context: { client: this.client },
            });
        };
        this.client = esClient;
    }
    create(req, res) {
        return __awaiter(this, void 0, void 0, function* () {
            const { commit, repoUrl } = req.body;
            const execution = {
                commit,
                repoUrl,
                id: commit + Date.now(),
                executionInitialisedAt: new Date().toISOString(),
                status: 'INITIALISING',
                vmName: `'benchmark-executor-${commit.trim()}`,
            };
            try {
                yield this.client.create(Object.assign({}, this.getDefaultEsClientPayload(), { id: execution.id, body: {
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
                    } }));
                const instanceController = new instance_1.InstanceController(execution);
                instanceController.start();
                console.log('New execution added to ES.');
                res.status(200).json({ triggered: true });
            }
            catch (error) {
                console.error(error);
                res.status(500).json({ triggered: false, error: true });
            }
        });
    }
    destroy(req, res) {
        return __awaiter(this, void 0, void 0, function* () {
            try {
                const execution = req.body;
                yield this.client.delete(Object.assign({}, this.getDefaultEsClientPayload, { id: execution.id }));
                const instanceController = new instance_1.InstanceController(execution);
                instanceController.delete();
                console.log('Execution deleted.');
                res.status(200).json({});
            }
            catch (error) {
                res.status(500).json({});
                console.error(error);
            }
        });
    }
    updateStatus(req, res) {
        return __awaiter(this, void 0, void 0, function* () {
            const { execution } = req.body;
            const { newStatus } = req.locals;
            try {
                yield this.client.update(Object.assign({}, this.getDefaultEsClientPayload(), { id: execution.id, body: {
                        status: newStatus,
                        executionCompletedAt: new Date().toISOString(),
                    } }));
                const deleteRequiringStatuses = ['COMPLETED', 'FAILED', 'STOPPED'];
                if (deleteRequiringStatuses.includes(newStatus)) {
                    const instanceController = new instance_1.InstanceController(execution);
                    instanceController.delete();
                }
                console.log(`Execution marked as ${newStatus}.`);
                res.status(200).json({});
            }
            catch (error) {
                console.error(error);
                res.status(500).json({});
            }
        });
    }
    getDefaultEsClientPayload() {
        return { index: 'grakn-benchmark', type: 'execution' };
    }
    filterResultsByStatus(statuses) {
        const should = statuses.map(status => ({ match: { status } }));
        return { query: { bool: { should } } };
    }
    sortResults(orderBy, orderMethod) {
        return { sort: [{ [orderBy]: orderMethod || 'desc' }] };
    }
    limitResults(offset, limit) {
        return { from: offset || 0, size: limit || 50 };
    }
}
exports.ExecutionController = ExecutionController;
