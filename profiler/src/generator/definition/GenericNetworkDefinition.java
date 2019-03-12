package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.FixedUniform;
import grakn.benchmark.profiler.generator.provider.concept.ConceptIdStorageProvider;
import grakn.benchmark.profiler.generator.provider.concept.NotInRelationshipConceptIdProvider;
import grakn.benchmark.profiler.generator.provider.value.RepeatingIntegerProvider;
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

public class GenericNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public GenericNetworkDefinition(Random random, ConceptStorage storage) {
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
        this.metaTypeStrategies.add(3.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }


    private void buildEntityStrategies() {
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "blob",
                        new FixedUniform(this.random, 10, 30))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "square",
                        new FixedUniform(this.random, 20, 40))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "circle",
                        new FixedUniform(this.random, 30, 50))
        );

    }

    private void buildAttributeStrategies() {
        // values are repeated some numerb of times (mean 20 times)
        RepeatingIntegerProvider valueGenerator = new RepeatingIntegerProvider(0, new FixedDiscreteGaussian(random, 20, 5));
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "value",
                        new FixedDiscreteGaussian(this.random, 50, 15),
                        valueGenerator
                )
        );

        // interaction values are unique
        UniqueIntegerProvider interactionValueGenerator = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "interaction-value",
                        // TODO adjust values
                        new FixedUniform(this.random, 3, 9),
                        interactionValueGenerator
                )
        );

        // costs are repeated (mean 5 times)
        RepeatingIntegerProvider costGenerator = new RepeatingIntegerProvider(0, new FixedDiscreteGaussian(random, 5, 1));
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "ownership-cost",
                        // TODO adjust values
                        new FixedUniform(this.random, 2, 8),
                        costGenerator
                )
        );
    }

    private void buildExplicitRelationshipStrategies() {


        // -------------    interactions    ---------------

        /* _interaction_ with 2-12 role players */
        RolePlayerTypeStrategy blobberRolePlayer = new RolePlayerTypeStrategy( "blobber",
                new FixedUniform(random, 0, 4),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );

        RolePlayerTypeStrategy circlerRolePlayer = new RolePlayerTypeStrategy("circler",
                new FixedUniform(random, 1,7),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );

        RolePlayerTypeStrategy squarerRolePlayer = new RolePlayerTypeStrategy("squarer",
                new FixedUniform(random, 1,11),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy( "interaction",
                        new FixedUniform(this.random, 25, 75),
                        new HashSet<>(Arrays.asList(blobberRolePlayer, circlerRolePlayer, squarerRolePlayer))
                )
        );


        // -------------    ownership    ---------------

        /* blob  owns  blobs */
        RolePlayerTypeStrategy blobOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy blobOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 5, 15),
                        new HashSet<>(Arrays.asList(blobOwner, blobOwned))
                )
        );

        /* blob  owns  square */
        RolePlayerTypeStrategy blobOwnerSquare = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy squareOwnedBlob = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 5, 15),
                        new HashSet<>(Arrays.asList(blobOwnerSquare, squareOwnedBlob))
                )
        );

        /* circle  owns  circle */
        RolePlayerTypeStrategy circleOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        RolePlayerTypeStrategy circleOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 5, 15),
                        new HashSet<>(Arrays.asList(circleOwner, circleOwned))
                )
        );

        /* circle  owns  blob */
        RolePlayerTypeStrategy blobOwnedCircle= new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy circleOwnerBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 3, 14),
                        new HashSet<>(Arrays.asList(blobOwnedCircle, circleOwnerBlob))
                )
        );

        /* square  owns  circle */
        RolePlayerTypeStrategy circleOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        RolePlayerTypeStrategy squareOwnsCircle = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 2, 10),
                        new HashSet<>(Arrays.asList(circleOwnedSquare, squareOwnsCircle))
                )
        );

        /* square  owns  blob*/
        RolePlayerTypeStrategy blobOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy squareOwnsBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("ownership",
                        new FixedUniform(this.random, 2, 10),
                        new HashSet<>(Arrays.asList(blobOwnedSquare, squareOwnsBlob))
                )
        );



        // -------------    sizing     ----------------

        // blob  sizing  blob
        RolePlayerTypeStrategy blobBigger = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy blobSmaller = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing",
                        new FixedUniform(this.random, 3, 14),
                        new HashSet<>(Arrays.asList(blobBigger, blobSmaller))
                )
        );


        // blob  sizing  circle
        RolePlayerTypeStrategy blobBiggerCircle = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy circleSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing",
                        new FixedUniform(this.random, 2, 10),
                        new HashSet<>(Arrays.asList(blobBiggerCircle, circleSmallerBlob))
                )
        );


        // blob  sizing  square
        RolePlayerTypeStrategy blobBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        RolePlayerTypeStrategy squareSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing",
                        new FixedUniform(this.random, 2, 8),
                        new HashSet<>(Arrays.asList(blobBiggerSquare, squareSmallerBlob))
                )
        );

        // circle  sizing  square
        RolePlayerTypeStrategy circleBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "circle")
        );
        RolePlayerTypeStrategy squareSmallerCircle = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing",
                        new FixedUniform(this.random, 2, 6),
                        new HashSet<>(Arrays.asList(circleBiggerSquare, squareSmallerCircle))
                )
        );

        // square  sizing  blob
        RolePlayerTypeStrategy squareBiggerBlob = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "square")
        );
        RolePlayerTypeStrategy blobSmallerSquare = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "blob")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("sizing",
                        new FixedUniform(this.random, 1, 5),
                        new HashSet<>(Arrays.asList(squareBiggerBlob, blobSmallerSquare))
                )
        );
    }

    private void buildImplicitRelationshipStrategies() {

        // assign randomly values to blobs that don't have values
        RolePlayerTypeStrategy blobValueOwner = new RolePlayerTypeStrategy("@has-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(random, storage, "blob", "@has-value", "@has-value-owner")
        );
        RolePlayerTypeStrategy blobValueValue = new RolePlayerTypeStrategy("@has-value-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "value")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-value",
                        new FixedUniform(this.random, 10, 30),
                        new HashSet<>(Arrays.asList(blobValueOwner, blobValueValue))
                )
        );

        // assign squares values that don't have values
        RolePlayerTypeStrategy squareValueOwner = new RolePlayerTypeStrategy("@has-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(random, storage, "square", "@has-value", "@has-value-owner")
        );
        RolePlayerTypeStrategy squareValueValue = new RolePlayerTypeStrategy("@has-value-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "value")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-value",
                        new FixedUniform(this.random, 20, 40),
                        new HashSet<>(Arrays.asList(squareValueOwner, squareValueValue))
                )
        );

        // assign circles values that don't have values
        RolePlayerTypeStrategy circleValueOwner = new RolePlayerTypeStrategy("@has-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(random, storage, "circle", "@has-value", "@has-value-owner")
        );
        RolePlayerTypeStrategy circleValueValue = new RolePlayerTypeStrategy("@has-value-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "value")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-value",
                        new FixedUniform(this.random, 30, 50),
                        new HashSet<>(Arrays.asList(circleValueOwner, circleValueValue))
                )
        );

        // assign interactions interaction-value that don't have a value
        RolePlayerTypeStrategy interactionValueOwner = new RolePlayerTypeStrategy("@has-interaction-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(random, storage, "interaction", "@has-interaction-value", "@has-interaction-value-owner")
        );
        RolePlayerTypeStrategy interactionValueValue = new RolePlayerTypeStrategy("@has-interaction-value-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "interaction-value")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-interaction-value",
                        new FixedDiscreteGaussian(this.random, 25, 8),
                        new HashSet<>(Arrays.asList(interactionValueOwner, interactionValueValue))
                )
        );

        // assign a cost to an ownership-cost
        RolePlayerTypeStrategy ownershipCostOwner = new RolePlayerTypeStrategy("@has-ownership-cost-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(random, storage, "ownership", "@has-ownership-cost", "@has-ownership-cost-owner")
        );
        RolePlayerTypeStrategy ownershipCostValue = new RolePlayerTypeStrategy("@has-ownership-cost-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, this.storage, "ownership-cost")
        );
        this.relationshipStrategies.add(1.0,
                new RelationStrategy("@has-ownership-cost",
                        new FixedDiscreteGaussian(this.random, 5, 2),
                        new HashSet<>(Arrays.asList(ownershipCostOwner, ownershipCostValue))
                )
        );
    }


    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
