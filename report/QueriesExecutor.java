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

package grakn.benchmark.report;

import grakn.benchmark.report.container.QueryExecutionResults;
import grakn.core.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.answer.ConceptSet;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlDelete;
import graql.lang.query.GraqlGet;
import graql.lang.query.GraqlInsert;
import graql.lang.query.GraqlQuery;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

class QueriesExecutor implements Callable<Map<GraqlQuery, QueryExecutionResults>> {

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
        Map<GraqlQuery, QueryExecutionResults> result = new HashMap<>();
        for (int rep = 0; rep < repetitions; rep++) {
            for (GraqlQuery query : queries) {

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

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asGet(), answer);
                    conceptsHandled = AnswerAnalysis.retrievedConcepts(query.asGet(), answer);

                } else if (query instanceof GraqlInsert) {
                    queryType = "insert";

                    startTime = System.currentTimeMillis();
                    ConceptMap answer = tx.stream(query.asInsert()).findFirst().get();
                    tx.commit();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asInsert(), answer);
                    conceptsHandled = AnswerAnalysis.insertedConcepts(query.asInsert(), answer);

                    // TODO delete inserted concepts

                } else if (query instanceof GraqlDelete) {
                    queryType = "delete";

                    startTime = System.currentTimeMillis();
                    ConceptSet answer = tx.stream(query.asDelete()).findFirst().get();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asDelete(), answer);
                    conceptsHandled = AnswerAnalysis.deletedConcepts(query.asDelete(), answer);

                } else if (query instanceof GraqlCompute) {
                    queryType = "compute";

                    // TODO handle compute queries

                } else {
                    queryType = "UNKNOWN";
                }

                tx.close();

                // initialise data container if needed
                result.putIfAbsent(query, new QueryExecutionResults(queryType, conceptsHandled, roundTrips));
                result.get(query).addExecutionTime(endTime - startTime);

            }
        }
        return result;
    }
}
