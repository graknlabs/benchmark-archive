package grakn.benchmark.report;

import java.util.LinkedList;
import java.util.List;

public class QueryExecutionResults {
    private List<Long> queryExecutionTimes = new LinkedList<>();
    private Integer conceptsInvolved = null;
    private String type = null;
    private Integer roundTrips = null;

    public QueryExecutionResults() {
    }

    public void record(Long milliseconds, Integer conceptsInvolved, String type, Integer roundTrips) {
        queryExecutionTimes.add(milliseconds);
        this.conceptsInvolved = conceptsInvolved;
        this.type = type;
        this.roundTrips = roundTrips;
    }

    public List<Long> times() {
        return queryExecutionTimes;
    }

    public Integer concepts() {
        return conceptsInvolved;
    }

    public String type() {
        return type;
    }

    public Integer roundTrips() {
        return roundTrips;
    }

    /**
     * Merge two Results together into one object
     */
    public QueryExecutionResults merge(QueryExecutionResults queryExecutionsResult) {
        QueryExecutionResults result = new QueryExecutionResults();

        if (times().size() > 0) {
            // check if the merged item has any data, otherwise will be all nulls
            for (Long t : times()) {
                result.record(t, conceptsInvolved, type, roundTrips);
            }
        }

        if (queryExecutionsResult.times().size() > 0) {
            // check if the merged item has any data, otherwise will be all nulls
            for (Long t : queryExecutionsResult.times()) {
                result.record(t, queryExecutionsResult.concepts(), queryExecutionsResult.type(), queryExecutionsResult.roundTrips());
            }
        }

        return result;
    }
}