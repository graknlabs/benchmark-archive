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


import grakn.core.concept.type.AttributeType;
import grakn.core.concept.type.Role;
import grakn.core.concept.type.Type;
import graql.lang.statement.Variable;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consume a QueryBuilder and provide a n-dimensional vector representation
 */
public class Vectoriser {
    private final QueryBuilder queryBuilder;

    public Vectoriser(QueryBuilder queryBuilder) {
        this.queryBuilder = queryBuilder;
    }

    /**
     * @return - Number of unique variables in the query
     * Range: 0 - inf
     */
    int numVariables() {
        return queryBuilder.allVariables().size();
    }


    /**
     * Planning:
     * 2. number of unique entity, relation, attribute variables
     */

    /**
     * @return - average roles per relation (non-unique)
     * Range: 0 - inf
     */
    double meanRolesPerRelation() {
        Set<Variable> relationVariables = queryBuilder.relationVariables();

        int totalRolesPlayed = 0;
        int totalRelations = 0;
        for (Variable var : relationVariables) {
            List<Role> rolesPlayed = queryBuilder.rolesPlayedInRelation(var);
            totalRolesPlayed += rolesPlayed.size();
            totalRelations++;
        }

        if (totalRelations != 0) {
            return totalRolesPlayed / (double) totalRelations;
        } else {
            return 0.0;
        }
    }



    /**
     * @return - average unique roles per relation
     * Range: 0 - mean number of role types in a releation
     *
     * Example 0: match $x isa relation; get; (no role players)
     * Example max: match $x isa marriage; $x (husband: $h, wife: $w); get;
     */
    double meanUniqueRolesPerRelation() {
        Set<Variable> relationVariables = queryBuilder.relationVariables();

        int totalRolesPlayed = 0;
        int totalRelations = 0;
        for (Variable var : relationVariables) {
            Set<Role> rolesPlayed = new HashSet<>(queryBuilder.rolesPlayedInRelation(var));
            totalRolesPlayed += rolesPlayed.size();
            totalRelations++;
        }

        if (totalRelations != 0) {
            return totalRolesPlayed / (double) totalRelations;
        } else{
            return 0.0;
        }
    }

    /**
     * @return - average attrs per thing that can have any attrs
     * Range: 0 - inf
     *
     * Example of 0: match $x isa person; get; (where a person can own a name)
     * Example of 3: match $x isa person, has name $a1, has name $a2, has name $a3; get; (where a person can own a name)
     *
     * TODO include the idea of != to ensure the edges are actually different, otherwise the query just returns the same thing many times
     */
    double meanAttributesOwnedPerThing() {
        Set<Variable> allVariables = queryBuilder.allVariables();

        int totalAttributesOwned = 0;
        int totalThingsThatCanOwnAttributes = 0;

        for (Variable var : allVariables) {
            Type type = queryBuilder.getType(var);
            // determine attribtues this type can own
            Set<AttributeType> ownableTypes = type.attributes().collect(Collectors.toSet());
            if (ownableTypes.size() > 0) {
                totalThingsThatCanOwnAttributes++;

                List<Variable> attributeVariablesOwned = queryBuilder.attributesOwned(var);
                if (attributeVariablesOwned != null) {
                    totalAttributesOwned += attributeVariablesOwned.size();
                }
            }
        }

        if (totalThingsThatCanOwnAttributes != 0) {
            return totalAttributesOwned / (double) totalThingsThatCanOwnAttributes;
        } else {
            return 0.0;
        }
    }

    /**
     * Query ambiguity is a measure of the number of choices the query planner has to make
     * The higher the ambiguity, the more difficult a query is to plan
     *
     * The full computation enumerate all the orders of exploring nodes,
     * similar to what the query planner does in the optimal tree to plan traversal
     * This would be a potentially exponential tree search...
     *
     * As a simpler estimation of the number of choices there are to make we can perform the following variable-local
     * explanation:
     *
     * sum(E(var)!)
     *
     * where E is the number of edges connected to a variable `var`.
     * This is a local, 1-hop estimation of the number of choices to make
     *
     * Intuitively the ambiguity score should capture the following:
     * - linear chains are mid-level difficulty to plan, depending on where you elect to start
     * - the number of choices induced by a node is some function of the factorial of the number of connections (orderings of exploring the edges)
     * - more variables will always induce a more difficult planning phase
     *
     * The underlying assumption here is that there is 1 optimal plan that we are trying to find.
     * The more choices there are, the less likely/more work we have to do find it
     *
     * @return - ambiguity score
     */
    double ambiguity() {
        Set<Variable> allVariables = queryBuilder.allVariables();

        Map<Variable, Integer> edgeCounts = new HashMap<>();
        for (Variable var : allVariables) {
            edgeCounts.putIfAbsent(var, 0);

            // attribute ownership edges (from owner to attribute)
            List<Variable> variablesOwned = queryBuilder.attributesOwned(var);
            if (variablesOwned != null) {
                edgeCounts.put(var, edgeCounts.get(var) + variablesOwned.size());
                for (Variable ownedVariable : variablesOwned) {
                    edgeCounts.put(ownedVariable, edgeCounts.getOrDefault(ownedVariable, 0) + 1);
                }
            }

            // roles played in relation, if any, edges
            List<Role> rolesPlayed = queryBuilder.rolesPlayedInRelation(var);
            if (rolesPlayed != null) {
                edgeCounts.put(var, edgeCounts.get(var) + rolesPlayed.size());
            }

            Set<Variable> rolePlayers = queryBuilder.rolePlayersInRelation(var);
            if (rolePlayers != null) {
                for (Variable rolePlayer : rolePlayers) {
                    edgeCounts.put(rolePlayer, edgeCounts.getOrDefault(rolePlayer, 0) + 1);
                }
            }
        }

        int ambiguity = 0;
        for (Integer edges : edgeCounts.values()) {
            long f = factorial(edges);
            ambiguity += f;
        }

        return ambiguity;
    }

