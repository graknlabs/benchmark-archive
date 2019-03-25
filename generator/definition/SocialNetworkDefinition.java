/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.generator.definition;

import grakn.benchmark.generator.probdensity.FixedConstant;
import grakn.benchmark.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.generator.probdensity.ScalingBoundedZipf;
import grakn.benchmark.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.generator.provider.concept.ConceptIdStorageProvider;
import grakn.benchmark.generator.provider.value.RandomStringProvider;
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

public class SocialNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public SocialNetworkDefinition(Random random, ConceptStorage storage) {
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
        this.metaTypeStrategies.add(1.2, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies() {

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

    }

    private void buildAttributeStrategies() {
        RandomStringProvider nameStream = new RandomStringProvider(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedDiscreteGaussian(this.random, 18, 3),
                        nameStream
                )
        );
    }

    private void  buildExplicitRelationshipStrategies() {

        // friendship
        RolePlayerTypeStrategy friendRoleFiller = new RolePlayerTypeStrategy(
                "friend",
                new FixedConstant(2),
                new ConceptIdStorageProvider(
                        random,
                        this.storage,
                        "person")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "friendship",
                        new ScalingBoundedZipf(this.random, () -> storage.getGraphScale(), 0.5, 2.3),
                        new HashSet<>(Arrays.asList(friendRoleFiller))
                )
        );


        // `like` relationship
        RolePlayerTypeStrategy likedPageRole = new RolePlayerTypeStrategy(
                "liked",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "page")
        );
        RolePlayerTypeStrategy likerPersonRole = new RolePlayerTypeStrategy(
                "liker",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "person")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "likes",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.05, 0.001),
                        new HashSet<>(Arrays.asList(likedPageRole, likerPersonRole))
                )
        );

    }

    private void buildImplicitRelationshipStrategies() {
        // @has-name
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "person")
        );
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                new FixedConstant(1),
                new ConceptIdStorageProvider(random, storage, "name")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "@has-name",
                        new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.1, 0.03),
                        new HashSet<>(Arrays.asList(nameOwner, nameValue))
                )
        );
    }

    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
