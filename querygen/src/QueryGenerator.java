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
import grakn.core.concept.type.RelationType;
import grakn.core.concept.type.Role;
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

    List<GraqlGet> generate(int numQueries) {
        List<GraqlGet> queries = new ArrayList<>(numQueries);
        GraknClient client = new GraknClient(graknUri);
        GraknClient.Session session = client.session(keyspace);

        for (int i = 0; i < numQueries; i++) {
            try (GraknClient.Transaction tx = session.transaction().write()) {
                QueryBuilder builder = generateNewQuery(tx);
                queries.add(builder.build(tx, random));
            }
        }

        return queries;
    }

    QueryBuilder generateNewQuery(GraknClient.Transaction tx) {
        Type rootThing = tx.getMetaConcept();

        QueryBuilder builder = new QueryBuilder();

        Type startingType = SchemaWalker.walkSubs(rootThing, random);
        Variable startingVariable = builder.reserveNewVariable();
        builder.addMapping(startingVariable, startingType);

        // TODO determine how long this query should be

        int variablesToProduce = 5;
        int variablesProduced = 0;

        while (variablesProduced < variablesToProduce && builder.haveUnvisitedVariable()) {

            // pick a new variable from the mapping we have not visited
            Variable var = builder.randomUnvisitedVariable(random);
            builder.visitVariable(var);
            Type varType = builder.getType(var);

            if (varType.isRelationType()) {
                assignRolePlayers(tx, var, varType.asRelationType(), builder);
            }

            // assign attribute ownership
            assignAttributes(tx, var, varType, builder);

            variablesProduced++;
        }

        // TODO add a comparison between compatible attributes with a low probability

        return builder;
    }

    private void assignRolePlayers(GraknClient.Transaction tx, Variable relationVar, RelationType relationType, QueryBuilder builder) {
        List<Role> allowedRoles = relationType.roles().collect(Collectors.toList());

        // TODO choose some subset of roles to populate, with repetition
        int maxRoles = 5;
        int roles = 1 + random.nextInt(maxRoles-1); // must have at least 1 role player

        for (int i = 0; i < roles; i++) {
            Role role = allowedRoles.get(random.nextInt(allowedRoles.size()));

            List<Type> allowedRolePlayers = role.players().collect(Collectors.toList());

            // choose a random type that can play this role
            Type rolePlayerType = allowedRolePlayers.get(random.nextInt(allowedRolePlayers.size()));

            // TODO do we need to do another walkSubs(rolePlayerType) to choose which some random subtype?
            // TODO this may not be needed if all the subtypes are already included in the list above

            Variable rolePlayerVariable = chooseVariable(tx, builder, rolePlayerType, random);

            builder.addMapping(rolePlayerVariable, rolePlayerType);
            builder.addRolePlayer(relationVar, rolePlayerVariable, role);
        }
    }

    private void assignAttributes(GraknClient.Transaction tx, Variable var, Type varType, QueryBuilder builder) {
        // TODO determine how many attributes should be had
        int maxAttrs = 3;
        int attrs = random.nextInt(maxAttrs);

        List<AttributeType> allowedAttributes = varType.attributes().collect(Collectors.toList());
        if (allowedAttributes.size() > 0) {
            for (int i = 0; i < attrs; i++) {
                AttributeType ownableAttribute = allowedAttributes.get(random.nextInt(allowedAttributes.size()));

                // choose between walking up and walking down the type hierarchy
                Type attributeType = chooseSubOrSuperType(tx, ownableAttribute, random);

                // choose between reusing a variable for this type and making a new variable
                Variable attributeVariable = chooseVariable(tx, builder, attributeType, random);

                // write this new mapping to the query builder
                builder.addMapping(attributeVariable, attributeType);
                builder.addOwnership(var, attributeVariable);
            }
        }
    }

    /**
     * Reuse a variable 75% of the time, if we can (to cause the query to look connect to itself)
     * Else reserve a new variable
     *
     * @param tx
     * @param builder
     * @param type
     * @param random
     * @return
     */
    private Variable chooseVariable(GraknClient.Transaction tx, QueryBuilder builder, Type type, Random random) {
        // choose an existing variable 75% of the time
        double probability = random.nextDouble();
        if (probability > 0.25 && builder.containsVariableWithType(type)) {
            // reuse a variable
            List<Variable> varsMappedToType = builder.variablesWithType(type);
            int index = random.nextInt(varsMappedToType.size());
            return varsMappedToType.get(index);
        } else {
            return builder.reserveNewVariable();
        }
    }

    /**
     * Choose a sub (50%) or super (50%) direction, and then choose a random sub or super type of the given type
     *
     * @param tx
     * @param type
     * @param random
     * @return
     */
    private Type chooseSubOrSuperType(GraknClient.Transaction tx, Type type, Random random) {
        boolean walkSubs = random.nextBoolean();
        if (walkSubs) {
            return SchemaWalker.walkSubs(type, random);
        } else {
            return SchemaWalker.walkSupsNoMeta(tx, type, random);
        }
    }

}
