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
import graql.lang.Graql;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
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
        // 1/0.05 = 20 variables on average
        double variableGenerateProbability = 1.0;
        double variableGenerateProbabilityReduction = 0.9;
        double nextProbability = random.nextDouble();

        while (nextProbability < variableGenerateProbability && builder.haveUnvisitedVariable()) {

            // pick a new variable from the mapping we have not visited
            Variable newVariable = builder.randomUnvisitedVariable(random);
            builder.visitVariable(newVariable);
            Type newVariableType = builder.getType(newVariable);

            // assign role players to a relation
            if (newVariableType.isRelationType() && !newVariableType.isImplicit()) {
                // we do not assign role players for implicit relations manually
                assignRolePlayers(tx, newVariable, newVariableType.asRelationType(), builder);
            }

            // assign new relations this concept plays a role in
            assignRelations(tx, newVariable, newVariableType, builder);

            // assign attribute ownership
            assignAttributes(tx, newVariable, newVariableType, builder);

            variableGenerateProbability *= variableGenerateProbabilityReduction;
            nextProbability = random.nextDouble();
        }


        // because it's so rare we generate two compatible attributes in a query already,
        // we can set the probability of a comparison quite high
        double comparisonGenerateProbability = 0.5;
        double comparisonGenerateProbabilityReduction = 0.5;
        nextProbability = random.nextDouble();

        while (nextProbability < comparisonGenerateProbability) {

            assignAttributeComparison(builder);

            comparisonGenerateProbability *= comparisonGenerateProbabilityReduction;
            nextProbability = random.nextDouble();
        }

        return builder;
    }

    /**
     * Assign new relations this concept plays a role in, if any. This method allows us to generate multi-hop queries
     * NOTE this method can also generate new variables
     *
     * @param tx
     * @param variable
     * @param variableType
     * @param builder
     */
    private void assignRelations(GraknClient.Transaction tx, Variable variable, Type variableType, QueryBuilder builder) {
        // obtain all roles this concept type can play
        List<Role> playableRoles = variableType.playing().filter(role -> !role.isImplicit()).collect(Collectors.toList());

        double relationGenerateProbability = 0.8;
        double relationGenerateProbabilityReduction = 0.5;
        double nextRandom = random.nextDouble();

        if (playableRoles.size() > 0) {
            while (nextRandom < relationGenerateProbability) {
                // choose a role to play
                Role variableRole = playableRoles.get(random.nextInt(playableRoles.size()));

                // find all relations that can relate this type of role
                List<RelationType> allowedRelations = variableRole.relations().collect(Collectors.toList());
                RelationType newRelationType = allowedRelations.get(random.nextInt(allowedRelations.size()));

                // generate a new relation (no reuse here) with this var as a role player
                Variable newRelationVariable = builder.reserveNewVariable();
                builder.addMapping(newRelationVariable, newRelationType);

                assignRolePlayers(tx, newRelationVariable, newRelationType, variable, variableRole, builder);

                relationGenerateProbability *= relationGenerateProbabilityReduction;
                nextRandom = random.nextDouble();
            }
        }
    }


    private void assignRolePlayers(GraknClient.Transaction tx, Variable relationVar, RelationType relationType, QueryBuilder builder) {
        assignRolePlayers(tx, relationVar, relationType, null, null, builder);
    }

    /**
     * We want to use this method to generate a variety of specificity of the roles played
     * So, for a given starting relation type, we first find all roles that can be played by it or its subtypes (down a schema branch)
     * <p>
     * Then for a seed role (which is optionally provided), we find all relations that can relate this role
     * Then proceed to find all unique roles that can be related by these relations (collecting all roles on this branch,
     * filtering out relations on another branch)
     * <p>
     * We then construct role players
     *
     * @param tx
     * @param relationVariable
     * @param relationType
     * @param builder
     */
    private void assignRolePlayers(GraknClient.Transaction tx, Variable relationVariable, RelationType relationType,
                                   Variable seedRoleVariable, Role seedRole, QueryBuilder builder) {

        Set<RelationType> relationSubtypes = relationType.subs().filter(relation -> !relation.isImplicit()).collect(Collectors.toSet());


        double roleGenerateProbability;

        if (seedRole == null) {
            // a super relation can be seen as playing its children's roles because only compatible relation subtypes are returned
            List<Role> allPossibleRoles = relationSubtypes.stream().flatMap(RelationType::roles).collect(Collectors.toList());
            // choose a starting role, which can only ever occur with some other roles, but not any role
            seedRole = allPossibleRoles.get(random.nextInt(allPossibleRoles.size()));

            // force generation of at least 1 role
            roleGenerateProbability = 1.0;
        } else {
            builder.addRolePlayer(relationVariable, seedRoleVariable, seedRole);
            // we already have 1 role player guaranteed, reduce probability of generating more
            roleGenerateProbability = 0.5;
        }


        // do not reuse the same variables among the role players
        Set<Variable> usedRolePlayerVariables = builder.rolePlayersInRelation(relationVariable);

        // bias new roles away from being the same as old ones
        List<Role> usedRoleTypes = builder.rolesPlayedInRelation(relationVariable);

        // find all compatible roles so we generate combinations of roles that might actually occur in data, according to the schema
        List<Role> compatibleRoles = seedRole.relations().filter(relationSubtypes::contains).flatMap(RelationType::roles).distinct().collect(Collectors.toList());

        double roleGenerateProbabilityReduction = 0.5;
        double nextProbability = random.nextDouble();

        while (nextProbability < roleGenerateProbability) {
            Role newRole = compatibleRoles.get(random.nextInt(compatibleRoles.size()));
            // re-pick once if the role has already been picked before, slightly weighting away from re-using role types
            if (usedRoleTypes.contains(newRole)) {
                newRole = compatibleRoles.get(random.nextInt(compatibleRoles.size()));
            }

            // find all types that can play the chosen role, or its subtypes
            List<Type> allowedRolePlayers = newRole.subs().flatMap(Role::players).collect(Collectors.toList());

            // choose a random type that can play this role
            if (allowedRolePlayers.size() > 0) {
                Type rolePlayerType = allowedRolePlayers.get(random.nextInt(allowedRolePlayers.size()));

                Variable rolePlayerVariable = chooseVariable(tx, builder, rolePlayerType, random, usedRolePlayerVariables);

                // sometimes we get back a rolePlayerVariable that is already a role player, even though we try to exclude it
                // this is because the chooseVariable() only
                usedRolePlayerVariables.add(rolePlayerVariable);
                usedRoleTypes.add(newRole);

                builder.addMapping(rolePlayerVariable, rolePlayerType);
                builder.addRolePlayer(relationVariable, rolePlayerVariable, newRole);
            } else {
                LOG.debug("Role: " + newRole.label().toString() + ", or its subtypes, have no possible players");
            }

            roleGenerateProbability *= roleGenerateProbabilityReduction;
            nextProbability = random.nextDouble();
        }
    }

    private void assignAttributes(GraknClient.Transaction tx, Variable ownerVariable, Type ownerType, QueryBuilder builder) {

        // choose a geometric series number of attributes
        double attrGenerateProbability = 0.5;
        double attrGenerateProbabilityReduction = 0.3;
        double nextProbability = random.nextDouble();

        List<AttributeType> allowedAttributes = ownerType.attributes().collect(Collectors.toList());
        if (allowedAttributes.size() > 0) {
            while (nextProbability < attrGenerateProbability) {
                AttributeType ownableAttribute = allowedAttributes.get(random.nextInt(allowedAttributes.size()));

                // choose between walking up and walking down the type hierarchy
                Type attributeType = chooseSubOrSuperType(tx, ownableAttribute, random);

                // choose between reusing a variable for this type and making a new variable
                Variable attributeVariable = chooseVariable(tx, builder, attributeType, random);

                // write this new mapping to the query builder
                builder.addMapping(attributeVariable, attributeType);
                builder.addOwnership(ownerVariable, attributeVariable);

                attrGenerateProbability *= attrGenerateProbabilityReduction;
                nextProbability = random.nextDouble();
            }
        }
    }


    private void assignAttributeComparison(QueryBuilder builder) {
        Map<AttributeType<?>, List<Variable>> attributeTypeVariablesMap = builder.attributeTypeVariables();

        // only choose from types that have two ore more vars
        List<AttributeType<?>> possibleAttributeTypes = attributeTypeVariablesMap.entrySet().stream()
                .filter(entry -> !entry.getKey().label().toString().equals("attribute")) // TODO better to check if equals meta type
                .filter(entry -> entry.getValue().size() > 1)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());

        if (possibleAttributeTypes.size() == 0) {
            return;
        }

        // choose the attribute type that will use a comparison
        int index = random.nextInt(possibleAttributeTypes.size());
        AttributeType<?> attributeType = possibleAttributeTypes.get(index);

        // choose two variables to compare
        List<Variable> comparableVariables = attributeTypeVariablesMap.get(attributeType);
        int comparingVarIndex = random.nextInt(comparableVariables.size());
        int otherVarIndex = random.nextInt(comparableVariables.size());
        while (otherVarIndex == comparingVarIndex) {
            otherVarIndex = random.nextInt(comparableVariables.size());
        }

        // choose a comparator
        List<Graql.Token.Comparator> comparators = new ArrayList<>();
        if (attributeType.dataType().dataClass().equals(String.class)) {
            comparators.add(Graql.Token.Comparator.CONTAINS);
        }
        comparators.add(Graql.Token.Comparator.GT);
        comparators.add(Graql.Token.Comparator.LT);
        comparators.add(Graql.Token.Comparator.EQV);
        comparators.add(Graql.Token.Comparator.NEQV);


        Graql.Token.Comparator comparator = comparators.get(random.nextInt(comparators.size()));
        builder.addComparison(comparableVariables.get(comparingVarIndex), comparableVariables.get(otherVarIndex), comparator);
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
