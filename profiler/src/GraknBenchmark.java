/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

import grakn.benchmark.profiler.generator.DataGeneratorException;
import grakn.benchmark.profiler.generator.DataGenerator;
import grakn.benchmark.profiler.generator.query.QueryProvider;
import grakn.benchmark.profiler.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.profiler.generator.definition.DefinitionFactory;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.storage.IgniteConceptStorage;
import grakn.benchmark.profiler.generator.util.IgniteManager;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.benchmark.profiler.util.BenchmarkArguments;
import grakn.benchmark.profiler.util.BenchmarkConfiguration;
import grakn.benchmark.profiler.util.ElasticSearchManager;
import grakn.core.client.Grakn;
import grakn.core.concept.AttributeType;
import grakn.core.concept.EntityType;
import grakn.core.concept.RelationshipType;
import grakn.core.util.SimpleURI;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
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
            LOG.error("Unable to start Grakn Benchmark:", e);
        } finally {
            System.exit(exitCode);
        }
    }

    public GraknBenchmark(CommandLine arguments) {
        this.config = new BenchmarkConfiguration(arguments);
    }


    /**
     * Start the Grakn Benchmark, which, based on arguments provided via console, will run one of the following use cases:
     * - generate synthetic data while profiling the graph at different sizes
     * - don't generate new data and only profile an existing keyspace
     */
    public void start() {
        Grakn client = new Grakn(new SimpleURI(config.graknUri()), true);
        Grakn.Session session = client.session(config.getKeyspace());
        QueryProfiler queryProfiler = new QueryProfiler(session, config.executionName(), config.graphName(), config.getQueries());
        int repetitionsPerQuery = config.numQueryRepetitions();

        if (config.generateData()) {

            Ignite ignite = IgniteManager.initIgnite();
            try {
                DataGenerator dataGenerator = initDataGenerator(session);
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
            int numConcepts = queryProfiler.aggregateCount();
            queryProfiler.processStaticQueries(repetitionsPerQuery, numConcepts);
        }

        session.close();
    }

    private DataGenerator initDataGenerator(Grakn.Session session) {
        int randomSeed = 0;
        String graphName = config.graphName();

        SchemaManager schemaManager = new SchemaManager(session, config.getGraqlSchema());
        HashSet<EntityType> entityTypes = schemaManager.getEntityTypes();
        HashSet<RelationshipType> relationshipTypes = schemaManager.getRelationshipTypes();
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
