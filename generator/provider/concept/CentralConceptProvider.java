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

package grakn.benchmark.generator.provider.concept;

import grakn.benchmark.generator.probdensity.ProbabilityDensityFunction;
import grakn.core.concept.ConceptId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;

/**
 * The idea of a _central_ concept is that one or several concepts
 * will be the center of many relationships added (ie. imagine this as a _repeated concept provider_)
 * <p>
 * It can be specified with a PDF that is used to construct how many _central_ concepts will be used. For example,
 * when adding multiple relationships, if the centralConceptsPdf specifies `1`, all relationships
 * will connect to that same Concept in this iteration.
 */
public class CentralConceptProvider implements ConceptIdProvider {
    private static final Logger LOG = LoggerFactory.getLogger(CentralConceptProvider.class);

    private ConceptIdProvider conceptIdProvider;
    private Boolean isReset;
    private ArrayList<ConceptId> uniqueConceptIdsList;
    private int consumeFrom = 0;
    private ProbabilityDensityFunction centralConceptsPdf;

    public CentralConceptProvider(ProbabilityDensityFunction centralConceptsPdf, ConceptIdProvider conceptIdProvider) {
        this.conceptIdProvider = conceptIdProvider;
        this.isReset = true;
        this.uniqueConceptIdsList = new ArrayList<>();
        this.centralConceptsPdf = centralConceptsPdf;
    }

    public void resetUniqueness() {
        LOG.trace("Resetting central concept provider");
        isReset = true;
    }

    @Override
    public boolean hasNext() {
        if (isReset) {
            refillBuffer();
            isReset = false;
        }
        return !uniqueConceptIdsList.isEmpty();
    }

    @Override
    public boolean hasNextN(int n) {
        // because we use this as a circular buffer
        // we always have the required number of values if we have any
        return hasNext();
    }

    @Override
    public ConceptId next() {
        // Get the same list as used previously, or generate one if not seen before
        // Only create a new stream if resetUniqueness() has been called prior

        if (isReset) {
            refillBuffer();
            isReset = false;
        }

        // construct the circular buffer-reading stream
        ConceptId value = uniqueConceptIdsList.get(consumeFrom);
        consumeFrom = (consumeFrom + 1) % uniqueConceptIdsList.size();
        return value;
    }

    private void refillBuffer() {
        // re-fill the internal buffer of conceptIds to be repeated (the centrality aspect)
        int requiredCentralConcepts = centralConceptsPdf.sample();
        this.uniqueConceptIdsList.clear();
        LOG.trace("Trying to refill central concept provider with numebr of concepts: " + requiredCentralConcepts);

        // only if the provider can provide the required number of values
        // do we fill our circular buffer
        if (conceptIdProvider.hasNextN(requiredCentralConcepts)) {
            LOG.trace("Refilling central concept provider with number of concepts: " + requiredCentralConcepts);
            int count = 0;
            while (conceptIdProvider.hasNext() && count < requiredCentralConcepts) {
                uniqueConceptIdsList.add(conceptIdProvider.next());
                count++;
            }
        }
        // otherwise, don't fill at all

        this.consumeFrom = 0;
    }
}