    static long factorial(int number) {
        long result = 1;

        for (int factor = 2; factor <= number; factor++) {
            result *= factor;
        }

        return result;
    }


    /**
     * A measure of specific the query is, compared to how specific it could be overall.
     * Note that making it more specific could change the meaning of the query. However,
     * it is a general measure of how many results may be matched. It could also be seen as a sort
     * of measure of how well we can plan a query overall. The more specific the components, the more we can
     * try to infer and prune (in the long term).
     *
     * Ie. a low specificity query: match $x isa entity; get; // where we have thing - entity - home - apartment
     * a high specificity query: match $x isa apartment; get;
     *
     * Computation:
     * For each variable in the query, obtain its type T. Calculate its depth d(T). Obtain all the leaf
     * child types L(T). Take the mean depth of T wrt. its child leaves. Compute the mean specificity of T:
     * over l in L(T): m(T) = sum(d(T)/d(l)) / #(L(T))
     *
     * then compute the mean of m(T) = sum(m(T)) / #s(T)
     *
     * @return - mean specificity
     */
    double specificity() {
        Set<Variable> allVariables = queryBuilder.allVariables();

        double specificity = 0.0;
        for (Variable var : allVariables) {
            Type type = queryBuilder.getType(var);

            Set<Type> leafChildren = leafChildren(type);
            if (!leafChildren.isEmpty()) {
                int typeDepth = depth(type);
                double meanDepth = 0.0;
                for (Type leafChild : leafChildren) {
                    meanDepth += typeDepth / ((double) depth(leafChild));
                }
                specificity += meanDepth/leafChildren.size();
            }
        }

        specificity /= allVariables.size();
        return specificity;
    }

    int depth(Type t) {
        if (t.label().toString().equals("thing")) {
            return 0;
        }

        int depth = 1;
        Type parent = t;
        while (parent.sup() != null && !parent.label().toString().equals("thing")) {
            depth++;
            parent = parent.sup();
        }
        return depth;
    }

    /**
     *
     * @param t - concept Type t
     * @return - set of child types that are also leaves
     */
    Set<Type> leafChildren(Type t) {
        List<? extends Type> children = t.subs().collect(Collectors.toList());
        Set<Type> leafChildren = new HashSet<>();
        for (Type child : children) {
            // filter out children that have more `subs` than themselves
            if (child.subs().collect(Collectors.toList()).size() == 1) {
                leafChildren.add(child);
            }
        }
        return leafChildren;
    }

    /**
     * @return - 2*(role players + attrs owned)/#vars ~= edges per vertex
     *
     * Value Range: 0 - 1
     * because the same variable can only be used once in a role player, creating one edge from relation to role player
     * and each attribute variable will only be owned once, at least every relation is connected to every other variable
     * and every attribute is owned by everyone => 0.5 cost. We double it to count each edge in each direction so we
     * end up with range 0 - 1
     */
    double meanEdgesPerVariable() {
        Set<Variable> allVariables = queryBuilder.allVariables();

        int outEdges = 0;
        int numVariables = allVariables.size();

        for (Variable var : allVariables) {
            // attribute ownership edges (from owner to attribute)
            List<Variable> attributeVariables = queryBuilder.attributesOwned(var);
            int attributesOwned = attributeVariables != null ? attributeVariables.size() : 0;
            outEdges += attributesOwned;

            // roles played in relation, if any, edges
            List<Role> rolesPlayed = queryBuilder.rolesPlayedInRelation(var);
            if (rolesPlayed != null) {
                outEdges += rolesPlayed.size();
            }

            // we only track the roles played in relation, ie edges from relation to role player
            // rather than also computing the roles played by each variable
        }

        // simple example: two variables, one ownership =>  0.5
        // to expand the range of this value we double the outEdges to double count each edge in both directions

        return 2.0*outEdges / numVariables;
    }


    /**
     * @return
     */
    int numComparisons() {
        return queryBuilder.numAttributeComparisons();
    }
}
