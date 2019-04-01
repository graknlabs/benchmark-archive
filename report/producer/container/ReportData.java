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

package grakn.benchmark.report.producer.container;

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
public class ReportData {

    // each MultiScaleResults represents ONE query executed across different scales
    private Map<String, List<MultiScaleResults>> queryExecutionData;
    // use a lookup to find out which queries are already represented in the report
    private Map<GraqlQuery, MultiScaleResults> multiScaleQueryExecutionResultsLookup;

    // metadata
    private int concurrentClients;
    private String configName;
    private String description;
    private String dataGenerator;


    public ReportData() {
        queryExecutionData = new HashMap<>();
        multiScaleQueryExecutionResultsLookup = new HashMap<>();
    }

    public void addMetadata(String configName, int concurrentClients, String description, String dataGenerator) {
        this.configName = configName;
        this.concurrentClients = concurrentClients;
        this.description = description;
        this.dataGenerator = dataGenerator;
    }

    public void recordTimesAtScale(int scale, List<Map<GraqlQuery, QueryExecutionResults>> results) {

        // first aggregate the List of results from different clients
        // into one result.

        // create empty execution result holder for each query
        Map<GraqlQuery, QueryExecutionResults> mergedResults = new HashMap<>();
        for (GraqlQuery query : results.get(0).keySet()) {
            QueryExecutionResults data = results.get(0).get(query);
            String queryType = data.queryType();
            QueryExecutionResults queryExecutionResults = new QueryExecutionResults(queryType, 0, 0);
            queryExecutionResults.setScale(scale);
            mergedResults.put(query, queryExecutionResults);
        }

        for (int i = 0; i < results.size(); i++) {
            Map<GraqlQuery, QueryExecutionResults> singleClientData = results.get(i);
            for (GraqlQuery query : singleClientData.keySet()) {

                QueryExecutionResults singleQueryData = singleClientData.get(query);
                QueryExecutionResults mergedQueryData = mergedResults.get(query);

                // append the time taken by the next client's executions
                mergedQueryData.addExecutionTimes(singleQueryData.times());
                // add up the round trips taken
                mergedQueryData.setRoundTrips(mergedQueryData.roundTrips() + singleQueryData.roundTrips());
                // add up the total concepts involved
                mergedQueryData.setConcepts(mergedQueryData.concepts() + singleQueryData.concepts());
            }
        }

        // write the aggregated result into the main map
        mergedResults.forEach(this::recordQueryTimes);
    }

    private void recordQueryTimes(GraqlQuery query, QueryExecutionResults queryData) {
        String queryType = queryData.queryType();
        if (!multiScaleQueryExecutionResultsLookup.containsKey(query)) {
            MultiScaleResults results = new MultiScaleResults(query);
            multiScaleQueryExecutionResultsLookup.put(query, results);
            // make an entry for the query type if it doesn't exist already
            queryExecutionData.putIfAbsent(queryType, new LinkedList<>());
            queryExecutionData.get(queryType).add(results);
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
            throw new RuntimeException("Error serializing data to JSON", e);
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

    public String dataGenerator() {
        return dataGenerator;
    }

    public Map<String, List<MultiScaleResults>> queryExecutionData() {
        return queryExecutionData;
    }
}
