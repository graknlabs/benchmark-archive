package grakn.benchmark.report;

import graql.lang.query.GraqlQuery;

import java.util.LinkedList;
import java.util.List;

public class MultiScaleResults {

    private GraqlQuery query;
    private List<QueryExecutionResults> resultsPerScale;

    public MultiScaleResults(GraqlQuery query) {
        this.query = query;
        this.resultsPerScale = new LinkedList<>();
    }

    public void addResult(QueryExecutionResults result) {
        resultsPerScale.add(result);
    }

    public GraqlQuery query() {
        return query;
    }

    public List<QueryExecutionResults> resultsPerScale() {
        return resultsPerScale;
    }

}
