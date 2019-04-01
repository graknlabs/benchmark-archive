package grakn.benchmark.report.formatter;

import java.util.List;
import java.util.stream.Collectors;

class QueryDataEntry {
    public final int scale;
    public final int conceptsInvolved;
    public final int concurrency;
    public final int roundTrips;
    public final List<Long> msDuration;

    public QueryDataEntry(int scale, int conceptsInvolved, int concurrency, int roundTrips, List<Long> durations) {
        this.scale = scale;
        this.conceptsInvolved = conceptsInvolved;
        this.concurrency = concurrency;
        this.roundTrips = roundTrips;
        this.msDuration = durations;
    }

    public double meanTimePerConcept() {
        return durationMean()/conceptsInvolved;
    }

    public double meanThroughput() {
        return 1000.0/meanTimePerConcept();
    }

    public double stddevThroughput() {
        double mean = meanThroughput();
        List<Double> values = msDuration.stream().map(v -> 1000.0*conceptsInvolved/v).collect(Collectors.toList());
        return stddev(values, mean);
    }


    public double durationMean() {
        Long sum = msDuration.stream().reduce((a,b) -> a+b).get();
        return ((double) sum / msDuration.size());
    }

    private double stddev(List<Double> values, double mean) {
        double total = 0;
        for (Double v : values) {
            total += Math.pow(mean - v, 2);
        }
        double sampleVariance = total / (values.size() - 1);
        return Math.sqrt(sampleVariance);
    }
}
