package grakn.benchmark.report;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class ReportDataSerializer extends StdSerializer<ReportData> {

    protected ReportDataSerializer(Class<ReportData> t) {
        super(t);
    }

    @Override
    public void serialize(ReportData value, JsonGenerator gen, SerializerProvider provider) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("metadata");
        gen.writeStringField("configName", value.configName());
        gen.writeNumberField("concurrentClients", value.concurrentClients());
        gen.writeStringField("configDescription", value.description());
        gen.writeEndObject();
        gen.writeObjectField("queryExecutionData", value.queryExecutionData());
        gen.writeEndObject();
    }
}
