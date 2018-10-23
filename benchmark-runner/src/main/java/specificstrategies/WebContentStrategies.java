package specificstrategies;

import pdf.UniformPDF;
import storage.ConceptStore;
import storage.SchemaManager;
import strategy.EntityStrategy;
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

    private void setup() {

        /**
         * Entities
         */

        // ----- Person -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "person",
                        new UniformPDF(random, 20, 40)
        ));

        // ----- organisation -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "organisation",
                        new UniformPDF(random, 1,5)
        ));

        // --- company organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "company",
                        new UniformPDF(random, 1, 5)
        ));

        // --- department organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "department",
                        new UniformPDF(random, 1, 5)
        ));

        // --- university organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "university",
                        new UniformPDF(random, 1, 5)
        ));

        // --- team organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "team",
                        new UniformPDF(random, 1, 5)
        ));


        // ----- Publication -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "publication",
                        new UniformPDF(random, 10, 50)
        ));

        // --- scientific-publication publication
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-publication",
                        new UniformPDF(random, 10, 50)
        ));

        // --- medium-post publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "medium-post",
                        new UniformPDF(random, 10, 50)
        ));

        // - article medium-post -
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "article",
                        new UniformPDF(random, 10, 50)
        ));

        // --- book publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "book",
                        new UniformPDF(random, 10, 50)
                ));

        // --- scientific-journal publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-journal",
                        new UniformPDF(random, 10, 50)
                ));


        // ----- symposium -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "symposium",
                        new UniformPDF(random, 1, 3)
        ));

        // ----- publishing-platform -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("publishing-platform",
                        new UniformPDF(random, 1, 3)
        ));

        // --- web-service publishing-platform ---
        this.entityStrategies.add(
                1,
                new EntityStrategy("web-service",
                        new UniformPDF(random, 1, 3)
        ));

        // --- website publishing-platform
        this.entityStrategies.add(
                1,
                new EntityStrategy("website",
                        new UniformPDF(random, 1, 3)
        ));

        // ----- Object -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("object",
                        new UniformPDF(random, 10, 100)
        ));


    }

}
