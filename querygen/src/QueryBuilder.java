package grakn.benchmark.querygen;

import grakn.core.concept.type.Type;
import graql.lang.statement.Variable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class QueryBuilder {

    Map<Variable, Type> variableTypeMap;

    Map<Variable, Variable> attributeOwnership;

    List<Variable> unvisitedVariables;

    int nextVar = 0;

    public QueryBuilder() {
        this.variableTypeMap = new HashMap<>();
    }

    public Variable reserveNewVariable() {
        Variable var = new Variable("v" + nextVar);
        nextVar++;
        return var;
    }

    public void addMapping(Variable var, Type type) {
        variableTypeMap.put(var, type);
        unvisitedVariables.add(var);
    }

    public Type getType(Variable var) {
        return variableTypeMap.get(var);
    }

    public Variable randomUnvisitedVariable(Random random) {
        int index = random.nextInt(unvisitedVariables.size());
        return unvisitedVariables.get(index);
    }

    public void visitVariable(Variable var) {
        unvisitedVariables.remove(var);
    }


}
