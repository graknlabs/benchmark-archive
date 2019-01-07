package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;


public interface SchemaSpecificDataGenerator {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();
}
