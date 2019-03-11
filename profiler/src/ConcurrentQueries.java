package grakn.benchmark.profiler;


import brave.Span;
import brave.Tracer;
import grakn.benchmark.profiler.analysis.InsertQueryAnalyser;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.Answer;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

class ConcurrentQueries implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConcurrentQueries.class);

    private int concurrentId;
    private String graphName;
    private Tracer tracer;
    private final List<GraqlQuery> queries;
    private final int repetitions;
    private final int numConcepts;
    private final GraknClient.Session session;
    private final boolean deleteInsertedConcepts;
    private String executionName;

    public ConcurrentQueries(String executionName, int concurrentId, String graphName, Tracer tracer, List<GraqlQuery> queries, int repetitions, int numConcepts, GraknClient.Session session, boolean deleteInsertedConcepts) {
        this.executionName = executionName;
        this.concurrentId = concurrentId;
        this.graphName = graphName;
        this.tracer = tracer;
        this.queries = queries;
        this.repetitions = repetitions;
        this.numConcepts = numConcepts;
        this.session = session;
        this.deleteInsertedConcepts = deleteInsertedConcepts;
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
                for (GraqlQuery query : queries) {

                    if (counter % 100 == 0) {
                        System.out.println("Executed query #: " + counter + ", elapsed time " + (System.currentTimeMillis() - startTime));
                    }
                    Span querySpan = tracer.newChild(concurrentExecutionSpan.context());
                    querySpan.name("query");
                    querySpan.tag("query", query.toString());
                    querySpan.tag("repetitions", Integer.toString(repetitions));
                    querySpan.tag("repetition", Integer.toString(rep));
                    querySpan.start();

                    // perform trace in thread-local storage on the client
                    List<String> insertedConceptIds = null;
                    try (Tracer.SpanInScope span = tracer.withSpanInScope(querySpan)) {
                        // open new transaction
                        GraknClient.Transaction tx = session.transaction().write();
                        List<? extends Answer> answer = tx.execute(query);

                        if (query instanceof GraqlInsert) {
                            insertedConceptIds = InsertQueryAnalyser.getInsertedConcepts((GraqlInsert)query, (List<ConceptMap>)answer)
                                        .stream().map(concept -> concept.id().toString())
                                        .collect(Collectors.toList());
                        }
                        tx.commit();
                    } finally {
                        querySpan.finish();
                    }


                    if (deleteInsertedConcepts && insertedConceptIds != null) {
                        Span deleteQuerySpan = tracer.newChild(concurrentExecutionSpan.context());
                        deleteQuerySpan.name("deleteQuery");
                        try (Tracer.SpanInScope span = tracer.withSpanInScope(deleteQuerySpan)) {
                            for (String conceptId : insertedConceptIds) {
                                try {
                                    GraknClient.Transaction tx = session.transaction().write();
                                    tx.execute(Graql.parse("match $x id " + conceptId + "; delete $x;").asDelete());
                                    tx.commit();
                                } catch (Exception e) {
                                    // we may be trying to delete an ID that has been de-duplicated
                                    // because we're eventually consistent, so this is non-fatal
                                    LOG.warn("Delete query failed following insert", e);
                                }
                            }
                        } finally {
                            deleteQuerySpan.finish();
                        }
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
        } finally {
            session.close();
        }
        System.out.println("Thread runnable finished running queries");
        System.out.print("\n\n");
    }
}
