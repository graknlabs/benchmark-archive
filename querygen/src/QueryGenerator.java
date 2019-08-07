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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

public class QueryGenerator {

    private static final Logger LOG = LoggerFactory.getLogger(QueryGenerator.class);

    private final Random random;
    private final GraknClient.Session session;

    public QueryGenerator(GraknClient.Session session) {
        this.session = session;
        this.random = new Random(0);
    }

    List<GraqlGet> generate(int numQueries) {
        List<GraqlGet> queries = new ArrayList<>(numQueries);
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


        // exponential/geometric probability distribution
        // 1/0.1 = 10 variables on average
        double variableGenerateProbability = 1.0;
        double variableGenerateProbabilityReduction = 0.1;
        double nextProbability = random.nextDouble();

        while (nextProbability < variableGenerateProbability && builder.haveUnvisitedVariable()) {

            // pick a new variable from the mapping we have not visited
            Variable var = builder.randomUnvisitedVariable(random);
            builder.visitVariable(var);
            Type varType = builder.getType(var);

            if (varType.isRelationType() && !varType.isImplicit()) {
                // we do not assign role players for implicit relations manually
                assignRolePlayers(tx, var, varType.asRelationType(), builder);
            }

            // assign attribute ownership
            assignAttributes(tx, var, varType, builder);

            variableGenerateProbability *= variableGenerateProbabilityReduction;
            nextProbability = random.nextDouble();
        }

        // TODO add a comparison between compatible attributes with a low probability

        return builder;
    }

    /**
     * We want to use this method to generate a variety of specificity of the roles played
     * So, for a given starting relation type, we first find all roles that can be played by it or its subtypes (down a schema branch)
     * <p>
     * Then for a random seed role out of this branch, we find all relations that can relate this role
     * Then proceed to find all unique roles that can be related by these relations (collecting all roles on this branch,
     * filtering out relations on another branch)
     * <p>
     * We then construct role players
     *
     * @param tx
     * @param relationVar
     * @param relationType
     * @param builder
     */
    private void assignRolePlayers(GraknClient.Transaction tx, Variable relationVar, RelationType relationType, QueryBuilder builder) {
        Set<RelationType> relationSubtypes = relationType.subs().filter(relation -> !relation.isImplicit()).collect(Collectors.toSet());

        // a super relation can be seen as playing its children's roles because only compatible relation subtypes are returned
        List<Role> allPossibleRoles = relationSubtypes.stream().flatMap(RelationType::roles).collect(Collectors.toList());

        // choose a starting role, which can only ever occur with some other roles, but not any role
        Role seedRole = allPossibleRoles.get(random.nextInt(allPossibleRoles.size()));

        // find all compatible roles so we generate combinations of roles that might actually occur in data, according to the schema
        List<Role> compatibleRoles = seedRole.relations().filter(relationSubtypes::contains).flatMap(RelationType::roles).distinct().collect(Collectors.toList());


        // choose a geometric series number of roles
        // with mean of 1/0.5 = 2 roles, min 1
        double roleGenerateProbability = 1.0;
        double roleGenerateProbabilityReduction = 0.5;
        double nextProbability = random.nextDouble();

        // do not reuse the same variables
        Set<Variable> usedRolePlayerVariables = new HashSet<>();
        // bias new roles away from being the same as old ones
        List<Role> usedRoleTypes = new ArrayList<>();

        while (nextProbability < roleGenerateProbability) {
            Role role = compatibleRoles.get(random.nextInt(compatibleRoles.size()));
            // re-pick once if the role has already been picked before
            if (usedRoleTypes.contains(role)) {
                role = compatibleRoles.get(random.nextInt(compatibleRoles.size()));
            }

            // find all types that can play the chosen role, or its subtypes
            List<Type> allowedRolePlayers = role.subs().flatMap(Role::players).collect(Collectors.toList());

            // choose a random type that can play this role
            if (allowedRolePlayers.size() > 0) {
                Type rolePlayerType = allowedRolePlayers.get(random.nextInt(allowedRolePlayers.size()));

                Variable rolePlayerVariable = chooseVariable(tx, builder, rolePlayerType, random, usedRolePlayerVariables);
                usedRolePlayerVariables.add(rolePlayerVariable);
                usedRoleTypes.add(role);

                builder.addMapping(rolePlayerVariable, rolePlayerType);
                builder.addRolePlayer(relationVar, rolePlayerVariable, role);

            } else {
                LOG.debug("Role: " + role.label().toString() + ", or its subtypes, have no possible players");
            }

            roleGenerateProbability *= roleGenerateProbabilityReduction;
            nextProbability = random.nextDouble();
        }
    }

    private void assignAttributes(GraknClient.Transaction tx, Variable var, Type varType, QueryBuilder builder) {

        // choose a geometric series number of attributes
        double attrGenerateProbability = 0.5;
        double attrGenerateProbabilityReduction = 0.3;
        double nextProbability = random.nextDouble();

        List<AttributeType> allowedAttributes = varType.attributes().collect(Collectors.toList());
        if (allowedAttributes.size() > 0) {
            while (nextProbability < attrGenerateProbability) {
                AttributeType ownableAttribute = allowedAttributes.get(random.nextInt(allowedAttributes.size()));

                // choose between walking up and walking down the type hierarchy
                Type attributeType = chooseSubOrSuperType(tx, ownableAttribute, random);

                // choose between reusing a variable for this type and making a new variable
                Variable attributeVariable = chooseVariable(tx, builder, attributeType, random);

                // write this new mapping to the query builder
                builder.addMapping(attributeVariable, attributeType);
                builder.addOwnership(var, attributeVariable);

                attrGenerateProbability *= attrGenerateProbabilityReduction;
                nextProbability = random.nextDouble();
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
     * Reuse a variable 75% of the time, if we can (to cause the query to look connect to itself), and if the variables are not in the exclude set
     * Else reserve a new variable
     *
     * @param tx
     * @param builder
     * @param type
     * @param random
     * @param exclude - variables that may not be chosen
     * @return
     */
    private Variable chooseVariable(GraknClient.Transaction tx, QueryBuilder builder, Type type, Random random, Set<Variable> exclude) {
        // choose an existing variable 75% of the time, assuming other criteria are met
        double probability = random.nextDouble();
        if (probability > 0.25) {
            if (builder.containsVariableWithType(type)) {
                // reuse a variable
                List<Variable> varsMappedToType = builder.variablesWithType(type);
                varsMappedToType.removeAll(exclude);

                if (!varsMappedToType.isEmpty()) {
                    int index = random.nextInt(varsMappedToType.size());
                    return varsMappedToType.get(index);
                }
            }
        }

        return builder.reserveNewVariable();
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
