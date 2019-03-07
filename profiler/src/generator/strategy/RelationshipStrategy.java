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

package grakn.benchmark.profiler.generator.strategy;

import grakn.benchmark.profiler.generator.probdensity.ProbabilityDensityFunction;

import java.util.Set;

/**
 * A container for the three things required for how to generate a new batch of relationships:
 * - The relationship type label
 * - A PDF that can be sampled to indicate how big the new batch of relationships is going to be
 * - A _set_ of RolePlayerTypeStrategy, which in turn contain a type label, quantity PDF and provider for IDs to play that role
 */
public class RelationshipStrategy extends TypeStrategy {

    private Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies;

    public <P extends ProbabilityDensityFunction> RelationshipStrategy(String typeLabel, P numInstancesPDF, Set<RolePlayerTypeStrategy> rolePlayerTypeStrategies) {
        super(typeLabel, numInstancesPDF);
        this.rolePlayerTypeStrategies = rolePlayerTypeStrategies;
    }

    public Set<RolePlayerTypeStrategy> getRolePlayerTypeStrategies() {
        return rolePlayerTypeStrategies;
    }
}
