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

package grakn.benchmark.lib.instrumentation;

import brave.ScopedSpan;
import brave.Span;
import brave.Tracer;
import brave.Tracing;
import brave.propagation.TraceContext;
import grakn.benchmark.lib.util.GrpcMessageConversion;
import grakn.protocol.session.SessionProto;
import zipkin2.reporter.AsyncReporter;
import zipkin2.reporter.urlconnection.URLConnectionSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;


/**
 * This component is stateless, as parts of it (eg initInstrumentation) is only called once
 * whereas the others may be used my multiple threads in the case of concurrent transactions
 */
public class ServerTracing {

    private static final Map<Integer, ScopedSpan> openSpans = new ConcurrentHashMap<>();
    private static final AtomicInteger currentSpanId = new AtomicInteger();

    public static void initInstrumentation(String tracingServiceName) {
        // create a Zipkin reporter for the whole server
        AsyncReporter<zipkin2.Span> reporter = AsyncReporter.create(URLConnectionSender.create("http://localhost:9411/api/v2/spans"));

        // create a global Tracing instance with reporting
        Tracing.newBuilder()
                .localServiceName(tracingServiceName)
                .supportsJoin(false)
                .spanReporter(reporter)
                .build();
    }


    /**
     * Determine if tracing is enabled at all on the server
     *
     * @return
     */
    public static boolean tracingEnabled() {
        return Tracing.currentTracer() != null;
    }

    /**
     * Retrieves the current active Span (thread-local)
     *
     * @return
     */
    public static Span currentSpan() {
        return Tracing.currentTracer().currentSpan();
    }

    /**
     * Determine if tracing is enabled on the server and there is a thread-local active span
     */
    public static boolean tracingActive() {
        return tracingEnabled() && currentSpan() != null;
    }

    /**
     * Determine if tracing is enabled on the server
     * and the received message contains a TraceContext transmitted in the metadata fields
     *
     * @param message
     * @return
     */
    public static boolean tracingEnabledFromMessage(SessionProto.Transaction.Req message) {

        if (tracingEnabled() && message.getMetadataOrDefault("traceIdLow", "").length() > 0) {
            return true;
        }
        return false;
    }

    public static TraceContext extractTraceContext(SessionProto.Transaction.Req message) {
        String traceIdHigh = message.getMetadataOrThrow("traceIdHigh");
        String traceIdLow = message.getMetadataOrThrow("traceIdLow");
        String spanId = message.getMetadataOrThrow("spanId");
        String parentId = message.getMetadataOrDefault("parentId", "");
        return GrpcMessageConversion.stringsToContext(traceIdHigh, traceIdLow, spanId, parentId);
    }


    /**
     * @param spanName
     * @param parentContext
     * @return A new Span with the given parent Context, NOT thread-local and NOT `.start()`-ed
     */
    public static Span createChildSpanWithParentContext(String spanName, TraceContext parentContext) {
        Tracer tracing = Tracing.currentTracer();
        Span child = tracing.newChild(parentContext);
        child.name(spanName);
        return child;
    }

    /**
     * @param spanName
     * @param parentContext
     * @return A new started ScopedSpan with the given parent Context (ie. one that is thread-local, `.start()` already has been called on it)
     */
    public static ScopedSpan startScopedChildSpanWithParentContext(String spanName, TraceContext parentContext) {
        Tracer tracing = Tracing.currentTracer();
        ScopedSpan child = tracing.startScopedSpanWithParent(spanName, parentContext);
        return child;
    }

    /**
     * Build child span using the parent context when the parent is already present in openSpan map
     * @param spanName
     * @param parentSpanId
     * @return The Id associated to a new started ScopedSpan with the given parent Context (ie. one that is thread-local, `.start()` already has been called on it)
     *         This returns -1 if tracing is not enabled.
     */
    public static int startScopedChildSpanWithParentContext(String spanName, int parentSpanId) {
        if(!tracingActive()) return -1;

        Tracer tracing = Tracing.currentTracer();
        ScopedSpan parent = openSpans.get(parentSpanId);
        ScopedSpan child = tracing.startScopedSpanWithParent(spanName, parent.context());
        int spanId = currentSpanId.incrementAndGet();
        openSpans.put(spanId, child);
        return spanId;
    }


    /**
     * Looks up the current Span in thread-local storage, then creates a new child span on it with the given name
     * that is NOT thread-local nor started.
     *
     * @param spanName
     * @return
     */
    public static Span createChildSpan(String spanName) {
        Span currentSpan = currentSpan();
        return createChildSpanWithParentContext(spanName, currentSpan.context());
    }

    /**
     * Looks up the current Span in thread-local storage, create a new scoped child span with the given name
     * that IS thread-local AND has been started.
     * Store the scopedSpan in map associated to a unique ID.
     *
     * This also checks if tracing is active, if not, do nothing.
     *
     * @param spanName
     * @return unique ID that will be used to finish the span. This returns -1 if tracing is not enabled.
     */
    public static int startScopedChildSpan(String spanName) {
        if (!tracingActive()) return -1;

        Span currentSpan = currentSpan();
        ScopedSpan scopedSpan = startScopedChildSpanWithParentContext(spanName, currentSpan.context());
        int spanId = currentSpanId.incrementAndGet();
        openSpans.put(spanId, scopedSpan);
        return spanId;
    }

    /**
     * Finishes scopedSpan associated to spanId and removes it from in-memory map
     *
     * This also checks if tracing is active, if not, do nothing.
     *
     * @param spanId unique ID that will be used to finish the span
     */

    public static void closeScopedChildSpan(int spanId) {
        if (!tracingActive()) return;

        ScopedSpan scopedSpan = openSpans.remove(spanId);
        scopedSpan.finish();
    }

    /**
     * This will be used by tests to verify that there are no spans unfinished.
     * @return true if all spans have been finished, else otherwise
     */
    public boolean allSpansFinished(){
        return openSpans.isEmpty();
    }
}
