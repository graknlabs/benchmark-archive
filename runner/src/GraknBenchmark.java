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

package grakn.benchmark.runner;

import grakn.benchmark.runner.exception.DataGeneratorException;
import grakn.benchmark.runner.executor.QueryProfiler;
import grakn.benchmark.runner.generator.DataGenerator;
import grakn.benchmark.runner.util.SchemaManager;
import grakn.benchmark.runner.util.BenchmarkArguments;
import grakn.benchmark.runner.util.BenchmarkConfiguration;
import grakn.benchmark.runner.util.ElasticSearchManager;
import grakn.core.client.Grakn;
import grakn.core.util.SimpleURI;
import org.apache.commons.cli.CommandLine;
import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteLogger;
import org.apache.ignite.Ignition;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.logger.slf4j.Slf4jLogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
        Ignite ignite = initIgnite();
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
            ignite.close();
            System.exit(exitCode);
        }
    }

    public GraknBenchmark(CommandLine arguments) {
        BenchmarkConfiguration benchmarkConfig = new BenchmarkConfiguration(arguments);
        this.config = benchmarkConfig;
    }


    /**
     * Start the Grakn Benchmark, which, based on arguments provided via console, will run one of the following use cases:
     * - generate synthetic data while profiling the graph at different sizes
     * - don't generate new data and only profile an existing keyspace
     */
    public void start() {
        Grakn client = new Grakn(new SimpleURI(config.graknUri()), true);
        Grakn.Session session = client.session(config.getKeyspace());
        SchemaManager.verifyEmptyKeyspace(session);
        QueryProfiler queryProfiler = new QueryProfiler(session, config.executionName(), config.graphName(), config.getQueries());
        int repetitionsPerQuery = config.numQueryRepetitions();

        //TODO add check to make sure currentKeyspace does not exist, if it does throw exception
        // this can be done once we implement keyspaces().retrieve() on the client Java (issue #4675)

        if (config.generateData()) {
            int randomSeed = 0;
            DataGenerator dataGenerator = new DataGenerator(session, config, randomSeed);

            List<Integer> numConceptsInRun = config.scalesToProfile();
            for (int numConcepts : numConceptsInRun) {
                LOG.info("Generating graph to scale... " + numConcepts);
                dataGenerator.generate(numConcepts);
                queryProfiler.processStaticQueries(repetitionsPerQuery, numConcepts);
            }
        } else {
            int numConcepts = queryProfiler.aggregateCount();
            queryProfiler.processStaticQueries(repetitionsPerQuery, numConcepts);
        }

        session.close();
    }

    private static Ignite initIgnite() {
        System.setProperty("IGNITE_QUIET", "false"); // When Ignite is in quiet mode forces all the output to System.out, we don't want that
        System.setProperty("IGNITE_NO_ASCII", "true"); // Disable Ignite ASCII logo
        System.setProperty("IGNITE_PERFORMANCE_SUGGESTIONS_DISABLED", "true"); // Enable suggestions when need performance improvements
        System.setProperty("java.net.preferIPv4Stack", "true"); // As suggested by Ignite we set preference on IPv4
        IgniteConfiguration igniteConfig = new IgniteConfiguration();
        IgniteLogger logger = new Slf4jLogger();
        igniteConfig.setGridLogger(logger);
        return Ignition.start(igniteConfig);
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
