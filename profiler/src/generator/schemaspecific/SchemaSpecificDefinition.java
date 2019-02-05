package grakn.benchmark.profiler.generator.schemaspecific;

import grakn.benchmark.profiler.generator.strategy.RouletteWheel;
import grakn.benchmark.profiler.generator.strategy.TypeStrategyInterface;


public interface SchemaSpecificDefinition {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getDefinition();
}
