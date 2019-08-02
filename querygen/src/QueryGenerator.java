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
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.Type;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Variable;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class QueryGenerator {

    private final Random random;
    private String graknUri;
    private String keyspace;

    public QueryGenerator(String graknUri, String keyspace) {
        this.graknUri = graknUri;
        this.keyspace = keyspace;
        this.random = new Random(0);
    }

    List<String> generate(int numQueries) {
        List<String> queries = new ArrayList<>(numQueries);
        GraknClient client = new GraknClient(graknUri);
        GraknClient.Session session = client.session(keyspace);

        for (int i = 0; i < numQueries; i++) {
            queries.add(generateNewQuery(session).toString());
        }

        return queries;
    }

    private QueryBuilder generateNewQuery(GraknClient.Session session) {
        GraknClient.Transaction tx = session.transaction().write();
        Type rootThing = tx.getMetaConcept();

        QueryBuilder builder = new QueryBuilder();

        Type startingType = SchemaWalker.walkSub(rootThing, random);
        Variable startingVariable = builder.reserveNewVariable();
        builder.addMapping(startingVariable, startingType);

        // TODO determine how long this query should be

        for (int i = 0; i < 5; i++) {
            // pick a new variable from the mapping we have not visited
            Variable var = builder.randomUnvisitedVariable(random);
            builder.visitVariable(var);
            Type varType = builder.getType(var);

            if (varType.isRelationType()) {
                // assign role players
            }

            // assign attribute ownership
            assignAttributes(var, varType, builder);
        }

        // TODO add a comparison between compatible attributes with a low probability

        // convert QueryBuilder into graql query

        return null;
    }

    private void assignAttributes(Variable var, Type varType, QueryBuilder builder) {
        // TODO determine how many attributes should be had
        int maxAttrs = 3;
        int attrs = random.nextInt(maxAttrs);

        List<AttributeType> allowedAttributes = varType.attributes().collect(Collectors.toList());
        for (int i = 0; i < attrs; i++) {
            AttributeType attrToOwn = allowedAttributes.get(random.nextInt(allowedAttributes.size()));
            // choose between reusing a variable for this type
            // and a new variable
        }
    }

}
