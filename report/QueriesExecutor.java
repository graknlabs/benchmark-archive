package grakn.benchmark.report;

import grakn.core.client.GraknClient;
import grakn.core.concept.answer.Answer;
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
                Long startTime = System.currentTimeMillis();
                List<? extends Answer> answer = tx.execute(query);
                long endTime = System.currentTimeMillis();

                // TODO record commit time?
                tx.commit();

                String type;
                if (query instanceof GraqlGet) {
                    type = "get";
                } else if (query instanceof GraqlInsert) {
                    type = "insert";
                } else if (query instanceof GraqlDelete) {
                    type = "delete";
                } else if (query instanceof GraqlCompute) {
                    type = "compute";
                } else {
                    type = "UNKNOWN";
                }

                // TODO collect data about this answer - eg how many were inserted/deleted/matched, how many round trips
                result.get(query).record( endTime - startTime, -1, type, -1, null);

                // TODO delete inserted concepts
            }
        }
        return result;
    }
}
