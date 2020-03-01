/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

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
