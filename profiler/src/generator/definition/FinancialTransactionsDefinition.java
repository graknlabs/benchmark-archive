package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.pick.CountingIterator;
import grakn.benchmark.profiler.generator.pick.WeightedPicker;
import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.storage.ConceptIdStoragePicker;
import grakn.benchmark.profiler.generator.storage.NotInRelationshipConceptIdProvider;
import grakn.benchmark.profiler.generator.strategy.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class FinancialTransactionsDefinition extends DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public FinancialTransactionsDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;


        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies = new WeightedPicker<>(random);

        buildGenerator();
    }

    private void buildGenerator() {
        buildStrategies();
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildStrategies() {

        /*
        Entities
         */

        // SCALING number of entities added!
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "trader",
                        new ScalingDiscreteGaussian(this.random, () -> storage.getGraphScale(), 0.02, 0.01))
        );


        /*
        Attributes
         */

        // FIXED number of attributes added
        CountingIterator idGenerator = new CountingIterator(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "quantity",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        idGenerator
                )
        );


        /*
        Relationships
         */

        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy transactorRolePlayer = new RolePlayerTypeStrategy(
                "transactor",
                "transaction",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.01),
                new ConceptIdStoragePicker(
                        random,
                        this.storage,
                        "trader")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "transaction",
                        new FixedDiscreteGaussian(this.random, 50, 10), // but fixed number of rels added per iter
                        new HashSet<>(Arrays.asList(transactorRolePlayer))
                )
        );


        // @has-quantity on the transaction relationship (1 quantity per transaction)
        RolePlayerTypeStrategy quantityOwner = new RolePlayerTypeStrategy(
                "@has-quantity-owner",
                "@has-quantity",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "transaction", "@has-quantity", "@has-quantity-owner"
                )
        );
        RolePlayerTypeStrategy quantityValue = new RolePlayerTypeStrategy(
                "@has-quantity-value",
                "@has-quantity",
                new FixedConstant(1),
                new ConceptIdStoragePicker(
                        random,
                        this.storage,
                        "quantity"
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-quantity",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.005), // more than number of entities being created to compensate for being picked less
                        new HashSet<>(Arrays.asList(quantityOwner, quantityValue))
                )
        );

    }

    @Override
    protected WeightedPicker<WeightedPicker<TypeStrategy>> getDefinition() {
        return this.metaTypeStrategies;
    }

}
