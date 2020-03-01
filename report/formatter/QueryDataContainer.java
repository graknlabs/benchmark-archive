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

        long numConcurrency = numConcurrency();

        if (numConcurrency >= 2) {
            List<QueryDataEntry> lowThreadScalability = entriesForConcurrency(minConcurrency());
            representation += "\n\n  #### " + minConcurrency() + " Thread Scalability \n";
            representation += QueryDataFormatter.formatScaleTable(lowThreadScalability);
        }

        if (numConcurrency >= 1) {
            List<QueryDataEntry> highThreadScalability = entriesForConcurrency(maxConcurrency());
            representation += "\n\n  #### " + maxConcurrency() + " Thread Scalability \n";
            representation += QueryDataFormatter.formatScaleTable(highThreadScalability);

            List<QueryDataEntry> highScaleParallelisability = entriesForScale(maxScale());
            representation += "\n\n  #### Parallelisability at " + maxScale() + " Concepts \n";
            representation += QueryDataFormatter.formatConcurrencyTable(highScaleParallelisability);
        }


        return representation;
    }


    public void add(QueryDataEntry dataEntry) {
        unsortedData.add(dataEntry);
    }


    private List<QueryDataEntry> entriesForConcurrency(int concurrency) {
        return unsortedData.stream()
                .filter(entry -> entry.concurrency == concurrency)
                .collect(Collectors.toList());
    }

    private List<QueryDataEntry> entriesForScale(int scale) {
        return unsortedData.stream()
                .filter(entry -> entry.scale == scale)
                .collect(Collectors.toList());
    }

    private int maxScale() {
        return unsortedData.stream().map(entry -> entry.scale).max(Comparator.naturalOrder()).get();
    }

    private long numConcurrency() {
        return unsortedData.stream().map(entry -> entry.concurrency).distinct().count();
    }

    private int minConcurrency() {
        return unsortedData.stream().map(entry -> entry.concurrency).min(Comparator.naturalOrder()).get();
    }

    private int maxConcurrency() {
        return unsortedData.stream().map(entry -> entry.concurrency).max(Comparator.naturalOrder()).get();
    }
}

