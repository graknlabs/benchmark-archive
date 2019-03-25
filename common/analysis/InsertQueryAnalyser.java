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

package grakn.benchmark.common.analysis;


import grakn.core.concept.Concept;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.property.IdProperty;
import graql.lang.property.IsaProperty;
import graql.lang.property.RelationProperty;
import graql.lang.property.TypeProperty;
import graql.lang.query.GraqlInsert;
import graql.lang.query.MatchClause;
import graql.lang.statement.Statement;
import graql.lang.statement.Variable;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 *
 */
public class InsertQueryAnalyser {

    public static HashSet<Concept> getInsertedConcepts(GraqlInsert query, List<ConceptMap> answers) {
        /*
        Method

        Get the set of variables used in the insert

        Find those in the insert without an id

        If there's a match statement
            Get the set of variables used in the match

            Find those variables without an id

            Remove any variables in the insert that also exist in the match

        Those variables remaining must have been inserted
        Then find those variables in the answer, and get their concepts (there should be only one concept per variable?)

         */

        List<Statement> insertStatements = query.statements();

        HashSet<Variable> insertVarsWithoutIds = getVarsWithoutIds(insertStatements);
        MatchClause match = query.match();
        if (match != null) {
            // We only do anything with the match clause if it exists
            Set<Statement> matchStatements = match.getPatterns().statements();
            HashSet<Variable> matchVars = getVars(matchStatements);
            insertVarsWithoutIds.removeAll(matchVars);
        }

        HashSet<Concept> resultConcepts = new HashSet<>();

        for (ConceptMap answer : answers) {
            for (Variable insertVarWithoutId : insertVarsWithoutIds) {
                resultConcepts.add(answer.get(insertVarWithoutId));
            }
        }
        return resultConcepts;
    }

    /**
     * Given the insert query, return the IDs of the concepts that filled ROLES in a relationship
     * that was added in the given insert query. Returns empty if no relationships added
     * Can only handle 1 relationship type inserted per query!
     *
     * @return
     */
    public static Map<String, List<Concept>> getRolePlayersAndRoles(GraqlInsert query, List<ConceptMap> answers) {
        Map<String, List<Concept>> rolePlayers = new HashMap<>();

        for (Statement queryStatements : query.statements()) {
            Optional<RelationProperty> relationshipProperty = queryStatements.getProperty(RelationProperty.class);
            if (relationshipProperty.isPresent()) {
                Collection<RelationProperty.RolePlayer> relationPlayers = relationshipProperty.get().relationPlayers();

                // extract each role player and concept into the rolePlayers map
                for (RelationProperty.RolePlayer relationPlayer : relationPlayers) {

                    // get the role, if there's no role present, throw an exception
                    Statement roleAdmin = relationPlayer.getRole().get();
                    String role = roleAdmin.getProperty(TypeProperty.class).get().name();
                    Variable rolePlayerVar = relationPlayer.getPlayer().var();
                    // add the (concept, role) to the map
                    answers.stream()
                            .map(conceptMap -> conceptMap.get(rolePlayerVar))
                            .forEach(concept -> {
                                rolePlayers.putIfAbsent(role, new LinkedList<>());
                                rolePlayers.get(role).add(concept);
                            });
                }
            }
        }
        return rolePlayers;
    }

    public static String getRelationshipTypeLabel(GraqlInsert query) {
        for (Statement queryStatements: query.statements()) {
            Optional<RelationProperty> relationshipProperty = queryStatements.getProperty(RelationProperty.class);
            if (relationshipProperty.isPresent()) {
                // if there's a relationship
                // there's also a `isa LABEL` property present
                return queryStatements.getProperty(IsaProperty.class).get().
                        type().getProperty(TypeProperty.class).get().name();
            }
        }
        return null;
    }

    private static HashSet<Variable> getVars(Set<Statement> statements) {
        HashSet<Variable> vars = new HashSet<>();
        for (Statement statement : statements) {
            vars.addAll(statement.variables());
        }
        return vars;
    }

    private static HashSet<Variable> getVarsWithoutIds(List<Statement> statements) {

        HashSet<Variable> varsWithoutIds = new HashSet<>();
        HashSet<Variable> varsWithIds = new HashSet<>();

        for (Statement statement: statements) {
            varsWithoutIds.addAll(statement.variables());
            Optional<IdProperty> idProperty = statement.getProperty(IdProperty.class);
            if (idProperty.isPresent()) {
                varsWithIds.add(statement.var());
            } else {
                // If no id is present, then add to the set
                varsWithoutIds.add(statement.var());
            }
        }
        varsWithoutIds.removeAll(varsWithIds);
        return varsWithoutIds;
    }

}
