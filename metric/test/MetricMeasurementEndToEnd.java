package grakn.benchmark.metric.test;

import grakn.benchmark.metric.MetricMeasurement;
import org.junit.Test;
import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class MetricMeasurementEndToEnd {

    @Test
    public void standardGraphMetrics() {
        String standardGraphEdgeList = "/Users/joshua/Documents/benchmark/metric/test/binaryGraph.csv";
        double[] percentiles = new double[] {1.0, 30.0, 50.0, 70.0, 100.0};
        String[] args = new String[] {standardGraphEdgeList};
        double[] measurements = MetricMeasurement.mainImpl(args);

        double[] correctMetrics = new double[] {
                -0.43181818181818193,   // assortativity
                0.3,                    // transitivity
                1, 1, 1, 2, 3           //[0th, 20th, 50th, 70th, 100th] percentile degree distributions
        };

        assertThat(measurements, equalTo(correctMetrics));
    }

}
