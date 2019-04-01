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

package grakn.benchmark.report.producer.container;

import graql.lang.query.GraqlQuery;

import java.util.LinkedList;
import java.util.List;

public class MultiScaleResults {

    private GraqlQuery query;
    private List<QueryExecutionResults> resultsPerScale;

    public MultiScaleResults(GraqlQuery query) {
        this.query = query;
        this.resultsPerScale = new LinkedList<>();
    }

    public void addResult(QueryExecutionResults result) {
        resultsPerScale.add(result);
    }

    public GraqlQuery query() {
        return query;
    }

    public List<QueryExecutionResults> resultsPerScale() {
        return resultsPerScale;
    }

}
