/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
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

package grakn.benchmark.profiler;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import grakn.benchmark.common.configuration.parse.BenchmarkArguments;
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
import grakn.benchmark.profiler.util.ElasticSearchManager;
import grakn.benchmark.profiler.util.TracingGraknClient;
import grakn.core.client.GraknClient;
import grakn.core.concept.type.AttributeType;
import graql.lang.query.GraqlQuery;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static graql.lang.Graql.parseList;

/**
 * Class in charge of
 * - initialising Benchmark dependencies and BenchmarkConfiguration
 * - run data generation (populate empty keyspace) (DataGenerator)
 * - run benchmark on queries (ThreadedProfiler + QueryProfiler)
 */
public class GraknBenchmark {
    private static final Logger LOG = LoggerFactory.getLogger(GraknBenchmark.class);

    private final BenchmarkConfiguration config;

    /**
     * Entry point invoked by benchmark script
     */
    public static void main(String[] args) {
        printAscii();
        int exitCode = 0;
        try {
            // Parse the configuration for the benchmark
            CommandLine arguments = BenchmarkArguments.parse(args);

            ElasticSearchManager.putIndexTemplate(arguments);
            GraknBenchmark benchmark = new GraknBenchmark(arguments);
            benchmark.start();
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

    public GraknBenchmark(CommandLine arguments) {
        config = new BenchmarkConfiguration(arguments);
    }


    /**
     * Start the Grakn Benchmark, which, based on arguments provided via console, will run one of the following use cases:
     * - generate synthetic data while profiling the graph at different sizes
     * - don't generate new data and only profile an existing keyspace
     */
    public void start() {


        if (config.generateData()) {  // USECASE: Load Schema + Generate Data + Profile at different scales running queries from config file

            // Multiple concurrent clients are only allowed to query the same keyspace.
            if (config.concurrentClients() > 1 && config.uniqueConcurrentKeyspaces()) {
                throw new BootupException("Cannot currently perform data generation into more than 1 keyspace");
            }

            GraknClient tracingClient = TracingGraknClient.get(config.graknUri());
            traceKeyspaceCreation(tracingClient);
            ThreadedProfiler threadedProfiler = new ThreadedProfiler(tracingClient, Collections.singletonList(config.getKeyspace()), config);

            Ignite ignite = IgniteManager.initIgnite();
            GraknClient client = new GraknClient(config.graknUri());
            DataGenerator dataGenerator = initDataGenerator(client, config.getKeyspace()); // use a non tracing client as we don't trace data generation yet
            List<Integer> numConceptsInRun = config.scalesToProfile();

            try {
                for (int numConcepts : numConceptsInRun) {
                    LOG.info("Generating graph to scale... " + numConcepts);
                    dataGenerator.generate(numConcepts);
                    threadedProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                threadedProfiler.cleanup();
                tracingClient.close();
                client.close();
                ignite.close();
            }


        } else if (config.loadSchema()) {  // USECASE:  Load Schema + Profile running queries from config file

            GraknClient tracingClient = TracingGraknClient.get(config.graknUri());
            List<String> keyspaces;

            if (config.uniqueConcurrentKeyspaces()) {
                keyspaces = traceCreationOfMultipleKeyspaces(tracingClient);
            } else {
                traceKeyspaceCreation(tracingClient);
                keyspaces = Collections.singletonList(config.getKeyspace());
            }

            ThreadedProfiler threadedProfiler = new ThreadedProfiler(tracingClient, keyspaces, config);
            int numConcepts = 0;
            threadedProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
            threadedProfiler.cleanup();
            tracingClient.close();

        } else {  // USECASE:  Profile an existing keyspace using queries from config file.

            // Multiple concurrent clients are only allowed to query the same keyspace.
            if (config.concurrentClients() > 1 && config.uniqueConcurrentKeyspaces()) {
                throw new BootupException("Cannot currently perform profiling into more than 1 keyspace");
            }

            GraknClient client = new GraknClient(config.graknUri());
            ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, Collections.singletonList(config.getKeyspace()), config);

//            int numConcepts = threadedProfiler.aggregateCount();
            int numConcepts = 0; // TODO re-add this properly for concurrent clients
            threadedProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
            threadedProfiler.cleanup();
            client.close();
        }
    }

    private void traceKeyspaceCreation(GraknClient client) {
        String keyspace = config.getKeyspace();
        GraknClient.Session session = traceInitKeyspace(client, keyspace);
        session.close();
    }

    /**
     * Create and trace creation of keyspaces (via client.session()), schema insertions
     * If profiling a pre-populated keyspace, just instantiate the required concurrent sessions
     *
     * @return
     */
    private List<String> traceCreationOfMultipleKeyspaces(GraknClient client) {

        String keyspace = config.getKeyspace();
        List<String> keyspaces = new LinkedList<>();

        for (int i = 0; i < config.concurrentClients(); i++) {
            String keyspaceName = keyspace + "_" + i;
            GraknClient.Session session = traceInitKeyspace(client, keyspaceName);
            session.close();
            keyspaces.add(keyspaceName);

        }
        return keyspaces;
    }

    private GraknClient.Session traceInitKeyspace(GraknClient client, String keyspace) {
        // time creation of keyspace and insertion of schema
        LOG.info("Adding schema to keyspace: " + keyspace);
        Span span = Tracing.currentTracer().newTrace().name("New Keyspace + schema: " + keyspace);
        span.start();

        GraknClient.Session session;
        try (Tracer.SpanInScope ws = Tracing.currentTracer().withSpanInScope(span)) {
            span.annotate("Opening new session");
            session = client.session(keyspace);
            SchemaManager manager = new SchemaManager(session);
            span.annotate("Verifying keyspace is empty");
            if (!manager.verifyEmptyKeyspace()) {
                throw new BootupException("Keyspace " + keyspace + " is not empty");
            }
            span.annotate("Loading qraql schema");
            loadSchema(session, config.getGraqlSchema());
        }

        span.finish();
        return session;
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
        String dataGenerator= config.dataGenerator();
        GraknClient.Session session = client.session(keyspace);
        SchemaManager schemaManager = new SchemaManager(session);
        HashSet<String> entityTypeLabels = schemaManager.getEntityTypes();
        HashSet<String> relationshipTypeLabels = schemaManager.getRelationTypes();
        Map<String, AttributeType.DataType<?>> attributeTypeLabels = schemaManager.getAttributeTypes();

        ConceptStorage storage = new IgniteConceptStorage(entityTypeLabels, relationshipTypeLabels, attributeTypeLabels);

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
