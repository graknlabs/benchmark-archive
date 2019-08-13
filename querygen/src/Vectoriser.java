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

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Consume a GraqlGet query and provide a n-dimensional vector representation
 *
 *
 * TODO what to do if have 0 in the denominator
 */
public class Vectoriser {


    public static double[] vectorise(QueryBuilder query) {
        return null;
    }

    /**
     * @return - Number of unique variables in the query
     * Range: 0 - inf
     */
    public static int numVariables(QueryBuilder query) {
        return query.allVariables().size();
    }



    /**
     * Planning:
     * 2. number of unique entity, relation, attribute variables
     */

    /**
     * @return - average roles per relation (non-unique)
     * Range: 0 - inf
     */
    public static double meanRolesPerRelation(QueryBuilder query) {
        Set<Variable> relationVariables = query.relationVariables();

        int totalRolesPlayed = 0;
        int totalRelations = 0;
        for (Variable var : relationVariables) {
            List<Role> rolesPlayed = query.rolesPlayedInRelation(var);
            totalRolesPlayed += rolesPlayed.size();
            totalRelations++;
        }

        return totalRolesPlayed / (double) totalRelations;
    }



    /**
     * @return - average unique roles per relation
     * Range: 0 - mean number of role types in a releation
     *
     * Example 0: match $x isa relation; get; (no role players)
     * Example max: match $x isa marriage; $x (husband: $h, wife: $w); get;
     */
    public static double meanUniqueRolesPerRelation(QueryBuilder query) {
        Set<Variable> relationVariables = query.relationVariables();

        int totalRolesPlayed = 0;
        int totalRelations = 0;
        for (Variable var : relationVariables) {
            Set<Role> rolesPlayed = new HashSet<>(query.rolesPlayedInRelation(var));
            totalRolesPlayed += rolesPlayed.size();
            totalRelations++;
        }

        return totalRolesPlayed / (double) totalRelations;
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
    public static double meanAttributesOwnedPerThing(QueryBuilder query) {
        Set<Variable> allVariables = query.allVariables();

        int totalAttributesOwned = 0;
        int totalThingsThatCanOwnAttributes = 0;

        for (Variable var : allVariables) {
            Type type = query.getType(var);
            // determine attribtues this type can own
            Set<AttributeType<?>> ownableTypes = type.attributes().collect(Collectors.toSet());
            if (ownableTypes.size() > 0) {
                totalThingsThatCanOwnAttributes++;

                List<Variable> attributeVariablesOwned = query.attributesOwned(var);
                if (attributeVariablesOwned != null) {
                    totalAttributesOwned += attributeVariablesOwned.size();
                }
            }
        }
        return totalAttributesOwned / (double) totalThingsThatCanOwnAttributes;
    }

    /**
     * @return - mean ambiguity
     */


    /**
     * @return - mean specificity
     */

    /**
     * @return - 2*(role players + attrs owned)/#vars ~= edges per vertex
     *
     * Value Range: 0 - 1
     * because the same variable can only be used once in a role player, creating one edge from relation to role player
     * and each attribute variable will only be owned once, at least every relation is connected to every other variable
     * and every attribute is owned by everyone => 0.5 cost. We double it to count each edge in each direction so we
     * end up with range 0 - 1
     */
    public static double meanEdgesPerVariable(QueryBuilder query) {
        Set<Variable> allVariables = query.allVariables();

        int outEdges = 0;
        int numVariables = allVariables.size();

        for (Variable var : allVariables) {
            // attribute ownership edges (from owner to attribute)
            int attributesOwned = query.attributesOwned(var).size();
            outEdges += attributesOwned;

            // roles played in relation, if any, edges
            List<Role> rolesPlayed = query.rolesPlayedInRelation(var);
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
}
