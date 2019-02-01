package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.pick.CentralStreamProvider;
import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.pick.StringStreamGenerator;
import grakn.benchmark.runner.probdensity.*;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.runner.storage.IdStoreInterface;
import grakn.benchmark.runner.storage.NotInRelationshipConceptIdPicker;
import grakn.benchmark.runner.strategy.*;
import grakn.core.concept.ConceptId;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class RoadNetworkGenerator implements SchemaSpecificDataGenerator {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public RoadNetworkGenerator(Random random, ConceptStore storage) {
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
        this.operationStrategies.add(1.25, relationshipStrategies);
        this.operationStrategies.add(1.0, attributeStrategies);
    }

    private void buildStrategies() {

        /*
        Entities
         */

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "road",
                        new FixedUniform(this.random, 10, 40)
                )
        );

        /*
        Attributes
         */

        StringStreamGenerator nameStream = new StringStreamGenerator(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedUniform(this.random,10, 30),
                        new StreamProvider<>(nameStream)
                )
        );


        /*
        Relationships
         */

        RolePlayerTypeStrategy unusedEndpointRoads = new RolePlayerTypeStrategy(
                "endpoint",
                "intersection",
                new FixedConstant(1),
                new CentralStreamProvider<>(
                    new FixedUniform(random, 10, 40), // choose 10-40 roads not in relationships
                    new NotInRelationshipConceptIdPicker(
                            random,
                            (IdStoreInterface) storage,
                           "road",
                           "intersection",
                           "endpoint"
                    )
                )
        );
        RolePlayerTypeStrategy anyEndpointRoads = new RolePlayerTypeStrategy(
                "endpoint",
                "intersection",
                new FixedUniform(random, 1, 5), // choose 1-5 other role players for an intersection
                new StreamProvider<>(
                        new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "road")
                )
        );

        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "intersection",
                        new FixedUniform(random, 20, 100),
                        new HashSet<>(Arrays.asList(unusedEndpointRoads, anyEndpointRoads))
                )
        );

        // @has-name
        // find some roads that do not have a name and connect them
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                "@has-name",
                new FixedConstant(1),
                new StreamProvider<> (
                        new NotInRelationshipConceptIdPicker(
                            random,
                            (IdStoreInterface) storage,
                            "road",
                            "@has-name",
                            "@has-name-owner"
                    )
                )
        );
        // find some names not used and repeatedly connect a small set/one of them to the roads without names
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                "@has-name",
                new FixedConstant(1),
                new CentralStreamProvider<>(
                        new FixedConstant(60), // take unused names
                        new NotInRelationshipConceptIdPicker(
                               random,
                               (IdStoreInterface) storage,
                               "name",
                               "@has-name",
                                "@has-name-value"
                        )
                )
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-name",
                        new FixedConstant(60),
                        new HashSet<>(Arrays.asList(nameOwner, nameValue))
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
