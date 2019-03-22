package grakn.benchmark.report;

import grakn.core.client.GraknClient;
import grakn.core.concept.answer.Answer;
import grakn.core.concept.answer.ConceptList;
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
                // initialise results holder
                result.putIfAbsent(query, new QueryExecutionResults());

                // open a new write transaction and record execution time
                GraknClient.Transaction tx = session.transaction().write();

                // TODO record commit duration?
                tx.commit();

                String type;
                int roundTrips = -1;
                int conceptsHandled = -1;
                long startTime = 0;
                long endTime = -1; // set before start time so if not set properly, see negative time as a warning

                if (query instanceof GraqlGet) {
                    type = "get";

                    startTime = System.currentTimeMillis();
                    List<ConceptMap> answer = tx.execute(query.asGet());
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asGet(), answer);
                    conceptsHandled = AnswerAnalysis.retrievedConcepts(query.asGet(), answer);

                } else if (query instanceof GraqlInsert) {
                    type = "insert";

                    startTime = System.currentTimeMillis();
                    ConceptMap answer = tx.stream(query.asInsert()).findFirst().get();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asInsert(), answer);
                    conceptsHandled = AnswerAnalysis.insertedConcepts(query.asInsert(), answer);

                } else if (query instanceof GraqlDelete) {
                    type = "delete";

                    startTime = System.currentTimeMillis();
                    ConceptSet answer = tx.stream(query.asDelete()).findFirst().get();
                    endTime = System.currentTimeMillis();

                    roundTrips = AnswerAnalysis.roundTripsCompleted(query.asDelete(), answer);
                    conceptsHandled = AnswerAnalysis.deletedConcepts(query.asDelete(), answer);

                } else if (query instanceof GraqlCompute) {
                    type = "compute";
                } else {
                    type = "UNKNOWN";
                }

                // TODO collect data about this answer - eg how many were inserted/deleted/matched, how many round trips
                result.get(query).record( endTime - startTime, conceptsHandled, type, roundTrips, null);

                // TODO delete inserted concepts
            }
        }
        return result;
    }
}
