/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.lib.util;

import brave.propagation.TraceContext;


/**
 * Helper class for Grpc message to Trace context conversion
 */
public class GrpcMessageConversion {
    public static TraceContext stringsToContext(String traceIdHighStr,
                                                String traceIdLowStr,
                                                String spanIdStr,
                                                String parentIdStr) {

        // traceIdHigh may be '00...00' but lowerHexToUnsignedLong doesn't like this
        // use zipkin's conversion rather than brave's because brave's doesn't like zeros
        long traceIdHigh = HexCodec.lowerHexToUnsignedLong(traceIdHighStr);
        long traceIdLow = HexCodec.lowerHexToUnsignedLong(traceIdLowStr);
        long spanId = HexCodec.lowerHexToUnsignedLong(spanIdStr);
        Long parentId;
        if (parentIdStr.length() == 0) {
            parentId = null;
        } else {
            parentId = HexCodec.lowerHexToUnsignedLong(parentIdStr);
        }
        return constructContext(traceIdHigh, traceIdLow, spanId, parentId);
    }

    // helper method to obtain span or make a new one
    public static TraceContext constructContext(long traceIdHigh,
                                                long traceIdLow,
                                                long spanId,
                                                Long parentId) {
        TraceContext.Builder builder = TraceContext.newBuilder()
                .traceIdHigh(traceIdHigh)
                .traceId(traceIdLow)
                .spanId(spanId)
                .parentId(parentId)
                .sampled(true); // this MUST be set to be able to join
        TraceContext context = builder.build();
        return context;
    }
}

