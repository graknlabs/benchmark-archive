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
import grakn.benchmark.generator.util.IgniteManager;
import grakn.benchmark.generator.util.SchemaManager;
import grakn.core.client.GraknClient;
import grakn.core.concept.type.AttributeType;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
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
        Ignite ignite = IgniteManager.initIgnite();
        GraknClient client = new GraknClient(config.graknUri());
        String keyspace = config.getKeyspace();

        // error if keyspace not empty
        verifyKeyspaceIsEmpty(client, keyspace);

        // load schema into keyspace
        loadSchema(client, keyspace, config.getGraqlSchema());

        // create the data generator
        DataGenerator dataGenerator = initDataGenerator(client, keyspace);

        // alternate between generating data and profiling a queries
        List<GraqlQuery> queries = toGraqlQueries(config.getQueries());
        ReportData reportData = new ReportData();
        try {
            for (int graphScale : config.scalesToProfile()) {
                LOG.info("Generating graph to scale... " + graphScale);
                // NOTE number of concepts actually generated may be just around the desired quantity
                dataGenerator.generate(graphScale);

                // collect and aggregate results
                Map<GraqlQuery, QueryExecutionResults> result = executeQueries(client, queries);
                recordDataForScale(reportData, graphScale, result);
            }
        } finally {
            client.close();
            ignite.close();
        }

        // serialize data to JSON
        System.out.println(reportData.asJson());
    }

    private void recordDataForScale(ReportData reportData, int scale, Map<GraqlQuery, QueryExecutionResults> singleScaleResults) {
        for (GraqlQuery query : singleScaleResults.keySet()) {
            QueryExecutionResults queryExecutionResults = singleScaleResults.get(query);
            // inject the scale into the container
            queryExecutionResults.setScale(scale);
            reportData.recordQueryTimes(queryExecutionResults.queryType(), query, queryExecutionResults);
        }
    }


    private Map<GraqlQuery, QueryExecutionResults> executeQueries(GraknClient client, List<GraqlQuery> queries) {

        ExecutorService executorService = Executors.newFixedThreadPool(config.concurrentClients());
        List<Future<Map<GraqlQuery, QueryExecutionResults>>> runningQueries = new LinkedList<>();
        List<GraknClient.Session> openSessions = new LinkedList<>();

        // run N concurrent sessions querying Grakn
        for (int i = 0; i < config.concurrentClients(); i++) {
            GraknClient.Session session = client.session(config.getKeyspace());
            openSessions.add(session);
            runningQueries.add(executorService.submit(new QueriesExecutor(session, queries, config.numQueryRepetitions())));
        }

        // Collect N concurrent executors' data
        List<Map<GraqlQuery, QueryExecutionResults>> concurrentResults = new LinkedList<>();
        try {
            for (Future<Map<GraqlQuery, QueryExecutionResults>> futureResult : runningQueries) {
                concurrentResults.add(futureResult.get());
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException("Error in execution of queries", e);
        } finally {
            openSessions.forEach(GraknClient.Session::close);
        }

        // aggregate concurrent results, starting with first result
        Map<GraqlQuery, QueryExecutionResults> combinedResults = concurrentResults.get(0);

        for (Map<GraqlQuery, QueryExecutionResults> result : concurrentResults.subList(1, concurrentResults.size())) {
            for (GraqlQuery query : result.keySet()) {
                // do an in-place modification to aggregate all the times into one list
                List<Long> times = combinedResults.get(query).times();
                List<Long> otherTimes = result.get(query).times();
                times.addAll(otherTimes);
            }
        }

        return combinedResults;
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

    private List<GraqlQuery> toGraqlQueries(List<String> queries) {
        return queries.stream().map(q -> (GraqlQuery) Graql.parse(q)).collect(Collectors.toList());
    }

    private void verifyKeyspaceIsEmpty(GraknClient client, String keyspace) {
        // verify keyspace is empty
        SchemaManager schemaManager = new SchemaManager(client.session(keyspace));
        if (!schemaManager.verifyEmptyKeyspace()) {
            throw new BootupException("Keyspace " + keyspace + " is not empty.");
        }
    }

    private void loadSchema(GraknClient client, String keyspace, List<String> schemaQueries) {
        // load schema
        LOG.info("Initialising keyspace `" + keyspace + "`...");
        try (GraknClient.Transaction tx = client.session(keyspace).transaction().write()) {
            Stream<GraqlQuery> query = parseList(schemaQueries.stream().collect(Collectors.joining("\n")));
            query.forEach(q -> tx.execute(q));
            tx.commit();
        }
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
