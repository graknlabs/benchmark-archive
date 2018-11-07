package ai.grakn.benchmark.runner.specificstrategies;

import ai.grakn.benchmark.runner.strategy.RouletteWheel;
import ai.grakn.benchmark.runner.strategy.TypeStrategyInterface;


public interface SpecificStrategy {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();
}
