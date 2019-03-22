/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 GraknClient Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.report;


import grakn.benchmark.common.configuration.BenchmarkArguments;
import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.common.exception.BootupException;
import grakn.benchmark.generator.DataGenerator;
import grakn.benchmark.generator.DataGeneratorException;
import grakn.benchmark.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.generator.definition.DefinitionFactory;
import grakn.benchmark.generator.query.QueryProvider;
import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.benchmark.generator.storage.IgniteConceptStorage;
import grakn.benchmark.generator.util.SchemaManager;
import grakn.benchmark.profiler.util.ProfilerException;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.Answer;
import grakn.core.concept.type.AttributeType;
import graql.lang.Graql;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graql.lang.Graql.parseList;

public class ReportGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private final BenchmarkConfiguration config;

    public static void main(String[] args) {
        printAscii();
        int exitCode = 0;
        try {
            // Parse the configuration for the benchmark
            CommandLine arguments = BenchmarkArguments.parse(args);

            ReportGenerator reportGenerator = new ReportGenerator(arguments);
            reportGenerator.start();
        } catch (DataGeneratorException e) {
            exitCode = 1;
            LOG.error("Error in data generator: ", e);
        } catch (Exception e) {
            exitCode = 1;
            LOG.error("Exception while running Grakn Benchmark:", e);
        } finally {
            System.out.println("Exiting benchmark with exit code " + exitCode);
            System.exit(exitCode);
        }
    }

    public ReportGenerator(CommandLine arguments) {
        config = new BenchmarkConfiguration(arguments);
    }


    public void start() {
        GraknClient client = new GraknClient(config.graknUri());
        String keyspace = config.getKeyspace();

        // verify keyspace is empty
        SchemaManager schemaManager = new SchemaManager(client.session(keyspace));
        if (!schemaManager.verifyEmptyKeyspace()) {
            throw new BootupException("Keyspace " + keyspace + " is not empty.");
        }

        // load schema into keyspace
        loadSchema(client.session(keyspace), config.getGraqlSchema());

        // create the data generator
        DataGenerator dataGenerator = initDataGenerator(client, keyspace);

        // alternate between generating data and profiling a queries
        List<GraqlQuery> queries = config.getQueries().stream().map(q -> (GraqlQuery) Graql.parse(q)).collect(Collectors.toList());
        try {
            for (int numConcepts : config.scalesToProfile()) {
                LOG.info("Generating graph to scale... " + numConcepts);
                dataGenerator.generate(numConcepts);
                executeQueries(client, queries);
                Map<GraqlQuery, QueryExecutionResults> result = executeQueries(client, queries);
            }
        } finally {
            client.close();
        }
    }


    private Map<GraqlQuery, QueryExecutionResults> executeQueries(GraknClient client, List<GraqlQuery> queries) {

        ExecutorService executorService = Executors.newFixedThreadPool(config.concurrentClients());
        List<Future<Map<GraqlQuery, QueryExecutionResults>>> runningQueries = new LinkedList<>();
        List<GraknClient.Session> openSessions = new LinkedList<>();
        for (int i = 0; i < config.concurrentClients(); i++) {
            GraknClient.Session session = client.session(config.getKeyspace());
            openSessions.add(session);
            executorService.submit(new QueryExecutor(session, queries, config.numQueryRepetitions()));
        }

        List<Map<GraqlQuery, QueryExecutionResults>> results = new LinkedList<>();
        try {
            for (Future<Map<GraqlQuery, QueryExecutionResults>> futureResult : runningQueries) {
                results.add(futureResult.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error in execution of queries", e);
        } finally {
            openSessions.forEach(GraknClient.Session::close);
        }

        // aggregate concurrent results
        Map<GraqlQuery, QueryExecutionResults> combinedResults = new HashMap<>();

        for (Map<GraqlQuery, QueryExecutionResults> result : results) {
            for (GraqlQuery query : result.keySet()) {
                combinedResults.putIfAbsent(query, new QueryExecutionResults());
                // merge results for each query into the combined map
                combinedResults.put(query, combinedResults.get(query).merge(result.get(query)));
            }
        }

        return combinedResults;
    }

    class QueryExecutor implements Callable<Map<GraqlQuery, QueryExecutionResults>> {

        private final List<GraqlQuery> queries;
        private int repetitions;
        private final GraknClient.Session session;

        public QueryExecutor(GraknClient.Session session, List<GraqlQuery> queries, int repetitions) {
            this.session = session;
            this.queries = queries;
            this.repetitions = repetitions;
        }

        @Override
        public Map<GraqlQuery, QueryExecutionResults> call() {
            Map<GraqlQuery, QueryExecutionResults> result = new HashMap<>();
            for (int rep = 0; rep < repetitions; rep++) {
                for (GraqlQuery query : queries) {
                    // initialise results holder
                    result.putIfAbsent(query, new QueryExecutionResults());

                    // open a new write transaction and record execution time
                    GraknClient.Transaction tx = session.transaction().write();
                    Long startTime = System.currentTimeMillis();
                    List<? extends Answer> answer = tx.execute(query);
                    long endTime = System.currentTimeMillis();

                    // TODO record commit time?
                    tx.commit();

                    String type;
                    if (query instanceof GraqlGet) {
                        type = "get";
                    } else if (query instanceof GraqlInsert) {
                        type = "insert";
                    } else if (query instanceof GraqlDelete) {
                        type = "delete";
                    } else if (query instanceof GraqlCompute) {
                        type = "compute";
                    } else {
                        type = "UNKNOWN";
                    }

                    // TODO collect data about this answer - eg how many were inserted/deleted/matched, how many round trips
                    result.get(query).record( endTime - startTime, null, type, null);

                    // TODO delete inserted concepts
                }
            }
            return result;
        }
    }

    class MultiscaleQueryExecutorResult {

        private Map<String, Map<GraqlQuery, Map<Integer, QueryExecutionResults>>> data;

        public MultiscaleQueryExecutorResult() {
            data = new HashMap<>();
        }


    }

    class QueryExecutionResults {
        private List<Long> queryExecutionTimes = new LinkedList<>();
        private Integer conceptsInvolved = null;
        private String type = null;
        private Integer roundTrips = null;

        public QueryExecutionResults() {
        }

        public void record(Long milliseconds, Integer conceptsInvolved, String type, Integer roundTrips) {
            queryExecutionTimes.add(milliseconds);
            this.conceptsInvolved = conceptsInvolved;
            this.type = type;
            this.roundTrips = roundTrips;
        }

        public List<Long> times() {
            return queryExecutionTimes;
        }

        public Integer concepts() {
            return conceptsInvolved;
        }

        public String type() {
            return type;
        }

        public Integer roundTrips() {
            return roundTrips;
        }

        public QueryExecutionResults merge(QueryExecutionResults queryExecutionsResult) {
            QueryExecutionResults result = new QueryExecutionResults();

            if (times().size() > 0) {
                // check if the merged item has any data, otherwise will be all nulls
                for (Long t : times()) {
                    result.record(t, conceptsInvolved, type, roundTrips);
                }
            }

            if (queryExecutionsResult.times().size() > 0) {
                // check if the merged item has any data, otherwise will be all nulls
                for (Long t : queryExecutionsResult.times()) {
                    result.record(t, queryExecutionsResult.concepts(), queryExecutionsResult.type(), queryExecutionsResult.roundTrips());
                }
            }

            return result;
        }
    }

    private void loadSchema(GraknClient.Session session, List<String> schemaQueries) {
        // load schema
        LOG.info("Initialising keyspace `" + session.keyspace() + "`...");
        try (GraknClient.Transaction tx = session.transaction().write()) {
            Stream<GraqlQuery> query = parseList(schemaQueries.stream().collect(Collectors.joining("\n")));
            query.forEach(q -> tx.execute(q));
            tx.commit();
        }
    }

    /**
     * Connect a data generator to pre-prepared keyspace
     */
    private DataGenerator initDataGenerator(GraknClient client, String keyspace) {
        int randomSeed = 0;
        GraknClient.Session session = client.session(keyspace);

        SchemaManager schemaManager = new SchemaManager(session);
        HashSet<String> entityTypeLabels = schemaManager.getEntityTypes();
        HashSet<String> relationshipTypeLabels = schemaManager.getRelationTypes();
        Map<String, AttributeType.DataType<?>> attributeTypeLabels = schemaManager.getAttributeTypes();
        ConceptStorage storage = new IgniteConceptStorage(entityTypeLabels, relationshipTypeLabels, attributeTypeLabels);

        String dataGenerator = config.dataGenerator();
        DataGeneratorDefinition dataGeneratorDefinition = DefinitionFactory.getDefinition(dataGenerator, new Random(randomSeed), storage);
        QueryProvider queryProvider = new QueryProvider(dataGeneratorDefinition);
        return new DataGenerator(client, keyspace, storage, dataGenerator, queryProvider);
    }

    private static void printAscii() {
        System.out.println();
        System.out.println("========================================================================================================");
        System.out.println("   ______ ____   ___     __ __  _   __   ____   ______ _   __ ______ __  __ __  ___ ___     ____   __ __\n" +
                "  / ____// __ \\ /   |   / //_/ / | / /  / __ ) / ____// | / // ____// / / //  |/  //   |   / __ \\ / //_/\n" +
                " / / __ / /_/ // /| |  / ,<   /  |/ /  / __  |/ __/  /  |/ // /    / /_/ // /|_/ // /| |  / /_/ // ,<   \n" +
                "/ /_/ // _, _// ___ | / /| | / /|  /  / /_/ // /___ / /|  // /___ / __  // /  / // ___ | / _, _// /| |  \n" +
                "\\____//_/ |_|/_/  |_|/_/ |_|/_/ |_/  /_____//_____//_/ |_/ \\____//_/ /_//_/  /_//_/  |_|/_/ |_|/_/ |_|  \n" +
                "                                                                                                        ");
        System.out.println("========================================================================================================");
        System.out.println();
    }
}
