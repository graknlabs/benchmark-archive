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

package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.pick.CentralConceptProvider;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.core.concept.ConceptId;
import grakn.core.graql.Graql;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Pattern;
import grakn.core.graql.Var;
import grakn.core.graql.VarPattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static grakn.core.graql.internal.pattern.Patterns.var;

/**
 *
 */
public class RelationshipGenerator implements QueryGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(RelationshipGenerator.class);
    private final RelationshipStrategy strategy;

    /**
     * @param strategy
     */
    public RelationshipGenerator(RelationshipStrategy strategy) {

        this.strategy = strategy;
    }

    /**
     * @return
     */
    @Override
    public Iterator<InsertQuery> generate() {

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = this.strategy.getRolePlayerTypeStrategies();
        for (RolePlayerTypeStrategy rolePlayerTypeStrategy : rolePlayerTypeStrategies) {
            if (rolePlayerTypeStrategy.getConceptProvider() instanceof CentralConceptProvider) {
                ((CentralConceptProvider) rolePlayerTypeStrategy.getConceptProvider()).resetUniqueness();
            }
        }

        return new Iterator<InsertQuery>() {

            String relationshipTypeLabel = strategy.getTypeLabel();
            int queriesToGenerate = strategy.getNumInstancesPDF().sample();
            int queriesGenerated = 0;

            private boolean allRolePlayerHaveNext() {
                return rolePlayerTypeStrategies.stream()
                        .map(s -> s.getConceptProvider())
                        .allMatch(b -> b.hasNext());
            }

            @Override
            public boolean hasNext() {
                return (queriesGenerated < queriesToGenerate) && allRolePlayerHaveNext();
            }

            @Override
            public InsertQuery next() {
                /*
                Process:
                Find roleplayer types according to the RelationshipRoleStrategy objects
                Get a stream of conceptIds that can play that role, according to the picking strategy. This stream may be
                empty for one role, in which case, a decision has to be made whether to make the relationship anyway or abort

                Currently abort if cannot find a role player for a role
                */

                Pattern matchVarPattern = null;  //TODO It will be faster to use a pure insert, supplying the ids for the roleplayers' variables
                VarPattern insertVarPattern = var("r").isa(relationshipTypeLabel);

                // For each role type strategy
                for (RolePlayerTypeStrategy rolePlayerTypeStrategy : rolePlayerTypeStrategies) {
                    String roleLabel = rolePlayerTypeStrategy.getRoleLabel();

                    // Find random role-players matching this type
                    // Pick ids from the list of concept ids
                    Iterator<ConceptId> conceptProvider = rolePlayerTypeStrategy.getConceptProvider();
                    int rolePlayersRequired = rolePlayerTypeStrategy.getNumInstancesPDF().sample();

                    // Build the match insert query
                    int rolePlayersAssigned = 0;
                    while (conceptProvider.hasNext() && rolePlayersAssigned < rolePlayersRequired) {
                        ConceptId conceptId = conceptProvider.next();
                        // Add the concept to the query
                        Var v = Graql.var().asUserDefined();
                        if (matchVarPattern == null) {
                            matchVarPattern = v.id(conceptId);
                        } else {
                            Pattern varPattern = v.id(conceptId);
                            matchVarPattern = matchVarPattern.and(varPattern);
                        }
                        insertVarPattern = insertVarPattern.rel(roleLabel, v);
                        rolePlayersAssigned++;
                    }
                }
                queriesGenerated++;
                return Graql.match(matchVarPattern).insert(insertVarPattern);
            }
        };
    }
}
