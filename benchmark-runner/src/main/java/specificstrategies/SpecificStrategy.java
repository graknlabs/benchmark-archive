package specificstrategies;

import strategy.RouletteWheel;
import strategy.TypeStrategyInterface;


public interface SpecificStrategy {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();
}
