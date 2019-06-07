package grakn.benchmark.profiler.usecase;

import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.profiler.ThreadedProfiler;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.client.GraknClient;

import java.util.Collections;
import java.util.List;

public class LoadSchema implements UseCase {
    private final BenchmarkConfiguration config;
    private final GraknClient client;
    private final SchemaManager schemaManager;

    LoadSchema(BenchmarkConfiguration config, GraknClient client, SchemaManager schemaManager) {
        this.config = config;
        this.client = client;
        this.schemaManager = schemaManager;
    }

    @Override
    public void run() {
        schemaManager.loadSchema();
        List<String> keyspaces = Collections.singletonList(config.getKeyspace());
        int numConcepts = 0;

        ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, keyspaces, config);
        threadedProfiler.processQueries(config.numQueryRepetitions(), numConcepts);
        threadedProfiler.cleanup();
    }
}
