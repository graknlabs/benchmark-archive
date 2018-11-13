package grakn.benchmark.runner.specificstrategies;

import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;


public interface SpecificStrategy {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();
}
