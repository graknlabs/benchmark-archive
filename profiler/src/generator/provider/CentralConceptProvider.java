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

package grakn.benchmark.profiler.generator.provider;

import grakn.benchmark.profiler.generator.probdensity.ProbabilityDensityFunction;
import grakn.core.concept.ConceptId;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The idea of a _central_ concept is that one or several concepts
 * will be the center of many relationships added (ie. imagine this as a _repeated concept provider_)
 * <p>
 * It can be specified with a PDF that is used to construct how many _central_ concepts will be used. For example,
 * when adding multiple relationships, if the centralConceptsPdf specifies `1`, all relationships
 * will connect to that same Concept in this iteration.
 */
public class CentralConceptProvider implements Iterator<ConceptId> {

    private Iterator<ConceptId> iterator;
    private Boolean isReset;
    private ArrayList<ConceptId> uniqueConceptIdsList;
    private int consumeFrom = 0;
    private ProbabilityDensityFunction centralConceptsPdf;

    public CentralConceptProvider(ProbabilityDensityFunction centralConceptsPdf, Iterator<ConceptId> iterator) {
        this.iterator = iterator;
        this.isReset = true;
        this.uniqueConceptIdsList = new ArrayList<>();
        this.centralConceptsPdf = centralConceptsPdf;
    }

    public void resetUniqueness() {
        this.isReset = true;
    }

    @Override
    public boolean hasNext() {
        return !uniqueConceptIdsList.isEmpty();
    }

    @Override
    public ConceptId next() {
        // Get the same list as used previously, or generate one if not seen before
        // Only create a new stream if resetUniqueness() has been called prior

        if (this.isReset) {
            // re-fill the internal buffer of conceptIds to be repeated (the centrality aspect)
            int uniqueness = this.centralConceptsPdf.sample();

            this.uniqueConceptIdsList.clear();

            int i = 0;
            while (iterator.hasNext() && i < uniqueness) {
                uniqueConceptIdsList.add(iterator.next());
                i++;
            }

            this.consumeFrom = 0;
            this.isReset = false;
        }

        // construct the circular buffer-reading stream
        ConceptId value = uniqueConceptIdsList.get(consumeFrom);
        consumeFrom++;
        return value;
    }
}

