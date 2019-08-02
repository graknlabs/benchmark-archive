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

package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import graql.lang.query.GraqlGet;

import java.util.ArrayList;
import java.util.List;

public class QueryGenerator {

    private String graknUri;
    private String keyspace;

    public QueryGenerator(String graknUri, String keyspace) {
        this.graknUri = graknUri;
        this.keyspace = keyspace;
    }

    List<GraqlGet> generate(int numQueries) {
        List<GraqlGet> queries = new ArrayList<>(numQueries);
        GraknClient client = new GraknClient(graknUri);
        GraknClient.Session session = client.session(keyspace);

        for (int i = 0; i < numQueries; i++) {
            queries.add(generateNewQuery(session));
        }

        return queries;
    }

    private GraqlGet generateNewQuery(GraknClient.Session session) {
        return null;
    }
}
