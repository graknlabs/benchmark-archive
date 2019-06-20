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

        CountingKeyProvider globalUniqueKeyProvider = new CountingKeyProvider(0);

        buildEntityStrategies(globalUniqueKeyProvider);
        buildAttributeStrategies(globalUniqueKeyProvider);
        buildExplicitRelationshipStrategies(globalUniqueKeyProvider);
        buildImplicitRelationshipStrategies(globalUniqueKeyProvider);

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies(CountingKeyProvider globalKeyProvider) {
        // we use a scaling PDF rather than a fixed one for these entities
        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "trader",
                        new ScalingDiscreteGaussian(this.random, () -> storage.getGraphScale(), 0.02, 0.01),
                        globalKeyProvider
                )
        );

    }

    private void buildAttributeStrategies(CountingKeyProvider globalKeyProvider) {
        // fixed PDF for number of attributes added
        UniqueIntegerProvider idGenerator = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "quantity",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        globalKeyProvider,
                        idGenerator
                )
        );

    }

    private void buildExplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {

        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy transactorRolePlayer = new RolePlayerTypeStrategy(
                "transactor",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.005, 0.005),
                new ConceptKeyStorageProvider(
                        random,
                        this.storage,
                        "trader")
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationStrategy(
                        "transaction",
                        new FixedDiscreteGaussian(this.random, 50, 10), // but fixed number of rels added per iter
                        globalKeyProvider,
                        Arrays.asList(transactorRolePlayer)
                )
        );
    }

    private void buildImplicitRelationshipStrategies(CountingKeyProvider globalKeyProvider) {

        // @has-quantity on the transaction relationship (1 quantity per transaction)
        RolePlayerTypeStrategy quantityOwner = new RolePlayerTypeStrategy(
                "@has-quantity-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptKeyProvider(
                        random,
                        storage,
                        "transaction", "@has-quantity", "@has-quantity-owner"
                )
        );
        RolePlayerTypeStrategy quantityValue = new RolePlayerTypeStrategy(
                "@has-quantity-value",
                new FixedConstant(1),
                new ConceptKeyStorageProvider(
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
                        globalKeyProvider,
                        Arrays.asList(quantityOwner, quantityValue)
                )
        );

    }

    @Override
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }

}
