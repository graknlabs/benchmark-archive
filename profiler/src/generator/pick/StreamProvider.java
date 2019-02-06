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

package grakn.benchmark.profiler.generator.pick;

import grakn.benchmark.profiler.generator.probdensity.ProbabilityDensityFunction;

import java.util.stream.Stream;

/**
 * @param <T>
 */
public class StreamProvider<T> implements StreamProviderInterface<T> {
    private StreamInterface<T> streamer;

    public StreamProvider(StreamInterface<T> streamer) {
        this.streamer = streamer;
    }

    @Override
    public void reset() { }

    @Override
    public Stream<T> getStream(ProbabilityDensityFunction pdf) {
        // Simply limit the stream of ConceptIds to the number given by the probdensity
        int streamLength = pdf.sample();

        if (this.streamer.checkAvailable(streamLength)) {

            // limit check for availability of # required
            Stream<T> stream = this.streamer.getStream();

            //TODO also check the stream in case it curtails with nulls?

            // Return the unadjusted stream but with a limit
            return stream.limit(streamLength);
        } else {
            return Stream.empty();
        }
    }
}