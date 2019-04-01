package grakn.benchmark.report.formatter;

import mjson.Json;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class ReportFormatter {

    public static void main(String[] args) throws IOException {
        String rawReportFile = args[0];
        Path path = Paths.get(rawReportFile);
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        String blob = String.join("\n", lines);

        Json jsonData = Json.read(blob);

        Map<String, QueryData> dataByQuery = toMap(jsonData);

        dataByQuery.values().forEach(System.out::println);
    }

    private static Map<String, QueryData> toMap(Json data) {
        Map<String, QueryData> queryDataMap = new HashMap<>();

        // iterate over configuration executions
        for (Json configExecution : data.asJsonList()) {
            extractQueryData(configExecution, queryDataMap);
        }
        return queryDataMap;
    }

    private static void extractQueryData(Json configExecution, Map<String, QueryData> collectedQueryData) {
        Json metadata = configExecution.at("metadata");
        String configName = metadata.at("configName").asString();
        String description = metadata.at("configDescription").asString();
        Integer concurrency = metadata.at("concurrentClients").asInteger();

        Json queryExecutionData = configExecution.at("queryExecutionData");
        Set<String> queryTypes = queryExecutionData.asMap().keySet();
        for (String queryType : queryTypes) {
            List<Json> dataPerQueryType = queryExecutionData.at(queryType).asJsonList();
            for (Json queryData : dataPerQueryType) {
                String query = queryData.at("query").asString();

                List<Json> dataPerScale = queryData.at("dataPerScale").asJsonList();
                for (Json queryExecutionAtScale : dataPerScale) {
                    int conceptsInvolved = queryExecutionAtScale.at("conceptsInvolved").asInteger();
                    int roundTrips = queryExecutionAtScale.at("roundTrips").asInteger();
                    int scale = queryExecutionAtScale.at("scale").asInteger();
                    List<Long> durations = queryExecutionAtScale.at("duration").asList().stream().map(v -> (Long) v).collect(Collectors.toList());

                    // store the data
                    QueryDataEntry dataEntry = new QueryDataEntry(scale, conceptsInvolved, concurrency, roundTrips, durations);

                    collectedQueryData.putIfAbsent(query, new QueryData(query, configName, description));
                    collectedQueryData.get(query).add(dataEntry);
                }
            }
        }
    }
}
