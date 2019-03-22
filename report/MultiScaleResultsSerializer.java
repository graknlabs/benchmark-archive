package grakn.benchmark.report;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class MultiScaleResultsSerializer extends StdSerializer<MultiScaleResults> {

    protected MultiScaleResultsSerializer(Class<MultiScaleResults> t) {
        super(t);
    }

    @Override
    public void serialize(MultiScaleResults value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeStringField("query", value.query().toString());
        gen.writeObjectField("dataPerScale", value.resultsPerScale());
        gen.writeEndObject();
    }
}
