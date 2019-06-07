package grakn.benchmark.profiler.usecase;

import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.profiler.ThreadedProfiler;
import grakn.client.GraknClient;

import java.util.Collections;

public class ProfileExisting implements UseCase {
    private final BenchmarkConfiguration config;
    private final GraknClient client;

    ProfileExisting(BenchmarkConfiguration config, GraknClient client) {
        this.config = config;
        this.client = client;
    }

    @Override
    public void run() {
        ThreadedProfiler threadedProfiler = new ThreadedProfiler(client, Collections.singletonList(config.getKeyspace()), config);

//            int numConcepts = threadedProfiler.aggregateCount();
        int numConcepts = 0; // TODO re-add this properly for concurrent clients
        threadedProfiler.processQueries(config.numQueryRepetitions(), numConcepts);
        threadedProfiler.cleanup();
        client.close();
    }
}
