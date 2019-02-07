package grakn.benchmark.profiler.generator.definition;

import grakn.benchmark.profiler.generator.pick.CentralStreamProvider;
import grakn.benchmark.profiler.generator.pick.StandardStreamProvider;
import grakn.benchmark.profiler.generator.pick.RandomStringIterator;
import grakn.benchmark.profiler.generator.probdensity.FixedConstant;
import grakn.benchmark.profiler.generator.probdensity.FixedUniform;
import grakn.benchmark.profiler.generator.storage.ConceptStorage;
import grakn.benchmark.profiler.generator.storage.ConceptIdStoragePicker;
import grakn.benchmark.profiler.generator.storage.NotInRelationshipConceptIdPicker;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.profiler.generator.pick.WeightedPicker;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class RoadNetworkDefinition extends DataGeneratorDefinition {

    private Random random;
    private ConceptStorage storage;

    private WeightedPicker<TypeStrategy> entityStrategies;
    private WeightedPicker<TypeStrategy> relationshipStrategies;
    private WeightedPicker<TypeStrategy> attributeStrategies;
    private WeightedPicker<WeightedPicker<TypeStrategy>> metaTypeStrategies;

    public RoadNetworkDefinition(Random random, ConceptStorage storage) {
        this.random = random;
        this.storage = storage;

        this.entityStrategies = new WeightedPicker<>(random);
        this.relationshipStrategies = new WeightedPicker<>(random);
        this.attributeStrategies = new WeightedPicker<>(random);
        this.metaTypeStrategies = new WeightedPicker<>(random);

        buildGenerator();
    }

    private void buildGenerator() {
        buildStrategies();
        this.metaTypeStrategies.add(1.0, entityStrategies);
        this.metaTypeStrategies.add(1.25, relationshipStrategies);
        this.metaTypeStrategies.add(1.0, attributeStrategies);
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

        RandomStringIterator nameIterator = new RandomStringIterator(random, 6);

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedUniform(this.random, 10, 30),
                        new StandardStreamProvider<>(nameIterator)
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
                                storage,
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
                new StandardStreamProvider<>(
                        new ConceptIdStoragePicker(random, storage, "road")
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
                new StandardStreamProvider<>(
                        new NotInRelationshipConceptIdPicker(
                                random,
                                storage,
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
                                storage,
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
    protected WeightedPicker<WeightedPicker<TypeStrategy>> getDefinition() {
        return this.metaTypeStrategies;
    }

}
