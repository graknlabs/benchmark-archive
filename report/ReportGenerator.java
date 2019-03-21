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
import grakn.benchmark.generator.DataGeneratorException;
import grakn.core.client.GraknClient;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReportGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(ReportGenerator.class);

    private final BenchmarkConfiguration config;

    private final String[] configFiles = {
            "/common/conf/complex/conf_read.yml",
            "/common/conf/complex/conf_read.yml",
            "/common/conf/road_network_read.yml",
            "/common/conf/road_network_write.yml"
    };

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


    /**
     * Start the Grakn Benchmark, which, based on arguments provided via console, will run one of the following use cases:
     * - generate synthetic data while profiling the graph at different sizes
     * - don't generate new data and only profile an existing keyspace
     */
    public void start() {







        GraknClient client = new GraknClient(config.graknUri());
//        ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, config.getKeyspace(), config);
//
//        DataGenerator dataGenerator = initDataGenerator(client, config.getKeyspace()); // use a non tracing client as we don't trace data generation yet
//        List<Integer> numConceptsInRun = config.scalesToProfile();
//
        try {
//            for (int numConcepts : numConceptsInRun) {
//                LOG.info("Generating graph to scale... " + numConcepts);
//                dataGenerator.generate(numConcepts);
//                threadedProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
//            }
//        } catch (Exception e) {
//            throw e;
        } finally {
//            threadedProfiler.cleanup();
//            tracingClient.close();
            client.close();
        }


    }

//    private void traceKeyspaceCreation(GraknClient client) {
//        String keyspace = config.getKeyspace();
//        GraknClient.Session session = traceInitKeyspace(client, keyspace);
//        session.close();
//    }
//
//    /**
//     * Create and trace creation of keyspaces (via client.session()), schema insertions
//     * If profiling a pre-populated keyspace, just instantiate the required concurrent sessions
//     *
//     * @return
//     */
//    private List<String> traceCreationOfMultipleKeyspaces(GraknClient client) {
//
//        String keyspace = config.getKeyspace();
//        List<String> keyspaces = new LinkedList<>();
//
//        for (int i = 0; i < config.concurrentClients(); i++) {
//            String keyspaceName = keyspace + "_" + i;
//            GraknClient.Session session = traceInitKeyspace(client, keyspaceName);
//            session.close();
//            keyspaces.add(keyspaceName);
//
//        }
//        return keyspaces;
//    }
//
//    private GraknClient.Session traceInitKeyspace(GraknClient client, String keyspace) {
//        // time creation of keyspace and insertion of schema
//        LOG.info("Adding schema to keyspace: " + keyspace);
//        Span span = Tracing.currentTracer().newTrace().name("New Keyspace + schema: " + keyspace);
//        span.start();
//
//        GraknClient.Session session;
//        try (Tracer.SpanInScope ws = Tracing.currentTracer().withSpanInScope(span)) {
//            span.annotate("Opening new session");
//            session = client.session(keyspace);
//            SchemaManager manager = new SchemaManager(session, config.getGraqlSchema());
//            span.annotate("Verifying keyspace is empty");
//            manager.verifyEmptyKeyspace();
//            span.annotate("Loading qraql schema");
//            manager.loadSchema();
//        }
//
//        span.finish();
//        return session;
//    }
//
//
//    /**
//     * Connect a data generator to pre-prepared keyspace
//     */
//    private DataGenerator initDataGenerator(GraknClient client, String keyspace) {
//        int randomSeed = 0;
//        String dataGenerator= config.dataGenerator();
//        GraknClient.Session session = client.session(keyspace);
//        SchemaManager schemaManager = new SchemaManager(session, config.getGraqlSchema());
//        HashSet<String> entityTypeLabels = schemaManager.getEntityTypes();
//        HashSet<String> relationshipTypeLabels = schemaManager.getRelationTypes();
//        Map<String, AttributeType.DataType<?>> attributeTypeLabels = schemaManager.getAttributeTypes();
//
//        ConceptStorage storage = new IgniteConceptStorage(entityTypeLabels, relationshipTypeLabels, attributeTypeLabels);
//
//        DataGeneratorDefinition dataGeneratorDefinition = DefinitionFactory.getDefinition(dataGenerator, new Random(randomSeed), storage);
//
//        QueryProvider queryProvider = new QueryProvider(dataGeneratorDefinition);
//
//        return new DataGenerator(client, keyspace, storage, dataGenerator, queryProvider);
//    }

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
