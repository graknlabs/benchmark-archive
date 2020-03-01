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

package grakn.benchmark.querygen;

import grakn.benchmark.querygen.util.Pair;
import grakn.client.GraknClient;
import grakn.client.concept.AttributeType;
import grakn.client.concept.Role;
import grakn.client.concept.Type;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.property.ValueProperty;
import graql.lang.query.GraqlGet;
import graql.lang.statement.StatementInstance;
import graql.lang.statement.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Container for all the data that is required to build a GraqlGet query out of mappings from variables to variables
 * and variables to types
 *
 * The entire lifetime of the QueryBuilder needs to be tied to a single Transaction, until after `.build()` is called
 * This is because it contains live `Concepts` bound to this transaction that are unusable if it is closed
 */
public class QueryBuilder {

    // Actual type to query a variable against
    final Map<Variable, Type> variableTypeMap;
    // map for which variables are owned by which other variables
    final Map<Variable, List<Variable>> attributeOwnership;
    // For each relation for which we specify role players, record variable and role type
    final Map<Variable, List<Pair<Variable, Role>>> relationRolePlayers;
    // map between variables and the comparator used, storing attribute VALUE comparisons only
    final Map<Variable, Map<Variable, Graql.Token.Comparator>> attributeComparisons;

    // variables that we haven't processed yet when expanding a query, so we don't grow from the same variable twice
    // although doing so wouldn't be detrimental really
    final List<Variable> unvisitedVariables;

    int nextVar = 0;

    QueryBuilder() {
        this.variableTypeMap = new HashMap<>();
        this.attributeOwnership = new HashMap<>();
        this.relationRolePlayers = new HashMap<>();
        this.attributeComparisons = new HashMap<>();
        this.unvisitedVariables = new ArrayList<>();
    }

    Variable reserveNewVariable() {
        Variable var = new Variable("v" + nextVar);
        nextVar++;
        return var;
    }


    void addMapping(Variable variable, Type variableType) {
        variableTypeMap.put(variable, variableType);
        unvisitedVariables.add(variable);
    }

    void addOwnership(Variable owner, Variable owned) {
        attributeOwnership.putIfAbsent(owner, new ArrayList<>());
        attributeOwnership.get(owner).add(owned);
    }

    void addRolePlayer(Variable relationVar, Variable rolePlayerVariable, Role role) {
        relationRolePlayers.putIfAbsent(relationVar, new ArrayList<>());
        relationRolePlayers.get(relationVar).add(new Pair<>(rolePlayerVariable, role));
    }

    void addComparison(Variable leftVariable, Variable rightVariable, Graql.Token.Comparator comparator) {
        attributeComparisons.putIfAbsent(leftVariable, new HashMap<>());
        Map<Variable, Graql.Token.Comparator> comparisons = attributeComparisons.get(leftVariable);
        comparisons.put(rightVariable, comparator);
    }

    boolean containsVariableWithType(Type type) {
        return variableTypeMap.values().contains(type);
    }

    public Type getType(Variable var) {
        return variableTypeMap.get(var);
    }

    boolean haveUnvisitedVariable() {
        return !unvisitedVariables.isEmpty();
    }

    Variable randomUnvisitedVariable(Random random) {
        int index = random.nextInt(unvisitedVariables.size());
        return unvisitedVariables.get(index);
    }

    void visitVariable(Variable var) {
        unvisitedVariables.remove(var);
    }


