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
        this.operationStrategies.add(1.2, relationshipStrategies);
        this.operationStrategies.add(0.4, attributeStrategies);
    }

    private void buildStrategies() {

        /*
        Entities
         */

        this.entityStrategies.add(
                1.0,
                new EntityStrategy(
                        "road",
                        new FixedUniform(this.random, 10, 40))
        );

        /*
        Attributes
         */

        StringStreamGenerator nameStream = new StringStreamGenerator(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedUniform(this.random,3, 7),
                        new StreamProvider<>(nameStream)
                )
        );


        /*
        Relationships
         */


        // intersection
        // TODO what we want here is to repeat 5-10 roads not initially in a relationship
        // and use these to connect to to some number of other roads via intersections
        // ie. repeat those 5-10 roads that were initially not in a relationship until number of
        // relationships created is exhausted

        // TODO

//        RolePlayerTypeStrategy unusedEndpointRoads = new RolePlayerTypeStrategy(
//                "endpoint",
//                "road",
//                new FixedConstant(1),
//                new CentralStreamProvider<>(
//                    new NotInRelationshipConceptIdPicker(
//                            random,
//                            (IdStoreInterface) storage,
//                           "road",
//                           "intersection",
//                           "endpoint"
//                    )
//                )
//        );
//        RolePlayerTypeStrategy anyEndpointRoads = new RolePlayerTypeStrategy(
//                "endpoint",
//                "road",
//                new FixedUniform(random, 1, 5),
//                new StreamProvider<>(
//                        new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "road")
//                )
//        );
//
//        this.relationshipStrategies.add(
//                1.0,
//                new RelationshipStrategy(
//                        "intersection",
//                        new Fixed
//                        new HashSet<>(Arrays.asList(friendRoleFiller))
//                )
//        );


        // like
        RolePlayerTypeStrategy likedPageRole = new RolePlayerTypeStrategy(
                "liked",
                "page",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "page"))
        );
        RolePlayerTypeStrategy likerPersonRole = new RolePlayerTypeStrategy(
                "liker",
                "person",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "person"))
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "like",
                        new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.05, 0.001),
                        new HashSet<>(Arrays.asList(likedPageRole, likerPersonRole))
                )
        );


        // @has-name
        RolePlayerTypeStrategy nameOwner = new RolePlayerTypeStrategy(
                "@has-name-owner",
                "person",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "person"))
        );
        RolePlayerTypeStrategy nameValue = new RolePlayerTypeStrategy(
                "@has-name-value",
                "name",
                new FixedConstant(1),
                new StreamProvider<>(new FromIdStorageConceptIdPicker(random, (IdStoreInterface) storage, "name"))
        );
        this.relationshipStrategies.add(
                1.0,
                new RelationshipStrategy(
                        "@has-name",
                        new ScalingDiscreteGaussian(random, () -> this.getGraphScale(), 0.1, 0.03),
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
