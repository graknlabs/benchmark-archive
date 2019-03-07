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

package grakn.benchmark.profiler;

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import grakn.benchmark.profiler.generator.DataGenerator;
import grakn.benchmark.profiler.generator.DataGeneratorException;
import grakn.benchmark.profiler.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.profiler.generator.definition.DefinitionFactory;
import grakn.benchmark.profiler.generator.query.QueryProvider;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.storage.IgniteConceptStorage;
import grakn.benchmark.profiler.generator.util.IgniteManager;
import grakn.benchmark.profiler.util.BenchmarkArguments;
import grakn.benchmark.profiler.util.BenchmarkConfiguration;
import grakn.benchmark.profiler.util.ElasticSearchManager;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.benchmark.profiler.util.TracingGraknClient;
import grakn.core.client.GraknClient;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.EntityType;
import grakn.core.concept.type.RelationType;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

/**
 * Class in charge of
 * - initialising Benchmark dependencies and BenchmarkConfiguration
 * - run data generation (populate empty keyspace) (DataGenerator)
 * - run benchmark on queries (QueryProfiler)
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
            LOG.error("Unable to start GraknClient Benchmark:", e);
        } finally {
            System.out.println("Exiting benchmark with exit code " + exitCode);
            System.exit(exitCode);
        }
    }

    public GraknBenchmark(CommandLine arguments) {
        this.config = new BenchmarkConfiguration(arguments);
    }


    /**
     * Start the GraknClient Benchmark, which, based on arguments provided via console, will run one of the following use cases:
     * - generate synthetic data while profiling the graph at different sizes
     * - don't generate new data and only profile an existing keyspace
     */
    public void start() {

        if (config.generateData() && config.concurrentClients() > 1 && config.uniqueConcurrentKeyspaces()) {
            throw new BootupException("Cannot currently perform data generation into more than 1 keyspace");
        }

        List<GraknClient.Session> concurrentSessions = initKeyspaces();
        QueryProfiler queryProfiler = new QueryProfiler(concurrentSessions, config.executionName(), config.graphName(), config.getQueries(), config.commitQueries());

        if (config.generateData()) {
            Ignite ignite = IgniteManager.initIgnite();
            try {
                // data generator has its own non-benchmarking client to the keyspace
                DataGenerator dataGenerator = initDataGenerator(TracingGraknClient.get(config.graknUri()), config.getKeyspace());
                List<Integer> numConceptsInRun = config.scalesToProfile();
                for (int numConcepts : numConceptsInRun) {
                    LOG.info("Generating graph to scale... " + numConcepts);
                    dataGenerator.generate(numConcepts);
                    queryProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                ignite.close();
            }

        } else {
//            int numConcepts = queryProfiler.aggregateCount();
            int numConcepts = 0; // TODO re-add this properly for concurrent clients
            queryProfiler.processStaticQueries(config.numQueryRepetitions(), numConcepts);
        }

        for (GraknClient.Session session : concurrentSessions) {
            session.close();
        }
        queryProfiler.cleanup();
    }

    /**
     * Create and trace creation of keyspaces (via client.session()), schema insertions
     * If profiling a pre-populated keyspace, just instantiate the required concurrent sessions
     * @return
     */
    private List<GraknClient.Session> initKeyspaces() {

        String keyspace = config.getKeyspace();
        List<GraknClient.Session> sessions = new LinkedList<>();

        for (int i = 0; i < config.concurrentClients(); i++) {
            GraknClient client = TracingGraknClient.get(config.graknUri());

            if (config.generateData() || config.loadSchema()) {
                // if we generate data or load a schema at all
                // we want to trace the creation of the keyspace (via client.session())
                // and the insertion of the schema

                if (config.uniqueConcurrentKeyspaces()) {
                    String keyspaceName = keyspace + "_" + i;
                    GraknClient.Session session = traceInitKeyspace(client, keyspaceName);
                    sessions.add(session);
                } else {
                    GraknClient.Session session;
                    if (i == 0) {
                        session = traceInitKeyspace(client, keyspace);
                    } else {
                        session = client.session(keyspace);
                    }
                    sessions.add(session);
                }
            } else {
                // if we are only profiling an existing keyspace
                // we only open sessions that connect to that keyspace (can't concurrently benchmark multiple pre-populated keyspaces)
                GraknClient.Session session = client.session(keyspace);
                sessions.add(session);
            }
        }
        return sessions;
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
            SchemaManager manager = new SchemaManager(session, config.getGraqlSchema());
            span.annotate("Verifying keyspace is empty");
            manager.verifyEmptyKeyspace();
            span.annotate("Loading qraql schema");
            manager.loadSchema();
        }

        span.finish();
        return session;
    }


    /**
     * Connect a data generator to pre-prepared keyspace
     */
    private DataGenerator initDataGenerator(GraknClient client, String keyspace) {
        int randomSeed = 0;
        String graphName = config.graphName();

        SchemaManager schemaManager = new SchemaManager(client.session(keyspace), config.getGraqlSchema());
        HashSet<EntityType> entityTypes = schemaManager.getEntityTypes();
        HashSet<RelationType> relationshipTypes = schemaManager.getRelationshipTypes();
        HashSet<AttributeType> attributeTypes = schemaManager.getAttributeTypes();

        ConceptStorage storage = new IgniteConceptStorage(entityTypes, relationshipTypes, attributeTypes);

        DataGeneratorDefinition dataGeneratorDefinition = DefinitionFactory.getDefinition(graphName, new Random(randomSeed), storage);

        QueryProvider queryProvider = new QueryProvider(dataGeneratorDefinition);

        return new DataGenerator(client, keyspace, storage, graphName, queryProvider);
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
