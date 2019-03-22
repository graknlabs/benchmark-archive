package grakn.benchmark.report;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.module.SimpleModule;
import graql.lang.query.GraqlQuery;

import java.util.HashMap;
import java.util.Map;

/**
 * Generated report data that is serialized to JSON
 */
class ReportData {

    private Map<String, Map<GraqlQuery, Map<Integer, QueryExecutionResults>>> data;

    public ReportData() {
        data = new HashMap<>();
    }

    public void record(String type, GraqlQuery query, Integer scale, QueryExecutionResults queryData) {
        data.putIfAbsent(type, new HashMap<>());
        data.get(type).putIfAbsent(query, new HashMap<>());
        data.get(type).get(query).put(scale, queryData);
    }

    public String asJson() {
        // register a serialiser for the QueryExecutionResults
        QueryExecutionResultsSerializer serializer = new QueryExecutionResultsSerializer(QueryExecutionResults.class);
        SimpleModule module = new SimpleModule("QueryExecutionResultsSerializer");
        module.addSerializer(serializer);

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
