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

package grakn.benchmark.profiler.util;


import grakn.benchmark.lib.instrumentation.ClientInterceptor;
import grakn.client.GraknClient;
import grakn.core.common.http.SimpleURI;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class TracingGraknClient {

    public static GraknClient get(String uri) {
        GraknClient client = new GraknClient(uri);
        SimpleURI parsedURI = new SimpleURI(uri);

        ManagedChannel channel = ManagedChannelBuilder
                .forAddress(parsedURI.getHost(), parsedURI.getPort())
                .intercept(new ClientInterceptor("client-java-instrumentation"))
                .usePlaintext().build();

        client.overrideChannel(channel);

        return client;
    }
}
