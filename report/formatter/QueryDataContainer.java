package grakn.benchmark.report.formatter;

import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

class QueryDataContainer {
    private String query;
    private String configName;
    private String configDescription;

    private List<QueryDataEntry> unsortedData;

    public QueryDataContainer(String query, String configName, String configDescription) {
        this.query = query;
        this.configName = configName;
        this.configDescription = configDescription;
        unsortedData = new LinkedList<>();
    }

    @Override
    public String toString() {
        String representation = "\n\n ## Query Report";
        representation += "\n  Query: `" + query.replace("\n", " ") + "`";
        representation += "\n  Configuration name: " + configName;
        representation += "\n  Configuration description: " + configDescription;

        List<QueryDataEntry> lowThreadScalability = entriesForConcurrency(minConcurrency());
        representation += "\n\n  #### Single Thread Scalability \n";
        representation += QueryDataFormatter.formatScaleTable(lowThreadScalability);

        List<QueryDataEntry> highThreadScalability = entriesForConcurrency(maxConcurrency());
        representation += "\n\n  #### " + maxConcurrency() + " Thread Scalability \n";
        representation += QueryDataFormatter.formatScaleTable(highThreadScalability);

        List<QueryDataEntry> highScaleParallelisability = entriesForScale(maxScale());
        representation += "\n\n  #### Parallelisability at " + maxScale() + " Concepts \n";
        representation += QueryDataFormatter.formatConcurrencyTable(highScaleParallelisability);

        return representation;
    }


    public void add(QueryDataEntry dataEntry) {
        unsortedData.add(dataEntry);
    }


    public List<QueryDataEntry> entriesForConcurrency(int concurrency) {
        return unsortedData.stream()
                .filter(entry -> entry.concurrency == concurrency)
                .collect(Collectors.toList());
    }

    public List<QueryDataEntry> entriesForScale(int scale) {
        return unsortedData.stream()
                .filter(entry -> entry.scale == scale)
                .collect(Collectors.toList());
    }

    public int maxScale() {
        return unsortedData.stream().map(entry -> entry.scale).max(Comparator.naturalOrder()).get();
    }

    public int minConcurrency() {
        return unsortedData.stream().map(entry -> entry.concurrency).min(Comparator.naturalOrder()).get();
    }

    public int maxConcurrency() {
        return unsortedData.stream().map(entry -> entry.concurrency).max(Comparator.naturalOrder()).get();
    }
}

