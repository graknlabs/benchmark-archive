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

package grakn.benchmark.generator.query;

import grakn.benchmark.generator.provider.key.CentralConceptKeyProvider;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.RolePlayerTypeStrategy;
import graql.lang.Graql;
import graql.lang.pattern.Pattern;
import graql.lang.query.GraqlInsert;
import graql.lang.statement.Statement;
import graql.lang.statement.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;
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
    private static final Logger LOG = LoggerFactory.getLogger(RelationGenerator.class);

    private final RelationStrategy strategy;

    public RelationGenerator(RelationStrategy strategy) {
        this.strategy = strategy;
    }

    @Override
    public Iterator<GraqlInsert> generate() {

        StringBuilder roles = new StringBuilder("(");
        for (RolePlayerTypeStrategy rolePlayerTypeStrategy : this.strategy.getRolePlayerTypeStrategies()) {
            String role = rolePlayerTypeStrategy.getTypeLabel();
            roles.append(role);
            roles.append(",");
        }
        LOG.trace("Generating Rel " + strategy.getTypeLabel() + roles + "), target quantity: " + strategy.getNumInstancesPDF().peek());

        List<RolePlayerTypeStrategy> rolePlayerTypeStrategies = this.strategy.getRolePlayerTypeStrategies();
        for (RolePlayerTypeStrategy rolePlayerTypeStrategy : rolePlayerTypeStrategies) {
            if (rolePlayerTypeStrategy.getConceptKeyProvider() instanceof CentralConceptKeyProvider) {
                ((CentralConceptKeyProvider) rolePlayerTypeStrategy.getConceptKeyProvider()).resetUniqueness();
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
                return strategy.getRolePlayerTypeStrategies().stream()
                        .map(s -> s.getConceptKeyProvider().hasNextN(s.getNumInstancesPDF().peek()))
                        .allMatch(b -> b);
            }

            private boolean ensureAtLeastOneRolePlayer() {
                // we'll build some safety in here:
                //  we can hit a situation where a relationship is NEVER generated if the `.peek()` method of all the roles is 0
                //  to avoid this, if detected we sample each PDF once to hopefully skip out of the situation of only 0 roles
                boolean haveOneRolePlayer = strategy.getRolePlayerTypeStrategies().stream()
                        .map(s -> s.getNumInstancesPDF().peek() > 0)
                        .anyMatch(b -> b);

                if (!haveOneRolePlayer) {
                    LOG.warn("Found situation where all roles are required 0 times - sampling PDFs again to try to break out");
                    for (RolePlayerTypeStrategy rolePlayerTypeStrategy : strategy.getRolePlayerTypeStrategies()) {
                        rolePlayerTypeStrategy.getNumInstancesPDF().sample();
                    }

                    // only 1 re-try allowed per iteration
                    return strategy.getRolePlayerTypeStrategies().stream()
                            .map(s -> s.getNumInstancesPDF().peek() > 0)
                            .anyMatch(b -> b);
                } else {
                    return true;
                }
            }

            @Override
            public boolean hasNext() {
                return (queriesGenerated < queriesToGenerate) && ensureAtLeastOneRolePlayer() && haveRequiredRolePlayers();
            }


            @Override
            public GraqlInsert next() {

                Pattern matchVarPattern = null;  //TODO It will be faster to use a pure insert, supplying the ids for the roleplayers' variables
                Statement insertVarPattern = var("r").isa(relationshipTypeLabel);

                // For each role type strategy
                for (RolePlayerTypeStrategy rolePlayerTypeStrategy : strategy.getRolePlayerTypeStrategies()) {
                    String roleLabel = rolePlayerTypeStrategy.getTypeLabel();

                    // Find random role-players matching this type
                    // Pick ids from the list of keys
                    Iterator<Long> conceptProvider = rolePlayerTypeStrategy.getConceptKeyProvider();
                    int rolePlayersRequired = rolePlayerTypeStrategy.getNumInstancesPDF().sample();

                    // Build the match insert query
                    int rolePlayersAssigned = 0;
                    while (conceptProvider.hasNext() && rolePlayersAssigned < rolePlayersRequired) {
                        Long conceptKey = conceptProvider.next();
                        // Add the key to the query
                        Variable v = new Variable().asReturnedVar();
                        if (matchVarPattern == null) {
                            matchVarPattern = var(v).has("unique-key", conceptKey);
                        } else {
                            Pattern varPattern = var(v).has("unique-key", conceptKey);
                            matchVarPattern = and(matchVarPattern, varPattern);
                        }
                        insertVarPattern = insertVarPattern.rel(roleLabel, var(v));
                        rolePlayersAssigned++;
                    }
                }
                queriesGenerated++;
                return Graql.match(matchVarPattern).insert(insertVarPattern.has("unique-key", strategy.getConceptKeyProvider().next()));
            }
        };
    }
}
