package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.storage.ConceptStore;

import java.util.Random;

public class SchemaSpecificDataGeneratorFactory {

    public static SchemaSpecificDataGenerator getSpecificStrategy(String name, Random random, ConceptStore storage) {
        switch (name) {
            case "web_content":
                return new WebContentGenerator(random, storage);
            case "societal_model":
                return new SocietalModelGenerator(random, storage);
            case "social_network":
                return new SocialNetworkGenerator(random, storage);
            case "road_network":
                return new RoadNetworkGenerator(random, storage);
            case "biochem_network":
                return new BiochemNetworkGenerator(random, storage);
            case "financial":
                return new FinancialTransactionsGenerator(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
