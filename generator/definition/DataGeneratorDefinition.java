package grakn.benchmark.generator.definition;


import grakn.benchmark.generator.strategy.TypeStrategy;


/**
 * Provides a set of strategies for the generator, describing how to populate the graph
 */

public interface DataGeneratorDefinition {
    TypeStrategy sampleNextStrategy();
}
