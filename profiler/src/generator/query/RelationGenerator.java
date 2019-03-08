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

import grakn.benchmark.profiler.generator.provider.concept.CentralConceptProvider;
import grakn.benchmark.profiler.generator.strategy.RelationStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.core.concept.ConceptId;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.Variable;

import java.util.Iterator;
import java.util.Set;

import static graql.lang.Graql.and;
import static graql.lang.Graql.var;


/**
 * Generate insert queries for the relationship type indicated by the RelationshipStrategy.
 * Individual roles are filled by concepts provided by RolePlayerStategy objects.
 * <p>
 * If a role cannot be filled no relationship will be generated.
 */
public class RelationGenerator implements QueryGenerator {
    private final RelationStrategy strategy;

    public RelationGenerator(RelationStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<GraqlInsert> generate() {

        Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies = this.strategy.getRolePlayerTypeStrategies();
        for (RolePlayerTypeStrategy rolePlayerTypeStrategy : rolePlayerTypeStrategies) {
            if (rolePlayerTypeStrategy.getConceptProvider() instanceof CentralConceptProvider) {
                ((CentralConceptProvider) rolePlayerTypeStrategy.getConceptProvider()).resetUniqueness();
            }
        }

        return buildInsertRelationshipQueryIterator();
    }

    private Iterator<GraqlInsert> buildInsertRelationshipQueryIterator() {
        return new Iterator<GraqlInsert>() {

            String relationshipTypeLabel = strategy.getTypeLabel();
            int queriesToGenerate = strategy.getNumInstancesPDF().sample();
            int queriesGenerated = 0;

            private boolean haveRequiredRolePlayers() {
                /*
                Require that each PDF requires at least 1 role player, else hasNext() may be true but not generate any queries
                AND that the provider has actually got this many role players
                 */
                return strategy.getRolePlayerTypeStrategies().stream()
                        .map(s -> s.getNumInstancesPDF().peek() > 0 && s.getConceptProvider().hasNextN(s.getNumInstancesPDF().peek()))
                        .allMatch(b -> b);
            }

            @Override
            public boolean hasNext() {
                return (queriesGenerated < queriesToGenerate) && haveRequiredRolePlayers();
            }

            @Override
            public GraqlInsert next() {

                Pattern matchVarPattern = null;  //TODO It will be faster to use a pure insert, supplying the ids for the roleplayers' variables
                Statement insertVarPattern = var("r").isa(relationshipTypeLabel);

                // For each role type strategy
                for (RolePlayerTypeStrategy rolePlayerTypeStrategy : strategy.getRolePlayerTypeStrategies()) {
                    String roleLabel = rolePlayerTypeStrategy.getTypeLabel();

                    // Find random role-players matching this type
                    // Pick ids from the list of concept ids
                    Iterator<ConceptId> conceptProvider = rolePlayerTypeStrategy.getConceptProvider();
                    int rolePlayersRequired = rolePlayerTypeStrategy.getNumInstancesPDF().sample();

                    // Build the match insert query
                    int rolePlayersAssigned = 0;
                    while (conceptProvider.hasNext() && rolePlayersAssigned < rolePlayersRequired) {
                        ConceptId conceptId = conceptProvider.next();
                        // Add the concept to the query
                        Variable v = new Variable().asUserDefined();
                        if (matchVarPattern == null) {
                            matchVarPattern = var(v).id(conceptId.toString());
                        } else {
                            Pattern varPattern = var(v).id(conceptId.toString());
                            matchVarPattern = and(matchVarPattern, varPattern);
                        }
                        insertVarPattern = insertVarPattern.rel(roleLabel, var(v));
                        rolePlayersAssigned++;
                    }
                }
                queriesGenerated++;
                return Graql.match(matchVarPattern).insert(insertVarPattern);
            }
        };
    }
}
