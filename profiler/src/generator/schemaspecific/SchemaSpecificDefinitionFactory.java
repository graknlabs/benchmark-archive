package grakn.benchmark.profiler.generator.schemaspecific;

import grakn.benchmark.profiler.generator.storage.ConceptStore;

import java.util.Random;

public class SchemaSpecificDefinitionFactory {

    public static SchemaSpecificDefinition getDefinition(String name, Random random, ConceptStore storage) {
        switch (name) {
            case "social_network":
                return new SocialNetworkDefinition(random, storage);
            case "road_network":
                return new RoadNetworkDefinition(random, storage);
            case "biochemical_network":
                return new BiochemicalNetworkDefinition(random, storage);
            case "financial":
                return new FinancialTransactionsDefinition(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
