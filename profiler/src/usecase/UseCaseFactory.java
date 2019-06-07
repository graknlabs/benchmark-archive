package grakn.benchmark.profiler.usecase;

import grakn.benchmark.common.configuration.BenchmarkConfiguration;
import grakn.benchmark.profiler.util.SchemaManager;
import grakn.client.GraknClient;

public class UseCaseFactory {
    private final GraknClient tracingClient;
    private final SchemaManager schemaManager;

    public UseCaseFactory(GraknClient tracingClient, SchemaManager schemaManager) {
        this.tracingClient = tracingClient;
        this.schemaManager = schemaManager;
    }

    public UseCase create(BenchmarkConfiguration config) {
        if (config.generateData()) {
            return new LoadSchemaGenerateData(config, tracingClient, schemaManager);
        }
        if (config.loadSchema() && config.staticDataImport()) {
            return new LoadSchemaLoadData(config, tracingClient, schemaManager);
        }
        if (config.loadSchema()) {
            return new LoadSchema(config, tracingClient, schemaManager);
        }
        return new ProfileExisting(config, tracingClient);
    }
}
