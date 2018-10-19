package specificstrategies;

import strategy.RouletteWheelCollection;
import strategy.TypeStrategyInterface;


public interface SpecificStrategy {
    RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> getStrategy();
}
