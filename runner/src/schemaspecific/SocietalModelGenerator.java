package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.probdensity.*;
import grakn.benchmark.runner.pick.CentralStreamProvider;
import grakn.benchmark.runner.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.runner.pick.NotInRelationshipConceptIdStream;
import grakn.benchmark.runner.pick.PickableCollectionValuePicker;
import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.IdStoreInterface;
import grakn.benchmark.runner.strategy.AttributeStrategy;
import grakn.benchmark.runner.strategy.EntityStrategy;
import grakn.benchmark.runner.strategy.RelationshipStrategy;
import grakn.benchmark.runner.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Deprecated
public class SocietalModelGenerator implements SchemaSpecificDataGenerator {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public SocietalModelGenerator(Random random, ConceptStore storage) {
        this.random = random;
        this.storage = storage;

        this.entityStrategies = new RouletteWheel<>(random);
        this.relationshipStrategies = new RouletteWheel<>(random);
        this.attributeStrategies = new RouletteWheel<>(random);
        this.operationStrategies = new RouletteWheel<>(random);

        setup();
    }

    @Override
    public RouletteWheel<RouletteWheel<TypeStrategyInterface>> getStrategy() {
        return this.operationStrategies;
    }

    private void setup() {

        this.entityStrategies.add(
                0.5,
                new EntityStrategy(
                        "person",
                        new FixedUniform(random, 20, 40)
                ));

        this.entityStrategies.add(
                0.5,
                new EntityStrategy(
                        "company",
                        new FixedUniform(random, 1, 5)
                )
        );

        Set<RolePlayerTypeStrategy> employmentRoleStrategies = new HashSet<>();

        employmentRoleStrategies.add(
                new RolePlayerTypeStrategy(
                        "employee",
                        "person",
                        new FixedConstant(1),
                        new StreamProvider<>(
                                new FromIdStorageConceptIdPicker(
                                        random,
                                        (IdStoreInterface) this.storage,
                                        "person")
                        )
                )
        );

        employmentRoleStrategies.add(
                new RolePlayerTypeStrategy(
                        "employer",
                        "company",
                        new FixedConstant(1),
                        new CentralStreamProvider<>(
                                new FixedConstant(1),
                                new NotInRelationshipConceptIdStream(
                                        "employment",
                                        "employer",
                                        100,
                                        new FromIdStorageConceptIdPicker(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company")
                                )
                        )
                )
        );

        this.relationshipStrategies.add(
                0.3,
                new RelationshipStrategy(
                        "employment",
                        new ScalingDiscreteGaussian(random, ()->getGraphScale(), 0.5, 0.25),
                        employmentRoleStrategies)
        );

        RouletteWheel<String> nameValueOptions = new RouletteWheel<String>(random)
                .add(0.5, "Da Vinci")
                .add(0.5, "Nero")
                .add(0.5, "Grakn")
                .add(0.5, "Google")
                .add(0.5, "Facebook")
                .add(0.5, "Microsoft")
                .add(0.5, "JetBrains")
                .add(0.5, "IBM")
                .add(0.5, "Starbucks");


        // TODO insert attribute relationships, so no only orphaned
        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new FixedUniform(random,30, 50),
                        new StreamProvider<>(new PickableCollectionValuePicker<String>(nameValueOptions))
                )
        );

//        this.relationshipStrategies.add(
//                1.0,
//                new RelationshipStrategy(
//                        "@has-name",
//                        new FixedUniform(random, 30, 50),
//
//                )
//        )

//        this.attributeStrategies.add(
//                1.0,
//                new AttributeStrategy<>(
//                        "name",
//                        new ScalingUniform(random, ()->getGraphScale(), 0.75,1.25),
//                        new AttributeOwnerTypeStrategy<>(
//                                "company",
//                                new StreamProvider<>(
//                                        new FromIdStorageConceptIdPicker(
//                                                random,
//                                                (IdStoreInterface) this.storage,
//                                                "company")
//                                )
//                        ),
//                        new StreamProvider<>(
//                                new PickableCollectionValuePicker<String>(nameValueOptions)
//                        )
//                )
//        );


//            RouletteWheel<String> genderValueOptions = new RouletteWheel<String>(this.rand)
//            .add(0.5, "male")
//            .add(0.5, "female");
//
//
//            this.attributeStrategies.add(
//                    1.0,
//                    new AttributeStrategy<String>(
//                            schemaManager.getTypeFromString("gender", this.attributeTypes),
//                            new FixedUniform(this.rand, 3, 20),
//                            new AttributeOwnerTypeStrategy<>(
//                                    schemaManager.getTypeFromString("name", this.attributeTypes),
//                                    new StreamProvider<>(
//                                            new FromIdStoragePicker<>(
//                                                    this.rand,
//                                                    (IdStoreInterface) this.storage,
//                                                    "name",
//                                                    String.class)
//                                    )
//                            ),
//                            new StreamProvider<>(
//                                    new PickableCollectionValuePicker<String>(genderValueOptions)
//                            )
//                    )
//            );

//            RouletteWheel<Integer> ratingValueOptions = new RouletteWheel<Integer>(this.rand)
//            .add(0.5, 1)
//            .add(0.5, 2)
//            .add(0.5, 3)
//            .add(0.5, 4)
//            .add(0.5, 5)
//            .add(0.5, 6)
//            .add(0.5, 7)
//            .add(0.5, 8)
//            .add(0.5, 9)
//            .add(0.5, 10);


//        this.attributeStrategies.add(
//                1.0,
//                new AttributeStrategy<>(
//                        "rating",
//                        new ScalingUniform(random, ()->storage.totalEntities(), 0.33, 0.50),
//                        new AttributeOwnerTypeStrategy<>(
//                                "name",
//                                new StreamProvider<>(
//                                        new FromIdStorageStringAttrPicker(
//                                                random,
//                                                (IdStoreInterface) this.storage,
//                                                "name")
//                                )
//                        ),
//                        new StreamProvider<>(
//                                new CountingStreamGenerator(random, 0, 100)
//                        )
//                )
//        );


//        this.attributeStrategies.add(
//                5.0,
//                new AttributeStrategy<>(
//                        "rating",
//                       new ScalingUniform(random, ()->getGraphScale(),0.1, 1.0),
//                        new AttributeOwnerTypeStrategy<>(
//                                "company",
//                                new StreamProvider<>(
//                                        new FromIdStorageConceptIdPicker(
//                                                random,
//                                                (IdStoreInterface) this.storage,
//                                                "company")
//                                )
//                        ),
//                        new StreamProvider<>(
//                                new CountingStreamGenerator(random, 0, 1000000)
//                        )
//                )
//        );


//        this.attributeStrategies.add(
//                3.0,
//                new AttributeStrategy<>(
//                        "rating",
//                        new ScalingUniform(random, ()->storage.totalEntities(), 1.0, 1.5),
//                        new AttributeOwnerTypeStrategy<>(
//                                "employment",
//                                new StreamProvider<>(
//                                        new FromIdStorageConceptIdPicker(
//                                                random,
//                                                (IdStoreInterface) this.storage,
//                                                "employment")
//                                )
//                        ),
//                        new StreamProvider<>(
//                                new CountingStreamGenerator(random, 1, 10)
//                        )
//                )
//        );

        this.operationStrategies.add(0.6, this.entityStrategies);
        this.operationStrategies.add(0.2, this.relationshipStrategies);
        this.operationStrategies.add(0.2, this.attributeStrategies);

    }


    public ConceptStore getConceptStore() {
        return this.storage;
    }

}
