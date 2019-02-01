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

    public void processStaticQueries(int numRepeats, int numConcepts) {
        try {
            LOG.info("Starting processStaticQueries");
            this.processQueries(queries.stream(), numRepeats, numConcepts);
            LOG.info("Finished processStaticQueries");
        } catch (Exception e) {
            e.printStackTrace();
            LOG.info("LOL", e);
        }
    }

    public int aggregateCount() {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            List<Value> count = tx.graql().match(var("x").isa("thing")).aggregate(Graql.count()).execute();
            return count.get(0).number().intValue();
        }
    }

    void processQueries(Stream<Query> queryStream, int repetitions, int numConcepts) throws Exception {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {
            Tracer tracer = Tracing.currentTracer();

            List<Query> queries = queryStream.collect(Collectors.toList());

            for (int rep = 0; rep < repetitions; rep++) {

                for (Query rawQuery : queries) {
                    Query query = rawQuery.withTx(tx);

                    this.eraselinePrint(String.format("Profiling... (repetition %d/%d):\t%s", rep+1, repetitions, query.toString()));
                    LOG.info("Profiling... repetition " + rep + ": " + query.toString());
                    Span querySpan = tracer.newTrace().name("query");
                    LOG.info("New trace with `query` name created");
                    querySpan.tag("scale", Integer.toString(numConcepts));
                    LOG.info("Tag `scale`");
                    querySpan.tag("query", query.toString());
                    LOG.info("Tag `query`");
                    querySpan.tag("executionName", this.executionName);
                    LOG.info("Tag `executionName`");
                    querySpan.tag("repetitions", Integer.toString(repetitions));
                    LOG.info("Tag `repetitions`");
                    querySpan.tag("graphName", this.graphName);
                    LOG.info("Tag `graphName`");
                    querySpan.tag("repetition", Integer.toString(rep));
                    LOG.info("Tag `repetition`");
                    querySpan.start();
                    LOG.info("Started query span");

                    // perform trace in thread-local storage on the client
                    try (Tracer.SpanInScope ws = tracer.withSpanInScope(querySpan)) {
                        List<Answer> answer = query.execute();
                        LOG.info("executed query successfully");
                    } catch (RuntimeException | Error e) {
                        querySpan.error(e);
                        LOG.info("same error should be logged again after LOL", e);
                        throw e;
                    } finally {
                        querySpan.finish();
                        LOG.info("query span finished");
                    }
                }
                // wait for out-of-band reporting to complete
                Thread.sleep(500);
            }
            // wait for out-of-band reporting to complete
            Thread.sleep(1500);
        }
        System.out.print("\n\n");
    }

    private void eraselinePrint(String msg) {
        System.out.print("\r");
        System.out.print(msg);
    }
}
