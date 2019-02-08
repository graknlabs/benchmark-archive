/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
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

package grakn.benchmark.profiler.generator.util;

import com.google.common.collect.ImmutableMultiset;
import grakn.benchmark.profiler.generator.DataGeneratorException;
import grakn.core.concept.Concept;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Match;
import grakn.core.graql.Var;
import grakn.core.graql.admin.RelationPlayer;
import grakn.core.graql.admin.VarPatternAdmin;
import grakn.core.graql.answer.ConceptMap;
import grakn.core.graql.internal.pattern.property.IdProperty;
import grakn.core.graql.internal.pattern.property.IsaProperty;
import grakn.core.graql.internal.pattern.property.LabelProperty;
import grakn.core.graql.internal.pattern.property.RelationshipProperty;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 *
 */
public class InsertQueryAnalyser {

    public static HashSet<Concept> getInsertedConcepts(InsertQuery query, List<ConceptMap> answers) {
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

        Iterator<VarPatternAdmin> insertVarPatternsIterator = query.admin().varPatterns().iterator();

        HashSet<Var> insertVarsWithoutIds = getVarsWithoutIds(insertVarPatternsIterator);
        Match match = query.admin().match();
        if (match != null) {
            // We only do anything with the match clause if it exists
            Iterator<VarPatternAdmin> matchVarPatternsIterator = match.admin().getPattern().varPatterns().iterator();
            HashSet<Var> matchVars = getVars(matchVarPatternsIterator);
            insertVarsWithoutIds.removeAll(matchVars);
        }

        HashSet<Concept> resultConcepts = new HashSet<>();

        for (ConceptMap answer : answers) {
            for (Var insertVarWithoutId : insertVarsWithoutIds) {
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
    public static Map<Concept, String> getRolePlayersAndRoles(InsertQuery query, List<ConceptMap> answers) {
        Map<Concept, String> rolePlayers = new HashMap<>();

        for (VarPatternAdmin patternAdmin : query.admin().varPatterns()) {
            Optional<RelationshipProperty> relationshipProperty = patternAdmin.getProperty(RelationshipProperty.class);
            if (relationshipProperty.isPresent()) {
                ImmutableMultiset<RelationPlayer> relationPlayers = relationshipProperty.get().relationPlayers();
                // extract each role player and concept into the rolePlayers map
                for (RelationPlayer relationPlayer : relationPlayers) {
                    // get the role, if there's no role present, throw an exception
                    VarPatternAdmin roleAdmin = relationPlayer.getRole().
                            orElseThrow(() -> new DataGeneratorException("Require explicit roles in data generator"));
                    String role = roleAdmin.getProperty(LabelProperty.class).get().label().toString();
                    Var var = relationPlayer.getRolePlayer().var();
                    // add the (concept, role) to the map
                    answers.stream()
                            .map(conceptMap -> conceptMap.get(var))
                            .forEach(concept -> rolePlayers.put(concept, role));
                }
            }
        }
        return rolePlayers;
    }

    public static String getRelationshipTypeLabel(InsertQuery query) {
        for (VarPatternAdmin patternAdmin : query.admin().varPatterns()) {
            Optional<RelationshipProperty> relationshipProperty = patternAdmin.getProperty(RelationshipProperty.class);
            if (relationshipProperty.isPresent()) {
                // if there's a relationship
                // there's also a `isa LABEL` property present
                return patternAdmin.getProperty(IsaProperty.class).get().
                        type().getProperty(LabelProperty.class).get().label().toString();
            }
        }
        return null;
    }

    private static HashSet<Var> getVars(Iterator<VarPatternAdmin> varPatternAdminIterator) {
        HashSet<Var> vars = new HashSet<>();
        while (varPatternAdminIterator.hasNext()) {
            VarPatternAdmin varPatternAdmin = varPatternAdminIterator.next();
            vars.addAll(varPatternAdmin.commonVars());
        }
        return vars;
    }

    private static HashSet<Var> getVarsWithoutIds(Iterator<VarPatternAdmin> varPatternAdminIterator) {

        HashSet<Var> varsWithoutIds = new HashSet<>();
        HashSet<Var> varsWithIds = new HashSet<>();

        while (varPatternAdminIterator.hasNext()) {

            VarPatternAdmin varPatternAdmin = varPatternAdminIterator.next();
            varsWithoutIds.addAll(varPatternAdmin.commonVars());
            Optional<IdProperty> idProperty = varPatternAdmin.getProperty(IdProperty.class);
            if (idProperty.isPresent()) {
                varsWithIds.add(varPatternAdmin.var());
            } else {
                // If no id is present, then add to the set
                varsWithoutIds.add(varPatternAdmin.var());
            }
        }
        varsWithoutIds.removeAll(varsWithIds);
        return varsWithoutIds;
    }

}
