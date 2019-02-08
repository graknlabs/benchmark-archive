package grakn.benchmark.profiler.generator.definition;


import grakn.benchmark.profiler.generator.strategy.TypeStrategy;


/**
 * Provides a set of strategies for the generator, describing how to populate the graph
 */

public interface DataGeneratorDefinition {
    TypeStrategy sampleNextStrategy();
}
