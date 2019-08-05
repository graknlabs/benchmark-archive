package grakn.benchmark.querygen;

import grakn.client.GraknClient;
import grakn.core.concept.type.Type;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlGet;
import graql.lang.statement.Statement;
import graql.lang.statement.StatementInstance;
import graql.lang.statement.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class QueryBuilder {

    Map<Variable, Type> variableTypeMap;

    Map<Variable, List<Variable>> attributeOwnership;

    List<Variable> unvisitedVariables;

    int nextVar = 0;

    QueryBuilder() {
        this.variableTypeMap = new HashMap<>();
        this.attributeOwnership = new HashMap<>();
        this.unvisitedVariables = new ArrayList<>();
    }

    Variable reserveNewVariable() {
        Variable var = new Variable("v" + nextVar);
        nextVar++;
        return var;
    }


    public void addMapping(Variable var, Type type) {
        variableTypeMap.put(var, type);
        unvisitedVariables.add(var);
    }

    public void addOwnership(Variable owner, Variable owned) {
        attributeOwnership.putIfAbsent(owner, new ArrayList<>());
        attributeOwnership.get(owner).add(owned);
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

    GraqlGet build(GraknClient.Transaction tx) {
        List<Pattern> patterns = new ArrayList<>();
        for (Variable statementVariable : variableTypeMap.keySet()) {
            Type type = variableTypeMap.get(statementVariable);
            StatementInstance pattern = Graql.var(statementVariable).isa(type.label().toString());

            // attribute ownership
            if (attributeOwnership.containsKey(statementVariable)) {
                for (Variable ownedVariable : attributeOwnership.get(statementVariable)) {
                    Type ownedType = variableTypeMap.get(ownedVariable);
                    pattern = pattern.has(ownedType.label().toString(), Graql.var(ownedVariable));
                }
            }

            // TODO relation role players

            patterns.add(pattern);

        }
        return Graql.match(patterns).get();
    }
}
