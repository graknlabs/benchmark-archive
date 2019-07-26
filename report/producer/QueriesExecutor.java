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

package grakn.benchmark.report.producer;

import grakn.benchmark.report.producer.container.QueryExecutionResults;
import grakn.client.GraknClient;
import grakn.core.concept.Concept;
import grakn.core.concept.answer.AnswerGroup;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.answer.ConceptSet;
import grakn.core.concept.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class QueriesExecutor implements Callable<Map<GraqlQuery, QueryExecutionResults>> {

    private static final Logger LOG = LoggerFactory.getLogger(QueriesExecutor.class);

    private final List<GraqlQuery> queries;
    private int repetitions;
    private final GraknClient.Session session;

    public QueriesExecutor(GraknClient.Session session, List<GraqlQuery> queries, int repetitions) {
        this.session = session;
        this.queries = queries;
        this.repetitions = repetitions;
    }

    @Override
    public Map<GraqlQuery, QueryExecutionResults> call() {
        Map<GraqlQuery, QueryExecutionResults> queryData = new HashMap<>();

        List<String> insertedConceptIds = new LinkedList<>();

        for (int rep = 0; rep < repetitions; rep++) {
            for (GraqlQuery query : queries) {

                LOG.info("Repetition " + rep + ", this thread executed: " + query);

                // open a new write transaction and record execution time
                GraknClient.Transaction tx = session.transaction().write();

                String queryType;
                int roundTrips = -1;
                int conceptsHandled = -1;
                long startTime = 0;
                long endTime = -1; // set before start time so if not set properly, see negative time as a warning

                if (query instanceof GraqlGet) {
                    queryType = "get";

                    startTime = System.currentTimeMillis();
                    List<ConceptMap> answer = tx.execute(query.asGet());
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.countRoundTripsCompleted(query.asGet(), answer);
                    conceptsHandled = AnswerAnalysis.countRetrievedConcepts(query.asGet(), answer);

                } else if (query instanceof GraqlInsert) {
                    queryType = "insert";

                    startTime = System.currentTimeMillis();
                    ConceptMap answer = tx.stream(query.asInsert()).findFirst().get();
                    tx.commit();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.countRoundTripsCompleted(query.asInsert(), answer);
                    conceptsHandled = AnswerAnalysis.countInsertedConcepts(query.asInsert(), answer);

                    for (Concept concept : answer.concepts()) {
                        insertedConceptIds.add(concept.id().toString());
                    }

                } else if (query instanceof GraqlDelete) {
                    queryType = "delete";

                    startTime = System.currentTimeMillis();
                    ConceptSet answer = tx.stream(query.asDelete()).findFirst().get();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.countRoundTripsCompleted(query.asDelete(), answer);
                    conceptsHandled = AnswerAnalysis.countDeletedConcepts(query.asDelete(), answer);

                } else if (query instanceof GraqlCompute) {
                    queryType = "compute";

                    // TODO handle compute queries

                } else if (query instanceof GraqlGet.Group) {
                    queryType = "aggregate";

                    startTime = System.currentTimeMillis();
                    List<AnswerGroup<ConceptMap>> answer = tx.execute(query.asGetGroup());
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.countRoundTripsCompleted(answer);
                    conceptsHandled = AnswerAnalysis.countGroupedConcepts(answer);
                } else if (query instanceof GraqlGet.Aggregate) {
                    queryType = "aggregate";

                    startTime = System.currentTimeMillis();
                    List<Numeric> answer = tx.execute(query.asGetAggregate());
                    endTime = System.currentTimeMillis();

                    roundTrips = 2;
                    conceptsHandled = answer.get(0).number().intValue();
                } else {
                    queryType = "UNKNWON";
                }

                tx.close();

                // initialise data container if needed
                queryData.putIfAbsent(query, new QueryExecutionResults(queryType, conceptsHandled, roundTrips));
                queryData.get(query).addExecutionTime(endTime - startTime);

            }
        }


        // delete all the inserted concepts
        try (GraknClient.Transaction tx = session.transaction().write()) {
            insertedConceptIds.iterator().forEachRemaining(id -> tx.execute(Graql.parse("match $x id " + id+ "; delete $x;").asDelete()));
        }

        return queryData;
    }
}
