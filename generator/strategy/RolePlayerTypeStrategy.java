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

package grakn.benchmark.generator.strategy;

import grakn.benchmark.generator.probdensity.ProbabilityDensityFunction;
import grakn.benchmark.generator.provider.key.ConceptKeyProvider;

/**
 * A container for the three things required for how to generate a batch of role players:
 * - The role label
 * - A PDF that can be sampled to indicate how big the new batch of roles players is going to be
 * - A provider of Concepts that is going to fill the role in the relationship
 */
public class RolePlayerTypeStrategy extends TypeStrategy {


    public RolePlayerTypeStrategy(String roleLabel, ProbabilityDensityFunction numInstancesPDF, ConceptKeyProvider conceptKeyProvider) {
        super(roleLabel, numInstancesPDF, conceptKeyProvider);
    }

}

