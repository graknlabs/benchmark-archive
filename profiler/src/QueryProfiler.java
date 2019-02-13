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

import brave.Span;
import brave.Tracer;
import brave.Tracing;
import grakn.core.GraknTxType;
import grakn.core.client.Grakn;
import grakn.core.graql.Graql;
import grakn.core.graql.Query;
import grakn.core.graql.answer.Answer;
import grakn.core.graql.answer.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static grakn.core.graql.Graql.var;

/**
 *
 */
public class QueryProfiler {

    private static final Logger LOG = LoggerFactory.getLogger(QueryProfiler.class);

    private final String executionName;
    private final String graphName;
    private final List<Query> queries;
    private boolean commitQueries;
    private final List<Grakn.Session> sessions;
    private ExecutorService executorService;

    public QueryProfiler(List<Grakn.Session> sessions, String executionName, String graphName, List<String> queryStrings, boolean commitQueries) {
        this.sessions = sessions;

        this.executionName = executionName;
        this.graphName = graphName;

        // convert Graql strings into Query types
        this.queries = queryStrings.stream()
                .map(q -> (Query) Graql.parser().parseQuery(q))
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

    public int aggregateCount(Grakn.Session session) {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            List<Value> count = tx.graql().match(var("x").isa("thing")).aggregate(Graql.count()).execute();
            return count.get(0).number().intValue();
        }
    }

    void processQueries(List<Query> queries, int repetitions, int numConcepts) {
        List<Future> runningQueryProcessors = new LinkedList<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < sessions.size(); i++) {
            Grakn.Session session = sessions.get(i);
            QueryProcessor processor = new QueryProcessor(executionName, i, graphName, Tracing.currentTracer(), queries, repetitions, numConcepts, session, commitQueries);
            runningQueryProcessors.add(executorService.submit(processor));
        }

        // wait until all threads have finished
        try {
            for (Future future : runningQueryProcessors) {
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

class QueryProcessor implements Runnable {

    private int concurrentId;
    private String graphName;
    private Tracer tracer;
    private final List<Query> queries;
    private final int repetitions;
    private final int numConcepts;
    private final Grakn.Session session;
    private final boolean commitQuery;
    private String executionName;

    public QueryProcessor(String executionName, int concurrentId, String graphName, Tracer tracer, List<Query> queries, int repetitions, int numConcepts, Grakn.Session session, boolean commitQuery) {
        this.executionName = executionName;
        this.concurrentId = concurrentId;
        this.graphName = graphName;
        this.tracer = tracer;
        this.queries = queries;
        this.repetitions = repetitions;
        this.numConcepts = numConcepts;
        this.session = session;
        this.commitQuery = commitQuery;
    }


    @Override
    public void run() {
        try {
            Span concurrentExecutionSpan = tracer.newTrace().name("concurrent-execution");
            concurrentExecutionSpan.tag("executionName", executionName);
            concurrentExecutionSpan.tag("concurrentClient", Integer.toString(concurrentId));
            concurrentExecutionSpan.tag("graphName", this.graphName);
            concurrentExecutionSpan.tag("repetitions", Integer.toString(repetitions));
            concurrentExecutionSpan.tag("scale", Integer.toString(numConcepts));
            concurrentExecutionSpan.start();

            int counter = 0;
            for (int rep = 0; rep < repetitions; rep++) {
                for (Query rawQuery : queries) {
                    counter++;
                    Span querySpan = tracer.newChild(concurrentExecutionSpan.context());
                    querySpan.name("query");
                    querySpan.tag("query", rawQuery.toString());
                    querySpan.tag("repetitions", Integer.toString(repetitions));
                    querySpan.tag("repetition", Integer.toString(rep));
                    querySpan.start();

                    // perform trace in thread-local storage on the client
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(querySpan)) {
                        // open new transaction
                        Grakn.Transaction tx = session.transaction(GraknTxType.WRITE);
                        // attach query to transaction
                        Query query = rawQuery.withTx(tx);
                        if (counter % 100 == 0) {
                            System.out.println(String.format("%d [c%d] Profiling... (repetition %d/%d):\t%s", counter, concurrentId, rep + 1, repetitions, query.toString()));
                        }
                        List<Answer> answer = query.execute();

                        if (commitQuery) {
                            tx.commit();
                        }

                        tx.close();
                    } finally {
                        querySpan.finish();
                    }
                }
            }

            concurrentExecutionSpan.finish();
            // give zipkin reporter time to finish transmitting spans/close spans cleanly
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            System.out.println("Thread sleeps during data generation were interrupted");
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
        System.out.println("Thread runnable finished running queries");
        System.out.print("\n\n");
    }
}
