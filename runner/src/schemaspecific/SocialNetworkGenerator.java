package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.pick.StringStreamGenerator;
import grakn.benchmark.runner.probdensity.FixedConstant;
import grakn.benchmark.runner.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.runner.probdensity.ScalingBoundedZipf;
import grakn.benchmark.runner.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.runner.storage.IdStoreInterface;
import grakn.benchmark.runner.strategy.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class SocialNetworkGenerator implements SchemaSpecificDataGenerator {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public SocialNetworkGenerator(Random random, ConceptStore storage) {
        this.random = random;
        this.storage = storage;

        this.entityStrategies = new RouletteWheel<>(random);
        this.relationshipStrategies = new RouletteWheel<>(random);
        this.attributeStrategies = new RouletteWheel<>(random);
        this.operationStrategies = new RouletteWheel<>(random);

        buildGenerator();
    }

    private void buildGenerator() {
        buildStrategies();
        this.operationStrategies.add(1.0, entityStrategies);
        this.operationStrategies.add(1.2, relationshipStrategies);
        this.operationStrategies.add(1.0, attributeStrategies);
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

        StringStreamGenerator nameStream = new StringStreamGenerator(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedDiscreteGaussian(this.random,18, 3),
                        new StreamProvider<>(nameStream)
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
                new StreamProvider<>(
                    new FromIdStorageConceptIdPicker(
                        random,
                        (IdStoreInterface) this.storage,
                        "person")
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "friendship",
                        new ScalingBoundedZipf(this.random, ()->this.getGraphScale(), 0.5, 2.3),
                        new HashSet<>(Arrays.asList(friendRoleFiller))
                )
        );


        // like
        RolePlayerTypeStrategy likedPageRole = new RolePlayerTypeStrategy(
                "liked",
                "like",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "page"))
        );
        RolePlayerTypeStrategy likerPersonRole = new RolePlayerTypeStrategy(
                "liker",
                "like",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "person"))
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "like",
                        new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.05, 0.001),
                        new HashSet<>(Arrays.asList(likedPageRole, likerPersonRole))
                )
        );


        // @has-name
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                "@has-name",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "person"))
        );
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                "@has-name",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "name"))
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-name",
                        new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.1, 0.03),
                        new HashSet<>(Arrays.asList(nameOwner, nameValue))
                )
        );
    }

    @Override
    public RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy() {
        return this.operationStrategies;
    }

    @Override
    public ConceptStore getConceptStore() {
        return this.storage;
    }
}
