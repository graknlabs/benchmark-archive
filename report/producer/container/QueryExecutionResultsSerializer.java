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

package grakn.benchmark.report.producer.container;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.List;

public class QueryExecutionResultsSerializer extends StdSerializer<QueryExecutionResults> {

    protected QueryExecutionResultsSerializer(Class<QueryExecutionResults> t) {
        super(t);
    }

    @Override
    public void serialize(QueryExecutionResults value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();

        List<Long> times = value.times();
        long[] timesArray = times.stream().mapToLong(Long::longValue).toArray();

        gen.writeStringField("queryType", value.queryType());
        gen.writeNumberField("conceptsInvolved", value.concepts());
        gen.writeNumberField("roundTrips", value.roundTrips());
        gen.writeNumberField("scale", value.scale());

        gen.writeObjectField("duration", timesArray);

        gen.writeEndObject();
    }
}
