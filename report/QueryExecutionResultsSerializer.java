package grakn.benchmark.report;

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
