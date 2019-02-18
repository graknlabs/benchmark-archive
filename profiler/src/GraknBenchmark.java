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

import brave.ScopedSpan;
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
import grakn.core.client.GraknClient;
import grakn.core.graql.concept.AttributeType;
import grakn.core.graql.concept.EntityType;
import grakn.core.graql.concept.RelationType;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

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

        List<GraknClient> clients = new LinkedList<>();
        List<GraknClient.Session> sessions = new LinkedList<>();
        Set<String> keyspaces = new HashSet<>();

        String keyspaceBase = config.getKeyspace();
        // create concurrent clients
        for (int i = 0; i < config.concurrentClients(); i++) {
            GraknClient client = new GraknClient(config.graknUri(), true);
            clients.add(client);
            String keyspaceName;
            if (config.uniqueConcurrentKeyspaces()) {
                // make some unique keyspaces (hopefully empty, TODO this check if not exists further down)
                keyspaceName = keyspaceBase + "_c" + Integer.toString(i);
            } else {
                keyspaceName = keyspaceBase;
            }
            keyspaces.add(keyspaceName);
            GraknClient.Session session = client.session(keyspaceName);
            sessions.add(session);
        }


        QueryProfiler queryProfiler = new QueryProfiler(sessions, config.executionName(), config.graphName(), config.getQueries(), config.commitQueries());
        int repetitionsPerQuery = config.numQueryRepetitions();

        if (config.generateData()) {

            if (keyspaces.size() > 1) {
                throw new BootupException("Cannot currently perform data generatation into more than 1 keyspace");
            }

            Ignite ignite = IgniteManager.initIgnite();
            try {
                DataGenerator dataGenerator = initDataGenerator(sessions.get(0));
                List<Integer> numConceptsInRun = config.scalesToProfile();
                for (int numConcepts : numConceptsInRun) {
                    LOG.info("Generating graph to scale... " + numConcepts);
                    dataGenerator.generate(numConcepts);
                    queryProfiler.processStaticQueries(repetitionsPerQuery, numConcepts);
                }
            } catch (Exception e) {
                throw e;
            } finally {
                ignite.close();
            }

        } else {
//            int numConcepts = queryProfiler.aggregateCount();

            // TODO remove this
            // temporarily allow loadng a schema here
            for (String keyspace : keyspaces) {
                LOG.info("Adding schema to keyspace: " + keyspace);
                // insert schema into each keyspace
                Span span = Tracing.currentTracer().newTrace().name("Inserting schema into new Keyspace " + keyspace);
                span.start();
                try (Tracer.SpanInScope ws = Tracing.currentTracer().withSpanInScope(span)) {
                    GraknClient.Session session = clients.get(0).session(keyspace);
                    SchemaManager manager = new SchemaManager(session, config.getGraqlSchema());
                }
                span.finish();
            }

            int numConcepts = 0; // TODO re-add this properly for concurrent clients
            queryProfiler.processStaticQueries(repetitionsPerQuery, numConcepts);
        }

        for (GraknClient.Session session: sessions) {
            session.close();
        }
        queryProfiler.cleanup();
    }

    private DataGenerator initDataGenerator(GraknClient.Session session) {
        int randomSeed = 0;
        String graphName = config.graphName();

        SchemaManager schemaManager = new SchemaManager(session, config.getGraqlSchema());
        HashSet<EntityType> entityTypes = schemaManager.getEntityTypes();
        HashSet<RelationType> relationshipTypes = schemaManager.getRelationshipTypes();
        HashSet<AttributeType> attributeTypes = schemaManager.getAttributeTypes();

        ConceptStorage storage = new IgniteConceptStorage(entityTypes, relationshipTypes, attributeTypes);

        DataGeneratorDefinition dataGeneratorDefinition = DefinitionFactory.getDefinition(graphName, new Random(randomSeed), storage);

        QueryProvider queryProvider = new QueryProvider(dataGeneratorDefinition);

        return new DataGenerator(session, storage, graphName, queryProvider);
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
