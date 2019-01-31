package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;


public interface SchemaSpecificDataGenerator {
    RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy();

    ConceptStore getConceptStore();
    default int getGraphScale() {
        ConceptStore storage = getConceptStore();
        int entities = storage.totalEntities();
        int attributes = storage.totalAttributes();
        int relationships = storage.totalExplicitRelationships();
        return entities + attributes + relationships;
    }
}
