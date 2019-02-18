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

import brave.Tracing;
import grakn.core.client.GraknClient;
import grakn.core.graql.answer.Value;
import grakn.core.graql.query.Graql;
import grakn.core.graql.query.query.GraqlQuery;
import grakn.core.server.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

import static grakn.core.graql.query.Graql.var;


/**
 *
 */
public class QueryProfiler {

    private static final Logger LOG = LoggerFactory.getLogger(QueryProfiler.class);

    private final String executionName;
    private final String graphName;
    private final List<GraqlQuery> queries;
    private boolean commitQueries;
    private final List<GraknClient.Session> sessions;
    private ExecutorService executorService;

    public QueryProfiler(List<GraknClient.Session> sessions, String executionName, String graphName, List<String> queryStrings, boolean commitQueries) {
        this.sessions = sessions;

        this.executionName = executionName;
        this.graphName = graphName;

        // convert Graql strings into GraqlQuery types
        this.queries = queryStrings.stream()
                .map(q -> (GraqlQuery) Graql.parse(q))
                .collect(Collectors.toList());

        this.commitQueries = commitQueries;

        // create 1 thread per client session
        executorService = Executors.newFixedThreadPool(sessions.size());
    }

    public void processStaticQueries(int numRepeats, int numConcepts) {
        LOG.trace("Starting processStaticQueries");
        this.processQueries(queries, numRepeats, numConcepts);
        LOG.trace("Finished processStaticQueries");
    }

    public int aggregateCount(GraknClient.Session session) {
        try (GraknClient.Transaction tx = session.transaction(Transaction.Type.READ)) {
            List<Value> count = tx.execute(Graql.match(var("x").isa("thing")).get().count());
            return count.get(0).number().intValue();
        }
    }

    void processQueries(List<GraqlQuery> queries, int repetitions, int numConcepts) {
        List<Future> runningConcurrentQueries = new LinkedList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < sessions.size(); i++) {
            GraknClient.Session session = sessions.get(i);
            ConcurrentQueries processor = new ConcurrentQueries(executionName, i, graphName, Tracing.currentTracer(), queries, repetitions, numConcepts, session, commitQueries);
            runningConcurrentQueries.add(executorService.submit(processor));
        }

        // wait until all threads have finished
        try {
            for (Future future : runningConcurrentQueries) {
                future.get();
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }

        long length = System.currentTimeMillis() - start;
        System.out.println("Time: " + length);
    }

    public void cleanup() {
        executorService.shutdown();
    }
}

