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


import brave.Span;
import brave.Tracer;
import grakn.benchmark.common.analysis.InsertQueryAnalyser;
import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.client.GraknClient;
import grakn.core.concept.answer.Answer;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.Graql;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

class QueryProfiler implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(QueryProfiler.class);

    private int concurrentId;
    private String configName;
    private String description;
    private String dataGenerator;
    private Tracer tracer;
    private final List<GraqlQuery> queries;
    private final int repetitions;
    private final int numConcepts;
    private final GraknClient.Session session;
    private final boolean deleteInsertedConcepts;
    private final boolean traceDeleteInsertedConcepts;
    private String executionName;

    public QueryProfiler(BenchmarkConfiguration config, int concurrentId, Tracer tracer, List<GraqlQuery> queries, int repetitions, int numConcepts, GraknClient.Session session) {
        configName = config.configName();
        description = config.configDescription();
        executionName = config.executionName();
        deleteInsertedConcepts = config.deleteInsertedConcepts();
        traceDeleteInsertedConcepts = config.traceDeleteInsertedConcepts();
        dataGenerator = config.dataGenerator();
        this.concurrentId = concurrentId;
        this.tracer = tracer;
        this.queries = queries;
        this.repetitions = repetitions;
        this.numConcepts = numConcepts;
        this.session = session;
    }

    @Override
    public void run() {
        try {
            Span concurrentExecutionSpan = tracer.newTrace().name("concurrent-execution");
            concurrentExecutionSpan.tag("configurationName", configName);
            concurrentExecutionSpan.tag("description", description);
            concurrentExecutionSpan.tag("executionName", executionName);
            concurrentExecutionSpan.tag("concurrentClient", Integer.toString(concurrentId));
            concurrentExecutionSpan.tag("graphGeneratorDefinition", dataGenerator);
            concurrentExecutionSpan.tag("queryRepetitions", Integer.toString(repetitions));
            concurrentExecutionSpan.tag("graphScale", Integer.toString(numConcepts));
            concurrentExecutionSpan.start();

            int counter = 0;
            long startTime = System.currentTimeMillis();
            for (int rep = 0; rep < repetitions; rep++) {
                for (GraqlQuery query : queries) {

                    if (counter % 100 == 0) {
                        System.out.println("Executed query #: " + counter + ", elapsed time " + (System.currentTimeMillis() - startTime));
                    }
                    Span querySpan = tracer.newChild(concurrentExecutionSpan.context());

                    if (query instanceof GraqlInsert) { querySpan.name("insert-query"); }
                    else if (query instanceof GraqlGet) { querySpan.name("get-query"); }
                    else if (query instanceof GraqlDelete) { querySpan.name("delete-query"); }
                    else if (query instanceof GraqlCompute) { querySpan.name("compute-query"); }
                    else { querySpan.name("query"); }

                    querySpan.tag("query", query.toString());
                    querySpan.tag("repetitions", Integer.toString(repetitions));
                    querySpan.tag("repetition", Integer.toString(rep));
                    querySpan.start();

                    // perform trace in thread-local storage on the client
                    Set<String> insertedConceptIds = null;
                    try (Tracer.SpanInScope span = tracer.withSpanInScope(querySpan)) {
                        // open new transaction
                        GraknClient.Transaction tx = session.transaction().write();
                        List<? extends Answer> answer = tx.execute(query);

                        if (query instanceof GraqlInsert) {
                            insertedConceptIds = InsertQueryAnalyser.getInsertedConcepts((GraqlInsert)query, (List<ConceptMap>)answer)
                                        .stream().map(concept -> concept.id().toString())
                                        .collect(Collectors.toSet());
                        }
                        tx.commit();
                    } catch (Exception e) {
                        LOG.error(
                                "Exception in a concurrent query executor, query: " + query +
                                ". Ensure every query is valid and each inserted concept is associated with an explicit variable",
                                e);
                        throw e;
                    } finally {
                        querySpan.finish();
                    }


                    if (deleteInsertedConcepts && insertedConceptIds != null) {
                        if (traceDeleteInsertedConcepts) {
                            Span deleteQuerySpan = tracer.newChild(concurrentExecutionSpan.context());
                            deleteQuerySpan.name("delete-query");
                            deleteQuerySpan.start();
                            try (Tracer.SpanInScope span = tracer.withSpanInScope(deleteQuerySpan)) {
                                // create one tx per ID in case the concept no longer exists and throws an error (attr dedup)
                                GraknClient.Transaction tx = session.transaction().write();
                                for (String conceptId : insertedConceptIds) {
                                    tx.execute(Graql.parse("match $x id " + conceptId + "; delete $x;").asDelete());
                                }
                                tx.commit();
                            } finally {
                                deleteQuerySpan.finish();
                            }
                        } else {
                            GraknClient.Transaction tx = session.transaction().write();
                            insertedConceptIds.iterator().forEachRemaining(id -> tx.execute(Graql.parse("match $x id " + id + "; delete $x;").asDelete()));
                            tx.commit();
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
