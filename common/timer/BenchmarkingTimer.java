package grakn.benchmark.common.timer;

/**
 * A small timing class for tracking the proportion of time spent on:
 * insert data (growing the graph)
 * doing the profiling/recording of queries on the graphs
 * other (this includes time to create queries and processing in ignite)
 */
public class BenchmarkingTimer {

    private long generateAndProfileStartTime;

    private long totalDataGeneratorQueryTime = 0;
    private long startDataGeneratorQueryTime;

    private long totalProfilingTime = 0;
    private long startProfilingTime;

    public void startGenerateAndTrack() {
        generateAndProfileStartTime = now();
    }

    public void startDataGeneratorQuery() {
        startDataGeneratorQueryTime = now();
    }

    public void endDataGeneratorQuery() {
        totalDataGeneratorQueryTime += (now() - startDataGeneratorQueryTime);
    }

    public void startQueryTimeTracking() {
        startProfilingTime = now();
    }

    public void endQueryTimeTracking() {
        totalProfilingTime += (now() - startProfilingTime);
    }

    public void printTimings() {
        long totalElapsedMillis = now() - generateAndProfileStartTime;
        double fractionElapsedDataInsert = (double) totalDataGeneratorQueryTime / totalElapsedMillis;
        double fractionProfilingTime = (double) totalProfilingTime/ totalElapsedMillis;
        double fractionRemaining = (double) (totalElapsedMillis - totalProfilingTime - totalDataGeneratorQueryTime)/totalElapsedMillis;

        System.out.printf("Total elapsed time: %d ms, [%.2f data insert, %.2f record/profile, %.2f other (query generation etc.)]\n",
                totalElapsedMillis, fractionElapsedDataInsert, fractionProfilingTime, fractionRemaining);
    }


    private long now() {
        return System.currentTimeMillis();
    }
}
