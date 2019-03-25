package grakn.benchmark.profiler.util;

import grakn.benchmark.lib.instrumentation.ClientInterceptor;
import grakn.core.client.GraknClient;
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
