package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.pick.CountingStreamGenerator;
import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.probdensity.FixedConstant;
import grakn.benchmark.runner.probdensity.FixedDiscreteGaussian;
import grakn.benchmark.runner.probdensity.ScalingDiscreteGaussian;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.runner.storage.IdStoreInterface;
import grakn.benchmark.runner.storage.NotInRelationshipConceptIdPicker;
import grakn.benchmark.runner.strategy.*;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class BiochemNetworkGenerator implements SchemaSpecificDataGenerator {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public BiochemNetworkGenerator(Random random, ConceptStore storage) {
        this.random = random;
        this.storage = storage;


        this.entityStrategies = new RouletteWheel<>(random);
        this.relationshipStrategies = new RouletteWheel<>(random);
        this.attributeStrategies = new RouletteWheel<>(random);
        this.operationStrategies = new RouletteWheel<>(random);

        buildGenerator();
    }

    private void buildGenerator() {
        buildStrategies();
        this.operationStrategies.add(1.0, entityStrategies);
        this.operationStrategies.add(1.0, relationshipStrategies);
        this.operationStrategies.add(1.0, attributeStrategies);
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
                        new FixedDiscreteGaussian(this.random,5,3),
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
                new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.01, 0.005),
                new StreamProvider<>(
                        new FromIdStorageConceptIdPicker(
                                random,
                                (IdStoreInterface) this.storage,
                                "chemical")
                )
        );
        RolePlayerTypeStrategy catalystRolePlayer = new RolePlayerTypeStrategy(
                "catalyst",
                "interaction",
                // high variance in the number of role players
                new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.001, 0.001),
                new StreamProvider<>(
                        new FromIdStorageConceptIdPicker(
                                random,
                                (IdStoreInterface) this.storage,
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
                                (IdStoreInterface) storage,
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
                                (IdStoreInterface) storage,
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
                                (IdStoreInterface) storage,
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
                                (IdStoreInterface) storage,
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
    public RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy() {
        return this.operationStrategies;
    }

    @Override
    public ConceptStore getConceptStore() {
        return this.storage;
    }
}
