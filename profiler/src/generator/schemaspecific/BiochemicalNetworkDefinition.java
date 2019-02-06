package grakn.benchmark.profiler.generator.schemaspecific;

import grakn.benchmark.profiler.generator.pick.CountingStreamGenerator;
import grakn.benchmark.profiler.generator.pick.StreamProvider;
import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.profiler.generator.storage.ConceptStore;
import grakn.benchmark.profiler.generator.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.profiler.generator.storage.NotInRelationshipConceptIdPicker;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.profiler.generator.strategy.RouletteWheel;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class BiochemicalNetworkDefinition extends DataGeneratorDefinition {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategy> entityStrategies;
    private RouletteWheel<TypeStrategy> relationshipStrategies;
    private RouletteWheel<TypeStrategy> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategy>> metaTypeStrategies;

    public BiochemicalNetworkDefinition(Random random, ConceptStore storage) {
        this.random = random;
        this.storage = storage;


        this.entityStrategies = new RouletteWheel<>(random);
        this.relationshipStrategies = new RouletteWheel<>(random);
        this.attributeStrategies = new RouletteWheel<>(random);
        this.metaTypeStrategies = new RouletteWheel<>(random);

        buildGenerator();
    }

    private void buildGenerator() {
        buildStrategies();
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildStrategies() {

        /*
        Entities
         */

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "chemical",
                        new FixedDiscreteGaussian(this.random, 8, 4))
        );

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "enzyme",
                        new FixedDiscreteGaussian(this.random, 2, 0.5)
                )
        );

        /*
        Attributes
         */

        CountingStreamGenerator idGenerator = new CountingStreamGenerator(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "biochem-id",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        new StreamProvider<>(idGenerator)
                )
        );


        /*
        Relationships
         */


        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy agentRolePlayer = new RolePlayerTypeStrategy(
                "agent",
                "interaction",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.005),
                new StreamProvider<>(
                        new FromIdStorageConceptIdPicker(
                                random,
                                this.storage,
                                "chemical")
                )
        );
        RolePlayerTypeStrategy catalystRolePlayer = new RolePlayerTypeStrategy(
                "catalyst",
                "interaction",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.001, 0.001),
                new StreamProvider<>(
                        new FromIdStorageConceptIdPicker(
                                random,
                                this.storage,
                                "enzyme")
                )
        );
        this.relationshipStrategies.add(
                3.0,
                new RelationshipStrategy(
                        "interaction",
                        new FixedDiscreteGaussian(this.random, 50, 25),
                        new HashSet<>(Arrays.asList(agentRolePlayer, catalystRolePlayer))
                )
        );


        // @has-biochem-id for chemicals
        RolePlayerTypeStrategy chemicalIdOwner = new RolePlayerTypeStrategy(
                "@has-biochem-id-owner",
                "@has-biochem-id",
                new FixedConstant(1),
                new StreamProvider<>(
                        new NotInRelationshipConceptIdPicker(
                                random,
                                storage,
                                "chemical", "@has-biochem-id", "@has-biochem-id-owner"
                        )
                )
        );
        RolePlayerTypeStrategy chemicalIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                "@has-biochem-id",
                new FixedConstant(1),
                new StreamProvider<>(
                        new NotInRelationshipConceptIdPicker(
                                random,
                                storage,
                                "biochem-id", "@has-biochem-id", "@has-biochem-id-value"
                        )
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-biochem-id",
                        new FixedDiscreteGaussian(random, 20, 5), // more than number of entities being created to compensate for being picked less
                        new HashSet<>(Arrays.asList(chemicalIdOwner, chemicalIdValue))
                )
        );


        // @has-biochem-id for enzymes
        RolePlayerTypeStrategy enzymeIdOwner = new RolePlayerTypeStrategy(
                "@has-biochem-id-owner",
                "@has-biochem-id",
                new FixedConstant(1),
                new StreamProvider<>(
                        new NotInRelationshipConceptIdPicker(
                                random,
                                storage,
                                "enzyme", "@has-biochem-id", "@has-biochem-id-owner"
                        )
                )
        );
        RolePlayerTypeStrategy enzymeIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                "@has-biochem-id",
                new FixedConstant(1),
                new StreamProvider<>(
                        new NotInRelationshipConceptIdPicker(
                                random,
                                storage,
                                "biochem-id", "@has-biochem-id", "@has-biochem-id-value"
                        )
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-biochem-id",
                        new FixedDiscreteGaussian(random, 20, 5), // more than number of entities being created to compensate for being picked less
                        new HashSet<>(Arrays.asList(enzymeIdOwner, enzymeIdValue))
                )
        );
    }

    @Override
    protected RouletteWheel<RouletteWheel<TypeStrategy>> getDefinition() {
        return this.metaTypeStrategies;
    }
}
