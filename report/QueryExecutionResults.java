package grakn.benchmark.report;

import java.util.LinkedList;
import java.util.List;

public class QueryExecutionResults {
    private List<Long> queryExecutionTimes;
    private Integer conceptsInvolved = null;
    private String queryType = null;
    private Integer roundTrips = null;
    private Integer scale = null;

    public QueryExecutionResults() {
        queryExecutionTimes = new LinkedList<>();
    }

    public void record(Long milliseconds, Integer conceptsInvolved, String queryType, Integer roundTrips, Integer scale) {
        queryExecutionTimes.add(milliseconds);
        this.conceptsInvolved = conceptsInvolved;
        this.queryType = queryType;
        this.roundTrips = roundTrips;
        this.scale = scale;
    }

    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public List<Long> times() {
        return queryExecutionTimes;
    }

    public Integer concepts() {
        return conceptsInvolved;
    }

    public String queryType() {
        return queryType;
    }

    public Integer roundTrips() {
        return roundTrips;
    }

    public Integer scale() { return scale; }
}