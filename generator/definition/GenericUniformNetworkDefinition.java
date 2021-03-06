/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.generator.definition;


import grakn.benchmark.generator.probdensity.FixedConstant;
import grakn.benchmark.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.generator.probdensity.FixedUniform;
import grakn.benchmark.generator.provider.key.CentralConceptKeyProvider;
import grakn.benchmark.generator.provider.key.ConceptKeyStorageProvider;
import grakn.benchmark.generator.provider.key.CountingKeyProvider;
import grakn.benchmark.generator.provider.key.NotInRelationshipConceptKeyProvider;
import grakn.benchmark.generator.provider.value.UniqueIntegerProvider;
import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.benchmark.generator.strategy.AttributeStrategy;
import grakn.benchmark.generator.strategy.EntityStrategy;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.generator.strategy.TypeStrategy;
import grakn.benchmark.generator.util.WeightedPicker;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class GenericUniformNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> explicitRelationshipStrategies;
    private WeightedPicker<TypeStrategy> implicitRelationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public GenericUniformNetworkDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;
        buildDefinition();
    }

    private void buildDefinition() {

        entityStrategies = new WeightedPicker<>(random);
        explicitRelationshipStrategies = new WeightedPicker<>(random);
        implicitRelationshipStrategies = new WeightedPicker<>(random);
        attributeStrategies = new WeightedPicker<>(random);

        CountingKeyProvider globalUniqueKeyProvider = new CountingKeyProvider(0);

        buildEntityStrategies(globalUniqueKeyProvider);
        buildAttributeStrategies(globalUniqueKeyProvider);
        buildExplicitRelationshipStrategies(globalUniqueKeyProvider);
        buildImplicitRelationshipStrategies(globalUniqueKeyProvider);

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(8.0, explicitRelationshipStrategies);
        this.metaTypeStrategies.add(8.0/3, implicitRelationshipStrategies);
        this.metaTypeStrategies.add(5.0/3, attributeStrategies);
    }


    private void buildEntityStrategies(CountingKeyProvider globalKeyProvider) {
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "blob",
                        new FixedUniform(this.random, 8, 18),
                        globalKeyProvider)
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "square",
                        new FixedUniform(this.random, 11, 21),
                        globalKeyProvider)
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "circle",
                        new FixedUniform(this.random, 16, 26),
                        globalKeyProvider)
        );

    }

    private void buildAttributeStrategies(CountingKeyProvider globalKeyProvider) {
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "blob-value",
                        new FixedUniform(this.random, 1, 5),
                        globalKeyProvider,
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "square-value",
                        new FixedUniform(this.random, 3, 11),
                        globalKeyProvider,
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "circle-value",
                        new FixedUniform(this.random, 1, 3),
                        globalKeyProvider,
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "interaction-value",
                        new FixedUniform(this.random, 2, 18),
                        globalKeyProvider,
                        new UniqueIntegerProvider(0)
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "ownership-cost",
                        new FixedUniform(this.random, 3, 15),
                        globalKeyProvider,
                        new UniqueIntegerProvider(0)
                )
        );
    }

    private void buildExplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {


        // -------------    interactions    ---------------

        /* _interaction_ with 2-12 role players */
        RolePlayerTypeStrategy blobberRolePlayer = new RolePlayerTypeStrategy("blobber",
                new FixedUniform(random, 0, 4),
                new ConceptKeyStorageProvider(random, storage, "blob")
        );
        RolePlayerTypeStrategy squarerRolePlayer = new RolePlayerTypeStrategy("squarer",
                new FixedUniform(random, 0, 7),
                new ConceptKeyStorageProvider(random, storage, "square")
        );
        RolePlayerTypeStrategy circlerRolePlayer = new RolePlayerTypeStrategy("circler",
                new FixedUniform(random, 0, 11),
                new ConceptKeyStorageProvider(random, storage, "circle")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("interaction",
                        new FixedUniform(this.random, 45, 75),
                        globalKeyProvider,
                        Arrays.asList(blobberRolePlayer, circlerRolePlayer, squarerRolePlayer)
                )
        );

        // -------------    ownership    ---------------

        /* blob not an owner gets assigned an average of 10 blobs to own */
        RolePlayerTypeStrategy blobOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 8, 3),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, this.storage, "blob")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 10, 20),
                        globalKeyProvider,
                        Arrays.asList(blobOwner, blobOwned)
                )
        );

        /* blob not an owner gets assigned an average of 15 squares to own */
        RolePlayerTypeStrategy blobOwnerSquare = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 12, 4),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob", "ownership-of-square", "owner")
                )
        );
        RolePlayerTypeStrategy squareOwnedBlob = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, this.storage, "square")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-square",
                        new FixedUniform(this.random, 20, 30),
                        globalKeyProvider,
                        Arrays.asList(blobOwnerSquare, squareOwnedBlob)
                )
        );

        /* circle  owns  circle */
        RolePlayerTypeStrategy circleOwner = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 15, 5),
                        new NotInRelationshipConceptKeyProvider(random, storage, "circle", "ownership-of-circle", "owner")
                )
        );
        RolePlayerTypeStrategy circleOwned = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "circle")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-circle",
                        new FixedUniform(this.random, 25, 35),
                        globalKeyProvider,
                        Arrays.asList(circleOwner, circleOwned)
                )
        );

        /* circle  owns  blob */
        RolePlayerTypeStrategy circleOwnerBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 4, 1),
                        new NotInRelationshipConceptKeyProvider(random, storage, "circle", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwnedCircle = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "blob")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 12, 20),
                        globalKeyProvider,
                        Arrays.asList(blobOwnedCircle, circleOwnerBlob)
                )
        );

        /* square  owns  circle */
        RolePlayerTypeStrategy squareOwnsCircle = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 8, 3),
                        new NotInRelationshipConceptKeyProvider(random, storage, "square", "ownership-of-circle", "owner")
                )
        );
        RolePlayerTypeStrategy circleOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "circle")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-circle",
                        new FixedUniform(this.random, 12, 26),
                        globalKeyProvider,
                        Arrays.asList(circleOwnedSquare, squareOwnsCircle)
                )
        );

        /* square  owns  blob*/
        RolePlayerTypeStrategy squareOwnsBlob = new RolePlayerTypeStrategy("owner",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 15, 10),
                        new NotInRelationshipConceptKeyProvider(random, storage, "square", "ownership-of-blob", "owner")
                )
        );
        RolePlayerTypeStrategy blobOwnedSquare = new RolePlayerTypeStrategy("owned",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "blob")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("ownership-of-blob",
                        new FixedUniform(this.random, 2, 30),
                        globalKeyProvider,
                        Arrays.asList(blobOwnedSquare, squareOwnsBlob)
                )
        );


        // -------------    sizing     ----------------

        // blob  sizing  blob
        RolePlayerTypeStrategy blobBigger = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 4, 2),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob", "sizing-blob", "bigger")
                )
        );
        RolePlayerTypeStrategy blobSmaller = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "blob")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("sizing-blob",
                        new FixedUniform(this.random, 5, 13),
                        globalKeyProvider,
                        Arrays.asList(blobBigger, blobSmaller)
                )
        );


        // blob  sizing  circle
        RolePlayerTypeStrategy blobBiggerCircle = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random,6, 2),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob", "sizing-circle", "bigger")
                )
        );
        RolePlayerTypeStrategy circleSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "circle")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("sizing-circle",
                        new FixedUniform(random, 4, 10),
                        globalKeyProvider,
                        Arrays.asList(blobBiggerCircle, circleSmallerBlob)
                )
        );


        // blob  sizing  square
        RolePlayerTypeStrategy blobBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 4, 1.5),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob", "sizing-square", "bigger")
                )
        );
        RolePlayerTypeStrategy squareSmallerBlob = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "square")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("sizing-square",
                        new FixedUniform(random, 3, 8),
                        globalKeyProvider,
                        Arrays.asList(blobBiggerSquare, squareSmallerBlob)
                )
        );

        // circle  sizing  square
        RolePlayerTypeStrategy circleBiggerSquare = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 3, 1),
                        new NotInRelationshipConceptKeyProvider(random, storage, "circle", "sizing-square", "bigger")
                )

        );
        RolePlayerTypeStrategy squareSmallerCircle = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "square")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("sizing-square",
                        new FixedUniform(random, 2, 6),
                        globalKeyProvider,
                        Arrays.asList(circleBiggerSquare, squareSmallerCircle)
                )
        );

        // square  sizing  blob
        RolePlayerTypeStrategy squareBiggerBlob = new RolePlayerTypeStrategy("bigger",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 2, 1),
                        new NotInRelationshipConceptKeyProvider(random, storage, "square", "sizing-blob", "bigger")
                )
        );
        RolePlayerTypeStrategy blobSmallerSquare = new RolePlayerTypeStrategy("smaller",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "blob")
        );
        this.explicitRelationshipStrategies.add(1.0,
                new RelationStrategy("sizing-blob",
                        new FixedUniform(this.random, 1, 5),
                        globalKeyProvider,
                        Arrays.asList(squareBiggerBlob, blobSmallerSquare)
                )
        );
    }

    private void buildImplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {

        // assign randomly values to blobs that don't have values
        RolePlayerTypeStrategy blobValueOwner = new RolePlayerTypeStrategy("@has-blob-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(random, storage, "blob", "@has-blob-value", "@has-blob-value-owner")
        );
        RolePlayerTypeStrategy blobValueValue = new RolePlayerTypeStrategy("@has-blob-value-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 3, 1),
                        new NotInRelationshipConceptKeyProvider(random, storage, "blob-value", "@has-blob-value", "@has-blob-value-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-blob-value",
                        new FixedUniform(random, 7, 18),
                        globalKeyProvider,
                        Arrays.asList(blobValueOwner, blobValueValue)
                )
        );

        // assign squares values that don't have values
        RolePlayerTypeStrategy squareValueOwner = new RolePlayerTypeStrategy("@has-square-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(random, storage, "square", "@has-square-value", "@has-square-value-owner")
        );
        RolePlayerTypeStrategy squareValueValue = new RolePlayerTypeStrategy("@has-square-value-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 7, 2),
                        new NotInRelationshipConceptKeyProvider(random, storage, "square-value", "@has-square-value", "@has-square-value-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-square-value",
                        new FixedUniform(this.random, 11, 20),
                        globalKeyProvider,
                        Arrays.asList(squareValueOwner, squareValueValue)
                )
        );

        // assign circles values that don't have values
        RolePlayerTypeStrategy circleValueOwner = new RolePlayerTypeStrategy("@has-circle-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(random, storage, "circle", "@has-circle-value", "@has-circle-value-owner")
        );
        RolePlayerTypeStrategy circleValueValue = new RolePlayerTypeStrategy("@has-circle-value-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 2, 0.5),
                        new NotInRelationshipConceptKeyProvider(random, storage, "circle-value", "@has-circle-value", "@has-circle-value-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-circle-value",
                        new FixedUniform(random, 16, 25),
                        globalKeyProvider,
                        Arrays.asList(circleValueOwner, circleValueValue)
                )
        );

        // assign interactions interaction-value that don't have a value
        RolePlayerTypeStrategy interactionValueOwner = new RolePlayerTypeStrategy("@has-interaction-value-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(random, storage, "interaction", "@has-interaction-value", "@has-interaction-value-owner")
        );
        RolePlayerTypeStrategy interactionValueValue = new RolePlayerTypeStrategy("@has-interaction-value-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 10, 4),
                        new NotInRelationshipConceptKeyProvider(random, storage, "interaction-value", "@has-interaction-value", "@has-interaction-value-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-interaction-value",
                        new FixedDiscreteGaussian(random, 40, 15),
                        globalKeyProvider,
                        Arrays.asList(interactionValueOwner, interactionValueValue)
                )
        );



        /* --- Ownership cost --- */
        RolePlayerTypeStrategy blobOwnershipCostOwner = new RolePlayerTypeStrategy("@has-ownership-cost-owner",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "ownership-of-blob")
        );
        RolePlayerTypeStrategy blobOwnershipCostValue = new RolePlayerTypeStrategy("@has-ownership-cost-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 9, 3),
                        new NotInRelationshipConceptKeyProvider(random, storage, "ownership-cost", "@has-ownership-cost", "@has-ownership-cost-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-ownership-cost",
                        new FixedDiscreteGaussian(random, 31, 10),
                        globalKeyProvider,
                        Arrays.asList(blobOwnershipCostValue, blobOwnershipCostOwner)
                )
        );


        RolePlayerTypeStrategy circleOwnershipCostOwner = new RolePlayerTypeStrategy("@has-ownership-cost-owner",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "ownership-of-circle")
        );
        RolePlayerTypeStrategy circleOwnershipCostValue = new RolePlayerTypeStrategy("@has-ownership-cost-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 9, 3),
                        new NotInRelationshipConceptKeyProvider(random, storage, "ownership-cost", "@has-ownership-cost", "@has-ownership-cost-value")
                )
        );
        implicitRelationshipStrategies.add(1.0,
                new RelationStrategy("@has-ownership-cost",
                        new FixedDiscreteGaussian(random, 32.5, 15),
                        globalKeyProvider,
                        Arrays.asList(circleOwnershipCostOwner, circleOwnershipCostValue)
                )
        );


        /*
        NOTE this one has double weight - so we can halve the number of central role players and relationships added at a time
         */

        RolePlayerTypeStrategy squareOwnershipCostOwner = new RolePlayerTypeStrategy("@has-ownership-cost-owner",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(random, storage, "ownership-of-square")
        );
        RolePlayerTypeStrategy squareOwnershipCostValue = new RolePlayerTypeStrategy("@has-ownership-cost-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedDiscreteGaussian(random, 4, 1),
                        new NotInRelationshipConceptKeyProvider(random, storage, "ownership-cost", "@has-ownership-cost", "@has-ownership-cost-value")
                )
        );
        implicitRelationshipStrategies.add(2.0,
                new RelationStrategy("@has-ownership-cost",
                        new FixedDiscreteGaussian(random, 8, 2),
                        globalKeyProvider,
                        Arrays.asList(squareOwnershipCostOwner, squareOwnershipCostValue)
                )
        );
    }


    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
