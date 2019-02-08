package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.provider.UniqueIntegerProvider;
import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.profiler.generator.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.provider.ConceptIdStorageProvider;
import grakn.benchmark.profiler.generator.provider.NotInRelationshipConceptIdProvider;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.profiler.generator.util.WeightedPicker;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;

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

        buildEntityStrategies();
        buildAttributeStrategies();
        buildExplicitRelationshipStrategies();
        buildImplicitRelationshipStrategies();

        this.metaTypeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.0, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
    }

    private void buildEntityStrategies() {

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

    }

    private void buildAttributeStrategies() {

        UniqueIntegerProvider idGenerator = new UniqueIntegerProvider(0);
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "biochem-id",
                        new FixedDiscreteGaussian(this.random, 5, 3),
                        idGenerator
                )
        );


    }

    private void buildExplicitRelationshipStrategies() {

        // increasingly large interactions (increasing number of role players)
        RolePlayerTypeStrategy agentRolePlayer = new RolePlayerTypeStrategy(
                "agent",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.01, 0.005),
                new ConceptIdStorageProvider(
                        random,
                        this.storage,
                        "chemical")
        );
        RolePlayerTypeStrategy catalystRolePlayer = new RolePlayerTypeStrategy(
                "catalyst",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> storage.getGraphScale(), 0.001, 0.001),
                new ConceptIdStorageProvider(
                        random,
                        this.storage,
                        "enzyme")
        );
        this.relationshipStrategies.add(
                3.0,
                new RelationshipStrategy(
                        "interaction",
                        new FixedDiscreteGaussian(this.random, 50, 25),
                        new HashSet<>(Arrays.asList(agentRolePlayer, catalystRolePlayer))
                )
        );
    }

    private void buildImplicitRelationshipStrategies() {


        // @has-biochem-id for chemicals
        RolePlayerTypeStrategy chemicalIdOwner = new RolePlayerTypeStrategy(
                "@has-biochem-id-owner",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "chemical", "@has-biochem-id", "@has-biochem-id-owner"
                )
        );
        RolePlayerTypeStrategy chemicalIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "biochem-id", "@has-biochem-id", "@has-biochem-id-value"
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
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "enzyme", "@has-biochem-id", "@has-biochem-id-owner"
                )
        );
        RolePlayerTypeStrategy enzymeIdValue = new RolePlayerTypeStrategy(
                "@has-biochem-id-value",
                new FixedConstant(1),
                new NotInRelationshipConceptIdProvider(
                        random,
                        storage,
                        "biochem-id", "@has-biochem-id", "@has-biochem-id-value"
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
    public TypeStrategy sampleNextStrategy() {
        return this.metaTypeStrategies.sample().sample();
    }
}
