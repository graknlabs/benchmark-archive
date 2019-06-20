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
import grakn.benchmark.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.generator.provider.key.ConceptKeyProvider;
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

public class BiochemicalNetworkDefinition implements DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public BiochemicalNetworkDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;
        buildDefinition();
    }

    private void buildDefinition() {

        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);

        CountingKeyProvider globalUniqueKeyProvider = new CountingKeyProvider(0);

        buildEntityStrategies(globalUniqueKeyProvider);
        buildAttributeStrategies(globalUniqueKeyProvider);
        buildExplicitRelationshipStrategies(globalUniqueKeyProvider);
        buildImplicitRelationshipStrategies(globalUniqueKeyProvider);

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.5, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies(CountingKeyProvider globalKeyProvider) {

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "chemical",
                        new FixedDiscreteGaussian(this.random, 11, 5),
                        globalKeyProvider)
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "enzyme",
                        new FixedDiscreteGaussian(this.random, 5, 0.8),
                        globalKeyProvider
                )
        );

    }

    private void buildAttributeStrategies(CountingKeyProvider globalKeyProvider) {

        UniqueIntegerProvider idGenerator = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "biochem-id",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        globalKeyProvider,
                        idGenerator
                )
        );


    }

    private void buildExplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {

        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy agentRolePlayer = new RolePlayerTypeStrategy(
                "agent",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.005, 0.003),
                new ConceptKeyStorageProvider(
                        random,
                        this.storage,
                        "chemical")
        );
        RolePlayerTypeStrategy catalystRolePlayer = new RolePlayerTypeStrategy(
                "catalyst",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.001, 0.001),
                new ConceptKeyStorageProvider(
                        random,
                        this.storage,
                        "enzyme")
        );
        this.relationshipStrategies.add(
                2.7,
                new RelationStrategy(
                        "interaction",
                        new FixedDiscreteGaussian(this.random, 50, 25),
                        globalKeyProvider,
                        Arrays.asList(agentRolePlayer, catalystRolePlayer)
                )
        );
    }

    private void buildImplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {


        // @has-biochem-id for chemicals
        RolePlayerTypeStrategy chemicalIdOwner = new RolePlayerTypeStrategy(
                "@has-biochem-id-owner",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(
                        random,
                        storage,
                        "chemical"
                )
        );
        RolePlayerTypeStrategy chemicalIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                new FixedConstant(1),
//                new NotInRelationshipConceptKeyProvider(
                new ConceptKeyStorageProvider(
                        random,
                        storage,
                        "biochem-id"
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "@has-biochem-id",
                        // start with a constant bump to the graph size to accelerate the initial growth of the number of
                        // attribute ownership connections
                        new ScalingDiscreteGaussian(random, () -> 1500 + storage.getGraphScale(), 0.01, 0.005),
                        globalKeyProvider,
                        Arrays.asList(chemicalIdOwner, chemicalIdValue)
                )
        );


        // @has-biochem-id for enzymes
        RolePlayerTypeStrategy enzymeIdOwner = new RolePlayerTypeStrategy(
                "@has-biochem-id-owner",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(
                        random,
                        storage,
                        "enzyme"
                )
        );
        RolePlayerTypeStrategy enzymeIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(
                        random,
                        storage,
                        "biochem-id"
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "@has-biochem-id",
                        // start with a constant bump to the graph size to accelerate the initial growth of the number of
                        // attribute ownership connections
                        new ScalingDiscreteGaussian(random, () -> 1500 + storage.getGraphScale(), 0.01, 0.005),
                        globalKeyProvider,
                        Arrays.asList(enzymeIdOwner, enzymeIdValue)
                )
        );
    }

    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
