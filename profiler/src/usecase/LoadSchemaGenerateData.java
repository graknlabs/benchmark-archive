package grakn.benchmark.profiler.usecase;

import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.common.timer.BenchmarkingTimer;
import grakn.benchmark.generator.DataGenerator;
import grakn.benchmark.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.generator.definition.DefinitionFactory;
import grakn.benchmark.generator.query.QueryProvider;
import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.benchmark.generator.storage.IgniteConceptStorage;
import grakn.benchmark.generator.util.IgniteManager;
import grakn.benchmark.generator.util.KeyspaceSchemaLabels;
import grakn.benchmark.profiler.ThreadedProfiler;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.client.GraknClient;
import org.apache.ignite.Ignite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.List;
import java.util.Random;

public class LoadSchemaGenerateData implements UseCase {
    private static final Logger LOG = LoggerFactory.getLogger(LoadSchemaGenerateData.class);

    private final BenchmarkConfiguration config;
    private final GraknClient client;
    private final SchemaManager schemaManager;

    LoadSchemaGenerateData(BenchmarkConfiguration config, GraknClient client, SchemaManager schemaManager) {
        this.config = config;
        this.client = client;
        this.schemaManager = schemaManager;
    }

    @Override
    public void run() {

        schemaManager.loadSchema();

        Ignite ignite = IgniteManager.initIgnite();
        ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, Collections.singletonList(config.getKeyspace()), config);
        BenchmarkingTimer timer = new BenchmarkingTimer();
        DataGenerator dataGenerator = initDataGenerator(config.getKeyspace(), timer);
        List<Integer> numConceptsInRun = config.scalesToProfile();

        try {
            timer.startGenerateAndTrack();
            for (int numConcepts : numConceptsInRun) {
                LOG.info("\nGenerating graph to scale... " + numConcepts);
                dataGenerator.generate(numConcepts);
                timer.startQueryTimeTracking();
                threadedProfiler.processQueries(config.numQueryRepetitions(), numConcepts);
                timer.endQueryTimeTracking();
                timer.printTimings();
            }
        } finally {
            threadedProfiler.cleanup();
            ignite.close();
        }
    }

    /**
     * Connect a data generator to pre-prepared keyspace
     */
    private DataGenerator initDataGenerator(String keyspace, BenchmarkingTimer timer) {
        int randomSeed = 0;
        String dataGenerator = config.dataGenerator();
        KeyspaceSchemaLabels schemaLabels = new KeyspaceSchemaLabels(client, keyspace);
        ConceptStorage storage = new IgniteConceptStorage(schemaLabels);
        DataGeneratorDefinition dataGeneratorDefinition = DefinitionFactory.getDefinition(dataGenerator, new Random(randomSeed), storage);
        QueryProvider queryProvider = new QueryProvider(dataGeneratorDefinition);
        return new DataGenerator(client, keyspace, storage, dataGenerator, queryProvider, timer);
    }

}
