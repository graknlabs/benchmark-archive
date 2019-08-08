package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Type;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.property.ValueProperty;
import graql.lang.query.GraqlGet;
import graql.lang.statement.StatementInstance;
import graql.lang.statement.Variable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class QueryBuilder {

    final Map<Variable, Type> variableTypeMap;
    final Map<Variable, List<Variable>> attributeOwnership;
    final Map<Variable, List<Pair<Variable, Role>>> relationRolePlayers;
    final Map<Variable, Pair<Variable, Graql.Token.Comparator>> attributeComparisons;

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


    void addMapping(Variable var, Type type) {
        variableTypeMap.put(var, type);
        unvisitedVariables.add(var);
    }

    void addOwnership(Variable owner, Variable owned) {
        attributeOwnership.putIfAbsent(owner, new ArrayList<>());
        attributeOwnership.get(owner).add(owned);
    }

    void addRolePlayer(Variable relationVar, Variable rolePlayerVariable, Role role) {
        relationRolePlayers.putIfAbsent(relationVar, new ArrayList<>());
        relationRolePlayers.get(relationVar).add(new Pair<>(rolePlayerVariable, role));
    }

    void addComparison(Variable var1, Variable var2, Graql.Token.Comparator comparator) {
        attributeComparisons.put(var1, new Pair<>(var2, comparator));
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

    Map<AttributeType<?>, List<Variable>> attributeTypeVariables() {
        Map<AttributeType<?>, List<Variable>> attrTypeMapping = new HashMap<>();

        variableTypeMap.entrySet().stream()
                .filter(entry -> entry.getValue().isAttributeType())
                .forEach(entry -> attrTypeMapping.merge(
                        entry.getValue().asAttributeType(),
                        Collections.singletonList(entry.getKey()),
                        (l1, l2) -> Stream.concat(l1.stream(), l2.stream()).collect(Collectors.toList())
                ));

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
        for (Map.Entry<Variable, Pair<Variable, Graql.Token.Comparator>> attributeComparison : attributeComparisons.entrySet()) {
            Variable v1 = attributeComparison.getKey();
            Variable v2 = attributeComparison.getValue().getFirst();
            Graql.Token.Comparator comparator = attributeComparison.getValue().getSecond();
            patterns.add(Graql.var(v1).operation(ValueProperty.Operation.Comparison.Variable.of(comparator, Graql.var(v2))));
        }

        return Graql.match(patterns).get();
    }
}
