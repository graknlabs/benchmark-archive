package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.storage.ConceptStore;

import java.util.Random;

public class SchemaSpecificDataGeneratorFactory {

    public static SchemaSpecificDataGenerator getSpecificStrategy(String name, Random random, ConceptStore storage) {
        switch (name) {
            case "web content":
                return new WebContentGenerator(random, storage);
            case "societal model":
                return new SocietalModelGenerator(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
