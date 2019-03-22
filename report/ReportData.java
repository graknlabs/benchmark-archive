package grakn.benchmark.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graql.lang.query.GraqlQuery;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Generated report data that is serialized to JSON
 */
class ReportData {

    // each MultiScaleQueryExecutionResults represents ONE query executed across different scales
    private Map<String, List<MultiScaleResults>> data;
    // use a lookup to find out which queries are already represented in the report
    private Map<GraqlQuery, MultiScaleResults> multiScaleQueryExecutionResultsLookup;

    public ReportData() {
        data = new HashMap<>();
        multiScaleQueryExecutionResultsLookup = new HashMap<>();
    }

    public void recordQueryTimes(String type, GraqlQuery query, QueryExecutionResults queryData) {
        if (!multiScaleQueryExecutionResultsLookup.containsKey(query)) {
            MultiScaleResults results = new MultiScaleResults(query);
            multiScaleQueryExecutionResultsLookup.put(query, results);
            data.putIfAbsent(type, new LinkedList<>());
            data.get(type).add(results);
        }

        // add this specific queryData to the MultiScaleQueryExecutionResults
        multiScaleQueryExecutionResultsLookup.get(query).addResult(queryData);
    }

    public String asJson() {
        // register a serialiser for the QueryExecutionResults
        QueryExecutionResultsSerializer serializer = new QueryExecutionResultsSerializer(QueryExecutionResults.class);
        MultiScaleResultsSerializer containerSerialiser = new MultiScaleResultsSerializer(MultiScaleResults.class);
        SimpleModule module = new SimpleModule("QueryExecutionResultsSerializer");
        module.addSerializer(serializer);
        module.addSerializer(containerSerialiser);

        // register a serialiser for the MultiScaleQueryExecutionResults
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            return mapper.writeValueAsString(data);
        } catch (JsonProcessingException e) {
            // wrap in a custom runtime exception
            throw new ReportGeneratorException("Error serializing data to JSON", e);
        }
    }
}