    List<Variable> variablesWithType(Type type) {
        return variableTypeMap.entrySet().stream()
                .filter(entry -> entry.getValue().equals(type))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    /**
     * @return mapping from attribute type to all the variables corresponding to that attribute type
     */
    Map<AttributeType<?>, List<Variable>> attributeTypeVariables() {
        Map<AttributeType<?>, List<Variable>> attrTypeMapping = new HashMap<>();
        for (Map.Entry<Variable, Type> entry : variableTypeMap.entrySet()) {
            if (entry.getValue().isAttributeType()) {
                AttributeType<?> attributeType = entry.getValue().asAttributeType();
                Variable var = entry.getKey();
                attrTypeMapping.putIfAbsent(attributeType, new ArrayList<>());
                attrTypeMapping.get(attributeType).add(var);
            }
        }

        return attrTypeMapping;
    }

    Set<Variable> rolePlayersInRelation(Variable relationVariable) {
        List<Pair<Variable, Role>> pairs = relationRolePlayers.get(relationVariable);
        if (pairs == null) {
            return new HashSet<>();
        } else {
            return pairs.stream().map(Pair::getFirst).collect(Collectors.toSet());
        }
    }

    List<Role> rolesPlayedInRelation(Variable relationVariable) {
        List<Pair<Variable, Role>> pairs = relationRolePlayers.get(relationVariable);
        if (pairs == null) {
            return new ArrayList<>();
        } else {
            return pairs.stream().map(Pair::getSecond).collect(Collectors.toList());
        }
    }


    /**
    * @return all variables in this query
    */
    Set<Variable> allVariables() {
        return variableTypeMap.keySet();
    }


    /**
     * @return Set of variables that are representing relations
     */
    Set<Variable> relationVariables() {
        return variableTypeMap.entrySet().stream().filter(entry -> entry.getValue().isRelationType()).map(Map.Entry::getKey).collect(Collectors.toSet());
    }

    /**
     * @return List of variables representing attributes that are owned by a variable
     */
    List<Variable> attributesOwned(Variable ownerVar) {
        return attributeOwnership.get(ownerVar);
    }

    long numAttributeComparisons() {
        return attributeComparisons.values().stream().flatMap(map -> map.values().stream()).count();
    }

    /**
     * Build a GraqlGet query out of all the maps
     * Performs a bit of obfuscation when adding `$x has attributetype $a`, where we may provide a less concrete
     * type for $a here but later specify `$a isa subattrtype` that the query planner has to pick up on
     */
    GraqlGet build(GraknClient.Transaction tx, Random random) {
        List<Pattern> patterns = new ArrayList<>();

        // convert the maps into a graql query, linking connected variables
        for (Variable statementVariable : variableTypeMap.keySet()) {
            Type type = variableTypeMap.get(statementVariable);
            StatementInstance pattern = Graql.var(statementVariable).isa(type.label().toString());

            // attribute ownership
            if (attributeOwnership.containsKey(statementVariable)) {
                for (Variable ownedVariable : attributeOwnership.get(statementVariable)) {
                    Type ownedType = variableTypeMap.get(ownedVariable);

                    // NOTE we intentionally obfuscate the type when we say "match $x has attr $y" because later we provide
                    // a more specific "$y isa subAttr"

                    Type superOfOwnedType = SchemaWalker.walkSupsNoMeta(tx, ownedType, random);
                    pattern = pattern.has(superOfOwnedType.label().toString(), Graql.var(ownedVariable));
                }
            }

            if (relationRolePlayers.containsKey(statementVariable)) {
                for (Pair<Variable, Role> rolePlayer : relationRolePlayers.get(statementVariable)) {
                    pattern = pattern.rel(rolePlayer.getSecond().label().toString(), Graql.var(rolePlayer.getFirst()));
                }
            }

            patterns.add(pattern);

        }

        // add attribute - attribute comparisons to the query
        for (Map.Entry<Variable, Map<Variable, Graql.Token.Comparator>> attributeComparison : attributeComparisons.entrySet()) {
            Variable leftComparisonVariable = attributeComparison.getKey();
            for (Variable rightComparisonVariable : attributeComparison.getValue().keySet()) {
                Graql.Token.Comparator comparator = attributeComparison.getValue().get(rightComparisonVariable);
                patterns.add(Graql.var(leftComparisonVariable).operation(ValueProperty.Operation.Comparison.Variable.of(comparator, Graql.var(rightComparisonVariable))));
            }
        }

        return Graql.match(patterns).get();
    }
}
