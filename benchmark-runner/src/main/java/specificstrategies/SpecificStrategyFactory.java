package specificstrategies;

import storage.ConceptStore;
import storage.SchemaManager;

import java.util.Random;

public class SpecificStrategyFactory {

    public static SpecificStrategy getSpecificStrategy(String name, Random random, SchemaManager schemaManager, ConceptStore storage) {
        switch (name) {
            case "web content":
                return null;
            case "societal model":
                return new SocietalModelStrategy(random, schemaManager, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
