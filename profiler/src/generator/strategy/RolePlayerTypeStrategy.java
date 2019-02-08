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
import grakn.core.concept.ConceptId;

import java.util.Iterator;

/**
 * A container for the three things required for how to generate a batch of role players:
 * - The role label
 * - A PDF that can be sampled to indicate how big the new batch of roles players is going to be
 * - A provider of Concepts that is going to fill the role in the relationship
 */
public class RolePlayerTypeStrategy extends TypeStrategy {

    private Iterator<ConceptId> conceptIdProvider;

    public RolePlayerTypeStrategy(String roleLabel, ProbabilityDensityFunction numInstancesPDF, Iterator<ConceptId> conceptIdProvider) {
        super(roleLabel, numInstancesPDF);
        this.conceptIdProvider = conceptIdProvider;
    }

    public Iterator<ConceptId> getConceptProvider() {
        return conceptIdProvider;
    }
}

