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

        int concepts = value.concepts();
        int roundTrips = value.roundTrips();
        List<Long> times = value.times();
        long[] timesArray = times.stream().mapToLong(Long::longValue).toArray();
        String type = value.type();


        gen.writeStringField("queryType", type);
        gen.writeNumberField("conceptsInvolved", concepts);
        gen.writeNumberField("roundTrips", roundTrips);

        gen.writeArrayFieldStart("time");
        gen.writeArray(timesArray, 0, timesArray.length);
        gen.writeEndArray();

        gen.writeEndObject();
    }
}
