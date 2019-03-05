package grakn.benchmark.profiler;


import brave.Span;
import brave.Tracer;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.Answer;
import graql.lang.query.GraqlQuery;

import java.util.List;

class ConcurrentQueries implements Runnable {

    private int concurrentId;
    private String graphName;
    private Tracer tracer;
    private final List<GraqlQuery> queries;
    private final int repetitions;
    private final int numConcepts;
    private final GraknClient.Session session;
    private final boolean commitQuery;
    private String executionName;

    public ConcurrentQueries(String executionName, int concurrentId, String graphName, Tracer tracer, List<GraqlQuery> queries, int repetitions, int numConcepts, GraknClient.Session session, boolean commitQuery) {
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
            long startTime = System.currentTimeMillis();
            for (int rep = 0; rep < repetitions; rep++) {
                for (GraqlQuery rawQuery : queries) {

                    if (counter % 100 == 0) {
                        System.out.println("Executed query #: " + counter + ", elapsed time " + (System.currentTimeMillis() - startTime));
                    }
                    Span querySpan = tracer.newChild(concurrentExecutionSpan.context());
                    querySpan.name("query");
                    querySpan.tag("query", rawQuery.toString());
                    querySpan.tag("repetitions", Integer.toString(repetitions));
                    querySpan.tag("repetition", Integer.toString(rep));
                    querySpan.start();

                    // perform trace in thread-local storage on the client
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(querySpan)) {
                        // open new transaction
                        GraknClient.Transaction tx = session.transaction().write();
                        List<? extends Answer> answer = tx.execute(rawQuery);

                        if (commitQuery) {
                            tx.commit();
                        }

                        tx.close();
                    } finally {
                        querySpan.finish();
                    }
                    counter++;
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
