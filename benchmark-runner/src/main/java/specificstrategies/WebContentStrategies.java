package specificstrategies;

import storage.ConceptStore;
import storage.SchemaManager;
import strategy.RouletteWheelCollection;
import strategy.TypeStrategyInterface;

import java.util.Random;

public class WebContentStrategies implements SpecificStrategy {

    private Random random;
    private SchemaManager schemaManager;
    private ConceptStore storage;

    private RouletteWheelCollection<TypeStrategyInterface> entityStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> attributeStrategies;
    private RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> operationStrategies;

    public WebContentStrategies(Random random, SchemaManager schemaManager, ConceptStore storage) {
        this.random = random;
        this.schemaManager = schemaManager;
        this.storage = storage;

        this.entityStrategies = new RouletteWheelCollection<>(random);
        this.relationshipStrategies = new RouletteWheelCollection<>(random);
        this.attributeStrategies = new RouletteWheelCollection<>(random);
        this.operationStrategies = new RouletteWheelCollection<>(random);

        setup();
    }

    @Override
    public RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> getStrategy() {
        return this.operationStrategies;
    }

    public void setup() {
       // TODO
    }

}
