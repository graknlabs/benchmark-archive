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
const vm_1 = require("../vm");
class ExecutionController {
    constructor(esClient) {
        this.create = (req, res) => __awaiter(this, void 0, void 0, function* () {
            const { commit, repoUrl } = req.body;
            const execution = {
                commit,
                repoUrl,
                id: commit + Date.now(),
                // id: 'a0d7349e809b1b56b59245d561033c68e00c6d5c1563382456916',
                executionInitialisedAt: new Date().toISOString(),
                status: 'INITIALISING',
                vmName: `benchmark-executor-${commit.trim()}`,
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
                console.log('New execution added to ES.');
                const vmController = new vm_1.VmController(execution);
                const operation = yield vmController.start();
                operation.on('complete', () => {
                    // the executor VM has been launched and is ready to be benchmarked
                    vmController.runZipkin(execution.vmName, () => __awaiter(this, void 0, void 0, function* () {
                        try {
                            yield this.updateStatus(execution, 'RUNNING');
                        }
                        catch (error) {
                            throw error.body.error;
                        }
                        vmController.runBenchmark(execution.vmName, execution.id, () => __awaiter(this, void 0, void 0, function* () {
                            try {
                                yield this.updateStatus(execution, 'COMPLETED');
                                operation.removeAllListeners();
                                res.status(200).json({ triggered: true });
                            }
                            catch (error) {
                                throw error.body.error;
                            }
                        }));
                    }));
                    operation.on('error', (error) => __awaiter(this, void 0, void 0, function* () {
                        try {
                            yield this.updateStatus(execution, 'FAILED');
                            throw error;
                        }
                        catch (error) {
                            throw error.body.error;
                        }
                    }));
                });
            }
            catch (error) {
                res.status(500).json({ error, triggered: false });
            }
        });
        this.destroy = (req, res) => __awaiter(this, void 0, void 0, function* () {
            try {
                const execution = req.body;
                yield this.client.delete(Object.assign({}, this.getDefaultEsClientPayload(), { id: execution.id }));
                const vmController = new vm_1.VmController(execution);
                yield vmController.delete();
                console.log('Execution deleted.');
                res.status(200).json({});
            }
            catch (error) {
                res.status(500).json({});
                console.error(error);
            }
        });
        this.updateStatusRouteHandler = (req, res) => __awaiter(this, void 0, void 0, function* () {
            try {
                const newStatus = req.local.newStatus;
                yield this.updateStatus(req.body.execution, newStatus);
                res.status(200).json({});
            }
            catch (error) {
                console.error(error);
                res.status(500).json({});
            }
        });
        // since updating the status of an execution needs to be done both internally (in the process of running the benchmark)
        // and externally, we need this method which is called directly for internal use, and through updateStatusRouteHandler
        // for external use
        this.updateStatus = (execution, newStatus) => __awaiter(this, void 0, void 0, function* () {
            yield this.client.update(Object.assign({}, this.getDefaultEsClientPayload(), { id: execution.id, body: {
                    doc: {
                        status: newStatus,
                        executionCompletedAt: new Date().toISOString(),
                    },
                } }));
            const deleteRequiringStatuses = ['COMPLETED', 'FAILED', 'STOPPED'];
            if (deleteRequiringStatuses.includes(newStatus)) {
                const vmController = new vm_1.VmController(execution);
                vmController.delete();
            }
            console.log(`Execution marked as ${newStatus}.`);
        });
        this.getGraphqlServer = () => {
            return graphqlHTTP({
                schema: this.getSchema(),
                context: { client: this.client },
            });
        };
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
                    executions: (object, args, context, info) => __awaiter(this, void 0, void 0, function* () {
                        const body = {};
                        if (args.status)
                            Object.assign(body, this.filterResultsByStatus(args.status));
                        if (args.orderBy)
                            Object.assign(body, this.sortResults(args.orderBy, args.order));
                        Object.assign(body, this.limitResults(args.offset, args.limit));
                        const queryObject = Object.assign({}, this.getDefaultEsClientPayload(), { body });
                        try {
                            const results = yield context.client.search(queryObject);
                            return results.body.hits.hits.map(hit => Object.assign(hit._source, { id: hit._id }));
                        }
                        catch (error) {
                            // Return empty response as this exception only means there are no Executions in ES yet.
                            if (error.body.error.type === 'index_not_found_exception')
                                return [];
                            throw error;
                        }
                    }),
                    executionById: (object, args, context) => __awaiter(this, void 0, void 0, function* () {
                        try {
                            const result = yield context.client.get(Object.assign({}, this.getDefaultEsClientPayload(), { id: args.id }));
                            return Object.assign(result.body._source, { id: result.body._id });
                        }
                        catch (error) {
                            throw error;
                        }
                    }),
                },
            };
            const schema = graphql_tools_1.makeExecutableSchema({ typeDefs, resolvers });
            return schema;
        };
        this.client = esClient;
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
