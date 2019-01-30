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

package grakn.benchmark.runner.pick;

import grakn.core.client.Grakn;
import grakn.benchmark.runner.probdensity.ProbabilityDensityFunction;

import java.util.ArrayList;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 *
 * This is a highly flexible StreamProvider. The idea of a _central_ stream is that one or several concepts
 * will be the center of many relationships added (ie. imagine this as a _repeated concept provider_)
 *
 * It can be specified with two PDFs:
 * The one used to construct specifies how many _central_ concepts will be provided. For example,
 * when adding multiple relationships, if the centralConceptsPdf specifies `1`, all relationships
 * will connect to that same Concept.
 *
 * The second PDF specifies how many of these central items to withdraw in a stream this iteration.
 * The specific values are chosen from the list of central concepts as a circular buffer from the last
 * index that hasn't been used.
 *
 * @param <T>
 */
public class CentralStreamProvider<T> implements StreamProviderInterface<T> {
    StreamInterface<T> streamer;
    private Boolean isReset;
    private ArrayList<T> conceptIdList;
    private int consumeFrom = 0;
    private ProbabilityDensityFunction centralConceptsPdf;

    public CentralStreamProvider(ProbabilityDensityFunction centralConceptsPdf, StreamInterface<T> streamer) {
        this.streamer = streamer;
        this.isReset = true;
        this.conceptIdList = new ArrayList<>();
        this.centralConceptsPdf = centralConceptsPdf;
    }

    @Override
    public void reset() {
        this.isReset = true;
    }

    @Override
    public Stream<T> getStream(ProbabilityDensityFunction pdf, Grakn.Transaction tx) {
        // Get the same list as used previously, or generate one if not seen before
        // Only create a new stream if reset() has been called prior

        if (this.isReset) {
            // re-fill the internal buffer of conceptIds to be repeated (the centrality aspect)
            int quantity = this.centralConceptsPdf.sample();

            if (this.streamer instanceof NotInRelationshipConceptIdStream) {
                ((NotInRelationshipConceptIdStream)this.streamer).setRequiredLength(quantity);
            }

            this.conceptIdList.clear();
            this.streamer.getStream(tx).limit(quantity).forEach(conceptId -> conceptIdList.add(conceptId));

            this.consumeFrom = 0;
            this.isReset = false;
        }

        if (this.conceptIdList.size() == 0) {
            return Stream.empty();
        } else {

            // construct the circular buffer-reading stream

            // number of items to read, which if longer than the length of the IDs list will wrap
            int streamLength = pdf.sample();
            int startIndex = consumeFrom;
            int endIndex = startIndex + streamLength;
            this.consumeFrom = (endIndex) % this.conceptIdList.size();

            // feed the conceptIdList as a circular buffer
            return IntStream.range(startIndex, endIndex)
                    .mapToObj(index -> conceptIdList.get(index % conceptIdList.size()));
        }
    }
}

