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

package ai.grakn.benchmark.runner.pick;

import ai.grakn.client.Grakn;
import ai.grakn.benchmark.runner.pdf.PDF;

import java.util.ArrayList;
import java.util.stream.Stream;

/**
 * @param <T>
 */
public class CentralStreamProvider<T> implements StreamProviderInterface<T> {
    StreamInterface<T> streamer;
    private Boolean isReset;
    private ArrayList<T> conceptIdList;

    public CentralStreamProvider(StreamInterface<T> streamer) {
        this.streamer = streamer;
        this.isReset = true;
        this.conceptIdList = new ArrayList<>();
    }

    @Override
    public void reset() {
        this.isReset = true;
    }

    @Override
    public Stream<T> getStream(PDF pdf, Grakn.Transaction tx) {
        // Get the same list as used previously, or generate one if not seen before
        // Only create a new stream if reset() has been called prior

        int streamLength = pdf.next();
        if (this.isReset) {

            // TODO remove this hack when we have negation
            if (this.streamer instanceof NotInRelationshipConceptIdStream) {
                ((NotInRelationshipConceptIdStream)this.streamer).setRequiredLength(streamLength);
            }

            // don't bother with checking if the stream is long enough here, might just return a reduced length...
            Stream<T> stream = this.streamer.getStream(tx);

            //Read stream to list and store to be used again later
            this.conceptIdList.clear();

            stream.limit(streamLength).forEach(conceptId -> this.conceptIdList.add(conceptId));

            this.isReset = false;
        }
        // Return the same stream as before
        return this.conceptIdList.stream();
    }
}

