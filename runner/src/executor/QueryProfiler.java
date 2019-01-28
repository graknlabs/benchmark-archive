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

package grakn.benchmark.runner.executor;

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

import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static grakn.core.graql.Graql.var;

/**
 *
 */
public class QueryProfiler {

    private static final Logger LOG = LoggerFactory.getLogger(QueryProfiler.class);

    private final String executionName;
    private final String graphName;
    private final List<Query> queries;
    private final Grakn.Session session;

    public QueryProfiler(Grakn.Session session, String executionName, String graphName, List<String> queryStrings) {
        this.session = session;

        this.executionName = executionName;
        this.graphName = graphName;

        // convert Graql strings into Query types
        this.queries = queryStrings.stream()
                        .map(q -> (Query)Graql.parser().parseQuery(q))
                        .collect(Collectors.toList());
    }

    public void processStaticQueries(int numRepeats, int numConcepts, String extraMessage) {
        try {
            this.processQueries(queries.stream(), numRepeats, numConcepts, extraMessage);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void processStaticQueries(int numRepeats, int numConcepts) {
        processStaticQueries(numRepeats, numConcepts, null);
    }

    public int aggregateCount() {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            List<Value> count = tx.graql().match(var("x").isa("thing")).aggregate(Graql.count()).execute();
            return count.get(0).number().intValue();
        }
    }

    /**
     *
     * @param queryStream stream of Graql queries
     * @param numRepeats
     * @param numConcepts
     * @param msg
     * @throws Exception
     */
    void processQueries(Stream<Query> queryStream, int numRepeats, int numConcepts, String msg) throws Exception {

        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            Tracer tracer = Tracing.currentTracer();

            Iterator<Query> queryIterator = queryStream.iterator();
            int counter = 0;
            while (queryIterator.hasNext()) {

                Query query = queryIterator.next().withTx(tx);
                LOG.info("Running query: " + query.toString());

                Span batchSpan = tracer.newTrace().name("batch-query");
                batchSpan.tag("scale", Integer.toString(numConcepts));
                batchSpan.tag("query", query.toString());
                batchSpan.tag("executionName", this.executionName);
                batchSpan.tag("repetitions", Integer.toString(numRepeats));
                batchSpan.tag("graphName", this.graphName);
                if (msg != null) {
                    batchSpan.tag("extraTag", msg);
                }
                batchSpan.start();

                for (int i = 0; i < numRepeats; i++) {

                    Span span = tracer.newChild(batchSpan.context()).name("query-repetition");
                    span.tag("repetition", Integer.toString(i));
                    span.start();

                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
                        List<Answer> answer = query.execute();
                    } catch (RuntimeException | Error e) {
                        span.error(e);
                        throw e;
                    } finally {
                        span.finish();
                    }
                    counter ++;
                }
                batchSpan.finish();
                Thread.sleep(500);
            }
            Thread.sleep(1500);
            System.out.println(counter);
        }
    }
}
