package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.storage.ConceptStorage;

import java.util.Random;

public class DefinitionFactory {

    public static DataGeneratorDefinition getDefinition(String name, Random random, ConceptStorage storage) {
        switch (name) {
            case "social_network":
                return new SocialNetworkDefinition(random, storage);
            case "road_network":
                return new RoadNetworkDefinition(random, storage);
            case "biochemical_network":
                return new BiochemicalNetworkDefinition(random, storage);
            case "financial":
                return new FinancialTransactionsDefinition(random, storage);
            case "generic_uniform_network":
                return new GenericUniformNetworkDefinition(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
