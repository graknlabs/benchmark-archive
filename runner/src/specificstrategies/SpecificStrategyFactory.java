package grakn.benchmark.runner.specificstrategies;

import grakn.benchmark.runner.storage.ConceptStore;

import java.util.Random;

public class SpecificStrategyFactory {

    public static SpecificStrategy getSpecificStrategy(String name, Random random, ConceptStore storage) {
        switch (name) {
            case "web content":
                return new WebContentStrategies(random, storage);
            case "societal model":
                return new SocietalModelStrategy(random, storage);
            default:
                throw new RuntimeException("Unknown specific schema generation strategy name: " + name);
        }
    }
}
