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
     */
    public static int numVariables(QueryBuilder query) {
        return query.allVariables().size();
    }



    /**
     * Planning:
     * 2. number of unique entity, relation, attribute variables
     */

    /**
     * 3. average roles per relation (non-unique)
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
     * 4. average unique roles per relation
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
     * 5. average attrs per thing that can have any attrs
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
     * 6. mean ambiguity
     * 7. mean specificity
     * 8. (roles played + attrs owned)/#vars ~= edges per vertex
     */
}
