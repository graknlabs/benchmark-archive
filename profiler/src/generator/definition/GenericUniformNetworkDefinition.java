package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.FixedUniform;
import grakn.benchmark.profiler.generator.provider.concept.CentralConceptProvider;
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

public class GenericUniformNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public GenericUniformNetworkDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;
        buildDefinition();
    }

    private void buildDefinition() {

        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);

        buildEntityStrategies();
        buildAttributeStrategies();
        buildExplicitRelationshipStrategies();
        buildImplicitRelationshipStrategies();

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(2.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }


    private void buildEntityStrategies() {
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "blob",
                        new FixedUniform(this.random, 5, 15))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "square",
                        new FixedUniform(this.random, 10, 20))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "circle",
                        new FixedUniform(this.random, 15, 25))
        );

    }

    private void buildAttributeStrategies() {
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "blob-value",
                        new FixedUniform(this.random, 1, 3),
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "circle-value",
                        new FixedUniform(this.random, 1, 5),
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "square-value",
                        new FixedUniform(this.random, 2, 9),
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "interaction-value",
                        new FixedUniform(this.random, 1, 5),
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "ownership-cost",
                        new FixedUniform(this.random, 1, 4),
                        new UniqueIntegerProvider(0)
                )
        );
    }

    private void buildExplicitRelationshipStrategies() {


        // -------------    interactions    ---------------

        /* _interaction_ with 2-12 role players */
        RolePlayerTypeStrategy blobberRolePlayer = new RolePlayerTypeStrategy("blobber",
                new FixedUniform(random, 0, 4),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy circlerRolePlayer = new RolePlayerTypeStrategy("circler",
                new FixedUniform(random, 1, 7),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        RolePlayerTypeStrategy squarerRolePlayer = new RolePlayerTypeStrategy("squarer",
                new FixedUniform(random, 1, 11),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("interaction",
                        new FixedUniform(this.random, 15, 35),
                        new HashSet<>(Arrays.asList(blobberRolePlayer, circlerRolePlayer, squarerRolePlayer))
                )
        );

        // -------------    ownership    ---------------

        /* blob not an owner gets assigned an average of 10 blobs to own */
        RolePlayerTypeStrategy blobOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 8, 3),
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 20, 40),
                        new HashSet<>(Arrays.asList(blobOwner, blobOwned))
                )
        );

        /* blob not an owner gets assigned an average of 15 quares to own */
        RolePlayerTypeStrategy blobOwnerSquare = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 12, 4),
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "ownership-of-square", "owner")
                )
        );
        RolePlayerTypeStrategy squareOwnedBlob = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-square",
                        new FixedUniform(this.random, 30, 50),
                        new HashSet<>(Arrays.asList(blobOwnerSquare, squareOwnedBlob))
                )
        );

        /* circle  owns  circle */
        RolePlayerTypeStrategy circleOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 15, 5),
                        new NotInRelationshipConceptIdProvider(random, storage, "circle", "ownership-of-circle", "owner")
                )
        );
        RolePlayerTypeStrategy circleOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-circle",
                        new FixedUniform(this.random, 35, 55),
                        new HashSet<>(Arrays.asList(circleOwner, circleOwned))
                )
        );

        /* circle  owns  blob */
        RolePlayerTypeStrategy circleOwnerBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 4, 1),
                        new NotInRelationshipConceptIdProvider(random, storage, "circle", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwnedCircle = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 12, 20),
                        new HashSet<>(Arrays.asList(blobOwnedCircle, circleOwnerBlob))
                )
        );

        /* square  owns  circle */
        RolePlayerTypeStrategy squareOwnsCircle = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 8, 3),
                        new NotInRelationshipConceptIdProvider(random, storage, "square", "ownership-of-circle", "owner")
                )
        );
        RolePlayerTypeStrategy circleOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-circle",
                        new FixedUniform(this.random, 12, 26),
                        new HashSet<>(Arrays.asList(circleOwnedSquare, squareOwnsCircle))
                )
        );

        /* square  owns  blob*/
        RolePlayerTypeStrategy squareOwnsBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        new FixedDiscreteGaussian(random, 18, 10),
                        new NotInRelationshipConceptIdProvider(random, storage, "square", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 2, 40),
                        new HashSet<>(Arrays.asList(blobOwnedSquare, squareOwnsBlob))
                )
        );


        // -------------    sizing     ----------------

        // blob  sizing  blob
        RolePlayerTypeStrategy blobBigger = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO,
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "sizing-blob", "bigger")
                )
        );
        RolePlayerTypeStrategy blobSmaller = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing-blob",
                        new FixedUniform(this.random, 5, 14),
                        new HashSet<>(Arrays.asList(blobBigger, blobSmaller))
                )
        );


        // blob  sizing  circle
        RolePlayerTypeStrategy blobBiggerCircle = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO,
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "sizing-circle", "bigger")
                )
        );
        RolePlayerTypeStrategy circleSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing-circle",
                        new FixedUniform(this.random, 4, 10),
                        new HashSet<>(Arrays.asList(blobBiggerCircle, circleSmallerBlob))
                )
        );


        // blob  sizing  square
        RolePlayerTypeStrategy blobBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO,
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "sizing-square", "bigger")
                )
        );
        RolePlayerTypeStrategy squareSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing-square",
                        new FixedUniform(this.random, 3, 8),
                        new HashSet<>(Arrays.asList(blobBiggerSquare, squareSmallerBlob))
                )
        );

        // circle  sizing  square
        RolePlayerTypeStrategy circleBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "circle", "sizing-square", "bigger")
                )

        );
        RolePlayerTypeStrategy squareSmallerCircle = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing-square",
                        new FixedUniform(this.random, 2, 6),
                        new HashSet<>(Arrays.asList(circleBiggerSquare, squareSmallerCircle))
                )
        );

        // square  sizing  blob
        RolePlayerTypeStrategy squareBiggerBlob = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "square", "sizing-blob", "bigger")
                )
        );
        RolePlayerTypeStrategy blobSmallerSquare = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing-blob",
                        new FixedUniform(this.random, 1, 5),
                        new HashSet<>(Arrays.asList(squareBiggerBlob, blobSmallerSquare))
                )
        );
    }

    private void buildImplicitRelationshipStrategies() {

        // assign randomly values to blobs that don't have values
        RolePlayerTypeStrategy blobValueOwner = new RolePlayerTypeStrategy("@has-blob-value-owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "blob", "@has-blob-value", "@has-blob-value-owner")
                )
        );
        RolePlayerTypeStrategy blobValueValue = new RolePlayerTypeStrategy("@has-blob-value-value",
                new FixedConstant(1),

                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "blob-value", "@has-blob-value", "@has-blob-value-value")
                )
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-blob-value",
                        new FixedUniform(random, 10, 50),
                        new HashSet<>(Arrays.asList(blobValueOwner, blobValueValue))
                )
        );

        // assign squares values that don't have values
        RolePlayerTypeStrategy squareValueOwner = new RolePlayerTypeStrategy("@has-square-value-owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "square", "@has-square-value", "@has-square-value-owner")
                )
        );
        RolePlayerTypeStrategy squareValueValue = new RolePlayerTypeStrategy("@has-square-value-value",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "square-value", "@has-square-value", "@has-square-value-value")
                )
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-square-value",
                        new FixedUniform(this.random, 20, 65),
                        new HashSet<>(Arrays.asList(squareValueOwner, squareValueValue))
                )
        );

        // assign circles values that don't have values
        RolePlayerTypeStrategy circleValueOwner = new RolePlayerTypeStrategy("@has-square-value-owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "circle", "@has-circle-value", "@has-square-value-owner")
                )
        );
        RolePlayerTypeStrategy circleValueValue = new RolePlayerTypeStrategy("@has-value-value",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "circle-value", "@has-circle-value", "@has-circle-value-value")
                )
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-circle-value",
                        new FixedUniform(random, 25, 70),
                        new HashSet<>(Arrays.asList(circleValueOwner, circleValueValue))
                )
        );

        // assign interactions interaction-value that don't have a value
        RolePlayerTypeStrategy interactionValueOwner = new RolePlayerTypeStrategy("@has-interaction-value-owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "interaction", "@has-interaction-value", "@has-interaction-value-owner")
                )
        );
        RolePlayerTypeStrategy interactionValueValue = new RolePlayerTypeStrategy("@has-interaction-value-value",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "interaction-value", "@has-interaction-value", "@has-interaction-value-value")
                )
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-interaction-value",
                        new FixedDiscreteGaussian(random, 7, 3),
                        new HashSet<>(Arrays.asList(interactionValueOwner, interactionValueValue))
                )
        );

        // assign a cost to an ownership-cost
        RolePlayerTypeStrategy ownershipCostOwner = new RolePlayerTypeStrategy("@has-ownership-cost-owner",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "ownership", "@has-ownership-cost", "@has-ownership-cost-owner")
                )
        );
        RolePlayerTypeStrategy ownershipCostValue = new RolePlayerTypeStrategy("@has-ownership-cost-value",
                new FixedConstant(1),
                new CentralConceptProvider(
                        // TODO
                        null,
                        new NotInRelationshipConceptIdProvider(random, storage, "ownership-cost", "@has-ownership-cost", "@has-ownership-cost-value")
                )
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-ownership-cost",
                        new FixedDiscreteGaussian(random, 5, 1),
                        new HashSet<>(Arrays.asList(ownershipCostOwner, ownershipCostValue))
                )
        );
    }


    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
