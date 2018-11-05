package specificstrategies;

import ai.grakn.concept.ConceptId;
import pdf.ConstantPDF;
import pdf.DiscreteGaussianPDF;
import pdf.UniformPDF;
import pick.CentralStreamProvider;
import storage.FromIdStorageConceptIdPicker;
import storage.FromIdStoragePicker;
import pick.IntegerStreamGenerator;
import pick.NotInRelationshipConceptIdStream;
import pick.PickableCollectionValuePicker;
import pick.StreamProvider;
import storage.ConceptStore;
import storage.FromIdStorageStringAttrPicker;
import storage.IdStoreInterface;
import strategy.AttributeOwnerTypeStrategy;
import strategy.AttributeStrategy;
import strategy.EntityStrategy;
import strategy.RelationshipStrategy;
import strategy.RolePlayerTypeStrategy;
import strategy.RouletteWheel;
import strategy.TypeStrategyInterface;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SocietalModelStrategy implements SpecificStrategy {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public SocietalModelStrategy(Random random, ConceptStore storage) {
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
                        new UniformPDF(random, 20, 40)
                ));

        this.entityStrategies.add(
                0.5,
                new EntityStrategy(
                        "company",
                        new UniformPDF(random, 1, 5)
                )
        );

        Set<RolePlayerTypeStrategy> employmentRoleStrategies = new HashSet<>();

        employmentRoleStrategies.add(
                new RolePlayerTypeStrategy(
                        "employee",
                        "person",
                        new ConstantPDF(1),
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
                        new ConstantPDF(1),
                        new CentralStreamProvider<>(
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
                        new DiscreteGaussianPDF(random, 30.0, 30.0),
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

//            TODO How to get the datatype without having to declare it? Does it make sense to do this?
//            schemaManager.getDatatype("company", this.entityTypes),

        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new UniformPDF(random, 3, 100),
                        new AttributeOwnerTypeStrategy<>(
                                "company",
                                new StreamProvider<>(
                                        new FromIdStorageConceptIdPicker(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company")
                                )
                        ),
                        new StreamProvider<>(
                                new PickableCollectionValuePicker<String>(nameValueOptions)
                        )
                )
        );


//            RouletteWheel<String> genderValueOptions = new RouletteWheel<String>(this.rand)
//            .add(0.5, "male")
//            .add(0.5, "female");
//
//
//            this.attributeStrategies.add(
//                    1.0,
//                    new AttributeStrategy<String>(
//                            schemaManager.getTypeFromString("gender", this.attributeTypes),
//                            new UniformPDF(this.rand, 3, 20),
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


        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "rating",
                        new UniformPDF(random, 10, 20),
                        new AttributeOwnerTypeStrategy<>(
                                "name",
                                new StreamProvider<>(
                                        new FromIdStorageStringAttrPicker(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "name")
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerStreamGenerator(random, 0, 100)
                        )
                )
        );


        this.attributeStrategies.add(
                5.0,
                new AttributeStrategy<>(
                        "rating",
                        new UniformPDF(random, 3, 40),
                        new AttributeOwnerTypeStrategy<>(
                                "company",
                                new StreamProvider<>(
                                        new FromIdStorageConceptIdPicker(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company")
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerStreamGenerator(random, 0, 1000000)
                        )
                )
        );


        this.attributeStrategies.add(
                3.0,
                new AttributeStrategy<>(
                        "rating",
                        new UniformPDF(random, 40, 60),
                        new AttributeOwnerTypeStrategy<>(
                                "employment",
                                new StreamProvider<>(
                                        new FromIdStorageConceptIdPicker(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "employment")
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerStreamGenerator(random, 1, 10)
                        )
                )
        );

        this.operationStrategies.add(0.6, this.entityStrategies);
        this.operationStrategies.add(0.2, this.relationshipStrategies);
        this.operationStrategies.add(0.2, this.attributeStrategies);

    }

}
