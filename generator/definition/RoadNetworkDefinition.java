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
import grakn.benchmark.generator.probdensity.FixedUniform;
import grakn.benchmark.generator.provider.key.CentralConceptKeyProvider;
import grakn.benchmark.generator.provider.key.ConceptKeyProvider;
import grakn.benchmark.generator.provider.key.ConceptKeyStorageProvider;
import grakn.benchmark.generator.provider.key.CountingKeyProvider;
import grakn.benchmark.generator.provider.key.NotInRelationshipConceptKeyProvider;
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

public class RoadNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public RoadNetworkDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;
        buildGenerator();
    }

    private void buildGenerator() {
        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);

        ConceptKeyProvider globalKeyProvider = new CountingKeyProvider(0);

        buildEntityStrategies(globalKeyProvider);
        buildAttributeStrategies(globalKeyProvider);
        buildExplicitRelationshipStrategies(globalKeyProvider);
        buildImplicitRelationshipStrategies(globalKeyProvider);

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.25, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies(ConceptKeyProvider globalKeyProvider) {
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "road",
                        new FixedUniform(this.random, 10, 40),
                        globalKeyProvider
                )
        );

    }

    private void buildAttributeStrategies(ConceptKeyProvider globalKeyProvider) {
        RandomStringProvider nameIterator = new RandomStringProvider(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedUniform(this.random, 10, 30),
                        globalKeyProvider,
                        nameIterator
                )
        );

    }

    private void buildExplicitRelationshipStrategies(ConceptKeyProvider globalKeyProvider) {
        RolePlayerTypeStrategy unusedEndpointRoads = new RolePlayerTypeStrategy(
                "endpoint",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedUniform(random, 10, 40), // choose 10-40 roads not in relationships
                        new NotInRelationshipConceptKeyProvider(
                                random,
                                storage,
                                "road",
                                "intersection",
                                "endpoint"
                        )
                )
        );
        RolePlayerTypeStrategy anyEndpointRoads = new RolePlayerTypeStrategy(
                "endpoint",
                new FixedUniform(random, 1, 5), // choose 1-5 other role players for an intersection
                new ConceptKeyStorageProvider(random, storage, "road")
        );

        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "intersection",
                        new FixedUniform(random, 20, 100),
                        globalKeyProvider,
                        Arrays.asList(unusedEndpointRoads, anyEndpointRoads)
                )
        );

    }

    private void buildImplicitRelationshipStrategies(ConceptKeyProvider globalKeyProvider) {
        // @has-name
        // find some roads that do not have a name and connect them
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(
                        random,
                        storage,
                        "road",
                        "@has-name",
                        "@has-name-owner"
                )
        );
        // find some names not used and repeatedly connect a small set/one of them to the roads without names
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                new FixedConstant(1),
                new CentralConceptKeyProvider(
                        new FixedConstant(60), // take unused names
                        new NotInRelationshipConceptKeyProvider(
                                random,
                                storage,
                                "name",
                                "@has-name",
                                "@has-name-value"
                        )
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "@has-name",
                        new FixedConstant(60),
                        globalKeyProvider,
                        Arrays.asList(nameOwner, nameValue)
                )
        );
    }

    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }

}
