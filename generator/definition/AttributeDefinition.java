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

import grakn.benchmark.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.generator.provider.key.CountingKeyProvider;
import grakn.benchmark.generator.provider.value.RandomStringProvider;
import grakn.benchmark.generator.provider.value.ScalingGaussianDoubleProvider;
import grakn.benchmark.generator.provider.value.UniqueIntegerProvider;
import grakn.benchmark.generator.storage.ConceptStorage;
import grakn.benchmark.generator.strategy.AttributeStrategy;
import grakn.benchmark.generator.strategy.TypeStrategy;
import grakn.benchmark.generator.util.WeightedPicker;

import java.util.Random;

public class AttributeDefinition implements DataGeneratorDefinition {
    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public AttributeDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;
        buildDefinition();
    }

    private void buildDefinition() {
        this.attributeStrategies = new WeightedPicker<>(random);

        CountingKeyProvider globalUniqueKeyProvider = new CountingKeyProvider(0);
        buildAttributeStrategies(globalUniqueKeyProvider);

        metaTypeStrategies = new WeightedPicker<>(random);
        metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildAttributeStrategies(CountingKeyProvider globalKeyProvider) {
        RandomStringProvider nameProvider = new RandomStringProvider(random, 6);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedDiscreteGaussian(this.random, 10, 3),
                        globalKeyProvider,
                        nameProvider
                )
        );


        ScalingGaussianDoubleProvider doubleProvider = new ScalingGaussianDoubleProvider(1.0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "decimal",
                        new FixedDiscreteGaussian(this.random, 20, 5),
                        globalKeyProvider,
                        doubleProvider
                )
        );


//        ScalingGaussianIntegerProvider integerProvider = new ScalingGaussianIntegerProvider(1.0);
        UniqueIntegerProvider integerProvider = new UniqueIntegerProvider(-1000);
        this.attributeStrategies.add(
                2.0,
                new AttributeStrategy<>(
                        "anInteger",
                        new FixedDiscreteGaussian(this.random, 50, 20),
                        globalKeyProvider,
                        integerProvider
                )
        );

//        ScalingGaussianIntegerProvider smallIntegerProvider = new ScalingGaussianIntegerProvider(0.2);
        UniqueIntegerProvider smallIntegerProvider = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                2.0,
                new AttributeStrategy<>(
                        "anotherInteger",
                        new FixedDiscreteGaussian(this.random, 20, 10),
                        globalKeyProvider,
                        smallIntegerProvider
                )
        );


//        ScalingGaussianIntegerProvider biggerIntegerProvider = new ScalingGaussianIntegerProvider(2.0);
        UniqueIntegerProvider biggerIntegerProvider = new UniqueIntegerProvider(1000);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "thirdInteger",
                        new FixedDiscreteGaussian(this.random, 25, 10),
                        globalKeyProvider,
                        biggerIntegerProvider
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "fourthInteger",
                        new FixedDiscreteGaussian(this.random, 25, 10),
                        globalKeyProvider,
                        biggerIntegerProvider
                )
        );

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "fifthInteger",
                        new FixedDiscreteGaussian(this.random, 25, 10),
                        globalKeyProvider,
                        biggerIntegerProvider
                )
        );
    }


    @Override
    public TypeStrategy sampleNextStrategy() {
        return metaTypeStrategies.sample().sample();
    }
}
