package grakn.benchmark.querygen;

import grakn.core.concept.type.Type;
import graql.lang.statement.Variable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

public class QueryBuilder {

    Map<Variable, Type> variableTypeMap;

    Map<Variable, Variable> attributeOwnership;

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
        attributeOwnership.put(owner, owned);
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
}
