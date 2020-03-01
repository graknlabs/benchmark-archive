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

public class QueryDataFormatter {

    /**
     * Convert a list of entries into a six-column table showing
     * mean time per concept & throughput versus scale
     */
    public static String formatScaleTable(List<QueryDataEntry> entries) {
        List<QueryDataEntry> sortedEntries = entries.stream().sorted(Comparator.comparingInt(e -> e.scale)).collect(Collectors.toList());

        List<String> table = new LinkedList<>();
        table.add(String.format("  %-12s %16s %32s %16s%n", "Concepts", "Concepts Queried", "Mean Throughput (/s)", "Stddev"));
        String dataFormat = "  %-12d %16d %32f %16f";

        for (QueryDataEntry entry : sortedEntries) {
            double meanTimePerConcept = entry.meanTimePerConcept();
            double meanThroughput = entry.meanThroughput();
            double stddevThroughput = entry.stddevThroughput();
            table.add(String.format(dataFormat, entry.scale, entry.conceptsInvolved, meanThroughput, stddevThroughput));
        }
        return String.join("\n", table);
    }

    /**
     * Convert a list of entries into a six-column table showing
     * mean time per concept & throughput versus concurrency
     */
    public static String formatConcurrencyTable(List<QueryDataEntry> entries) {
        List<QueryDataEntry> sortedEntries = entries.stream().sorted(Comparator.comparingInt(e -> e.concurrency)).collect(Collectors.toList());

        List<String> table = new LinkedList<>();
        table.add(String.format("  %-12s %16s %32s %16s%n", "Concurrency", "Concepts Queried","Mean Throughput (/s)", "Stddev"));
        String dataFormat = "  %-12d %16d %32f %16f";

        for (QueryDataEntry entry : sortedEntries) {
            double meanThroughput = entry.meanThroughput();
            double stddevThroughput = entry.stddevThroughput();
            table.add(String.format(dataFormat, entry.concurrency, entry.conceptsInvolved, meanThroughput, stddevThroughput));
        }
        return String.join("\n", table);
    }
}

