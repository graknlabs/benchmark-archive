package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.pick.RandomStringIterator;
import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.ScalingBoundedZipf;
import grakn.benchmark.profiler.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.storage.ConceptIdStoragePicker;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.profiler.generator.pick.WeightedPicker;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class SocialNetworkDefinition extends DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public SocialNetworkDefinition(Random random, ConceptStorage storage) {
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
        this.metaTypeStrategies.add(1.2, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildStrategies() {

        /*
        Entities
         */

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "person",
                        new FixedDiscreteGaussian(this.random, 25, 10))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "page",
                        new FixedDiscreteGaussian(this.random, 5, 1)
                )
        );

        /*
        Attributes
         */

        RandomStringIterator nameStream = new RandomStringIterator(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedDiscreteGaussian(this.random,18, 3),
                        nameStream
                )
        );


        /*
        Relationships
         */


        // friendship
        RolePlayerTypeStrategy friendRoleFiller = new RolePlayerTypeStrategy(
                "friend",
                "friendship",
                new FixedConstant(2),
                    new ConceptIdStoragePicker(
                        random,
                        this.storage,
                        "person")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "friendship",
                        new ScalingBoundedZipf(this.random, ()-> storage.getGraphScale(), 0.5, 2.3),
                        new HashSet<>(Arrays.asList(friendRoleFiller))
                )
        );


        // like
        RolePlayerTypeStrategy likedPageRole = new RolePlayerTypeStrategy(
                "liked",
                "like",
                new FixedConstant(1),
                new ConceptIdStoragePicker(random, storage, "page")
        );
        RolePlayerTypeStrategy likerPersonRole = new RolePlayerTypeStrategy(
                "liker",
                "like",
                new FixedConstant(1),
                new ConceptIdStoragePicker(random, storage, "person")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "like",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.05, 0.001),
                        new HashSet<>(Arrays.asList(likedPageRole, likerPersonRole))
                )
        );


        // @has-name
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                "@has-name",
                new FixedConstant(1),
                new ConceptIdStoragePicker(random, storage, "person")
        );
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                "@has-name",
                new FixedConstant(1),
                new ConceptIdStoragePicker(random, storage, "name")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-name",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.1, 0.03),
                        new HashSet<>(Arrays.asList(nameOwner, nameValue))
                )
        );
    }

    @Override
    protected WeightedPicker<WeightedPicker<TypeStrategy>> getDefinition() {
        return this.metaTypeStrategies;
    }
}
