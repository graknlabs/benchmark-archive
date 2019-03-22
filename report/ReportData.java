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
    private Map<String, List<MultiScaleResults>> queryExecutionData;
    // use a lookup to find out which queries are already represented in the report
    private Map<GraqlQuery, MultiScaleResults> multiScaleQueryExecutionResultsLookup;

    // metadata
    private int concurrentClients;
    private String configName;
    private String description;


    public ReportData(String configName, int concurrentClients, String description) {
        queryExecutionData = new HashMap<>();
        multiScaleQueryExecutionResultsLookup = new HashMap<>();
        this.configName = configName;
        this.concurrentClients = concurrentClients;
        this.description = description;
    }

    public void recordQueryTimes(String type, GraqlQuery query, QueryExecutionResults queryData) {
        if (!multiScaleQueryExecutionResultsLookup.containsKey(query)) {
            MultiScaleResults results = new MultiScaleResults(query);
            multiScaleQueryExecutionResultsLookup.put(query, results);
            queryExecutionData.putIfAbsent(type, new LinkedList<>());
            queryExecutionData.get(type).add(results);
        }

        // add this specific queryData to the MultiScaleQueryExecutionResults
        multiScaleQueryExecutionResultsLookup.get(query).addResult(queryData);
    }

    public String asJson() {
        // register a serialiser for the QueryExecutionResults
        QueryExecutionResultsSerializer serializer = new QueryExecutionResultsSerializer(QueryExecutionResults.class);
        MultiScaleResultsSerializer containerSerialiser = new MultiScaleResultsSerializer(MultiScaleResults.class);
        ReportDataSerializer reportDataSerializer = new ReportDataSerializer(ReportData.class);
        SimpleModule module = new SimpleModule("serializers");
        module.addSerializer(serializer);
        module.addSerializer(containerSerialiser);
        module.addSerializer(reportDataSerializer);

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(module);
        mapper.enable(SerializationFeature.INDENT_OUTPUT); // enable indentation for readability

        try {
            return mapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            // wrap in a custom runtime exception
            throw new ReportGeneratorException("Error serializing data to JSON", e);
        }
    }

    public int concurrentClients() {
        return concurrentClients;
    }

    public String configName() {
        return configName;
    }

    public String description() {
        return description;
    }

    public Map<String, List<MultiScaleResults>> queryExecutionData() {
        return queryExecutionData;
    }
}
