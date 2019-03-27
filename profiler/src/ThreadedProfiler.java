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

import brave.Tracing;
import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.profiler.util.ProfilerException;
import grakn.client.GraknClient;
import graql.lang.Graql;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;


/**
 *
 */
public class ThreadedProfiler {

    private static final Logger LOG = LoggerFactory.getLogger(ThreadedProfiler.class);

    private final BenchmarkConfiguration config;

    private final List<GraqlQuery> queries;
    private final GraknClient client;
    private final List<String> keyspaces;
    private final int concurrentClients;
    private ExecutorService executorService;

    public ThreadedProfiler(GraknClient client, List<String> keyspaces, BenchmarkConfiguration config) {
        this.config = config;
        this.client = client;
        this.keyspaces = keyspaces;
        this.concurrentClients = config.concurrentClients();

        // convert Graql strings into GraqlQuery types
        this.queries = config.getQueries().stream()
                .map(q -> (GraqlQuery) Graql.parse(q))
                .collect(Collectors.toList());


        // create 1 thread per client session
        executorService = Executors.newFixedThreadPool(concurrentClients);
    }

    public void processStaticQueries(int numRepeats, int numConcepts) {
        LOG.trace("Starting processStaticQueries");
        this.processQueries(queries, numRepeats, numConcepts);
        LOG.trace("Finished processStaticQueries");
    }

    void processQueries(List<GraqlQuery> queries, int repetitions, int numConcepts) {
        List<Future> runningConcurrentQueries = new LinkedList<>();
        List<GraknClient.Session> openSessions = new LinkedList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < concurrentClients; i++) {
            // TODO: this can probably be optimised (keeping sessions open)
            String keyspace = (keyspaces.size() > 1) ? keyspaces.get(i) : keyspaces.get(0);
            GraknClient.Session session = client.session(keyspace);
            openSessions.add(session);
            QueryProfiler processor = new QueryProfiler(config, i, Tracing.currentTracer(), queries, repetitions, numConcepts, session);
            runningConcurrentQueries.add(executorService.submit(processor));
        }

        // wait until all threads have finished
        try {
            for (Future future : runningConcurrentQueries) {
                future.get();
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new ProfilerException("Error in execution of profiled queries", e);
        } finally {
            openSessions.forEach(GraknClient.Session::close);
        }

        long length = System.currentTimeMillis() - start;
        System.out.println("Time: " + length);
    }

    public void cleanup() {
        executorService.shutdown();
    }
}

