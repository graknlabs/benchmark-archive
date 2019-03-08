package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.profiler.generator.provider.concept.ConceptIdStorageProvider;
import grakn.benchmark.profiler.generator.provider.concept.NotInRelationshipConceptIdProvider;
import grakn.benchmark.profiler.generator.provider.value.UniqueIntegerProvider;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;
import grakn.benchmark.profiler.generator.util.WeightedPicker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class FinancialTransactionsDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public FinancialTransactionsDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;

        buildGenerator();
    }

    private void buildGenerator() {

        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);


        buildEntityStrategies();
        buildAttributeStrategies();
        buildExplicitRelationshipStrategies();
        buildImplicitRelationshipStrategies();

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies() {
        // we use a scaling PDF rather than a fixed one for these entities
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "trader",
                        new ScalingDiscreteGaussian(this.random, () -> storage.getGraphScale(), 0.02, 0.01))
        );

    }

    private void buildAttributeStrategies() {
        // fixed PDF for number of attributes added
        UniqueIntegerProvider idGenerator = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "quantity",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        idGenerator
                )
        );

    }

    private void buildExplicitRelationshipStrategies() {

        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy transactorRolePlayer = new RolePlayerTypeStrategy(
                "transactor",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.01),
                new ConceptIdStorageProvider(
                        random,
                        this.storage,
                        "trader")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "transaction",
                        new FixedDiscreteGaussian(this.random, 50, 10), // but fixed number of rels added per iter
                        new HashSet<>(Arrays.asList(transactorRolePlayer))
                )
        );
    }

    private void buildImplicitRelationshipStrategies() {


        // @has-quantity on the transaction relationship (1 quantity per transaction)
        RolePlayerTypeStrategy quantityOwner = new RolePlayerTypeStrategy(
                "@has-quantity-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "transaction", "@has-quantity", "@has-quantity-owner"
                )
        );
        RolePlayerTypeStrategy quantityValue = new RolePlayerTypeStrategy(
                "@has-quantity-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(
                        random,
                        this.storage,
                        "quantity"
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "@has-quantity",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.005), // more than number of entities being created to compensate for being picked less
                        new HashSet<>(Arrays.asList(quantityOwner, quantityValue))
                )
        );

    }

    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }

}
