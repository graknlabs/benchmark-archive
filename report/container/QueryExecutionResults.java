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

package grakn.benchmark.report.container;

import java.util.LinkedList;
import java.util.List;

/**
 * Container for data from the execution of one query repeatedly at a single scale
 * May be used to record the times for a single query from one concurrent client or
 * aggregated from multiple concurrent clients
 */
public class QueryExecutionResults {
    private List<Long> queryExecutionTimes;
    private Integer conceptsInvolved = null;
    private String queryType = null;
    private Integer roundTrips = null;
    private Integer scale = null;

    public QueryExecutionResults(String queryType, int conceptsInvolved, int roundTrips) {
        queryExecutionTimes = new LinkedList<>();
        this.conceptsInvolved = conceptsInvolved;
        this.queryType = queryType;
        this.roundTrips = roundTrips;
    }

    public void addExecutionTime(Long milliseconds) {
        queryExecutionTimes.add(milliseconds);
    }

    public void addExecutionTimes(List<Long> milliseconds) {
        queryExecutionTimes.addAll(milliseconds);
    }

    // setScale() is called once externally, after data has been recorded
    public void setScale(Integer scale) {
        this.scale = scale;
    }

    public void setRoundTrips(Integer roundTrips) {
        this.roundTrips = roundTrips;
    }

    public void setConcepts(Integer conceptsInvolved) {
        this.conceptsInvolved = conceptsInvolved;
    }

    public List<Long> times() {
        return queryExecutionTimes;
    }

    public Integer concepts() {
        return conceptsInvolved;
    }

    public String queryType() {
        return queryType;
    }

    public Integer roundTrips() {
        return roundTrips;
    }

    public Integer scale() { return scale; }
}
