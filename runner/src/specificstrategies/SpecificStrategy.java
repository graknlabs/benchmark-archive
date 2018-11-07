package ai.grakn.benchmark.benchmarkrunner.specificstrategies;

import ai.grakn.benchmark.benchmarkrunner.strategy.RouletteWheel;
import ai.grakn.benchmark.benchmarkrunner.strategy.TypeStrategyInterface;


public interface SpecificStrategy {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();
}
