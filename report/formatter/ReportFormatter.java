package grakn.benchmark.report.formatter;

import mjson.Json;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

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
        Option reportJsonOption = Option.builder("r")
                .longOpt("rawReport")
                .hasArg(true)
                .desc("Raw report JSON file")
                .required(true)
                .type(String.class)
                .build();
        Option destinationDirectory = Option.builder("d")
                .longOpt("destination")
                .hasArg(true)
                .desc("Target filesystem location to write the report to")
                .required(true)
                .type(String.class)
                .build();

        Options options = new Options();
        options.addOption(reportJsonOption);;
        options.addOption(destinationDirectory);
        CommandLineParser parser = new DefaultParser();
        CommandLine arguments;
        try {
            arguments = parser.parse(options, args);
        } catch (ParseException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        String rawReportFile = arguments.getOptionValue("rawReport");
        Path path = Paths.get(rawReportFile);
        List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
        String blob = String.join("\n", lines);

        Json jsonData = Json.read(blob);

        Map<String, QueryDataContainer> dataByQuery = toMap(jsonData);

        dataByQuery.values().forEach(System.out::println);

        Path reportFile = Paths.get(arguments.getOptionValue("destination"), "formatted_report.txt");
        List<String> reportEntries = dataByQuery.keySet().stream()
                .sorted()
                .map(key -> dataByQuery.get(key).toString())
                .collect(Collectors.toList());
        Files.write(reportFile, reportEntries, Charset.defaultCharset());
    }

    private static Map<String, QueryDataContainer> toMap(Json data) {
        Map<String, QueryDataContainer> queryDataMap = new HashMap<>();

        // iterate over configuration executions
        for (Json configExecution : data.asJsonList()) {
            extractQueryData(configExecution, queryDataMap);
        }
        return queryDataMap;
    }

    private static void extractQueryData(Json configExecution, Map<String, QueryDataContainer> collectedQueryData) {
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

                    collectedQueryData.putIfAbsent(query, new QueryDataContainer(query, configName, description));
                    collectedQueryData.get(query).add(dataEntry);
                }
            }
        }
    }
}
