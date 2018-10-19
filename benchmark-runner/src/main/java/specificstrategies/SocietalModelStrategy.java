package specificstrategies;

import ai.grakn.concept.ConceptId;
import pdf.ConstantPDF;
import pdf.DiscreteGaussianPDF;
import pdf.UniformPDF;
import pick.CentralStreamProvider;
import pick.FromIdStoragePicker;
import pick.IntegerPicker;
import pick.NotInRelationshipConceptIdStream;
import pick.PickableCollectionValuePicker;
import pick.StreamProvider;
import storage.ConceptStore;
import storage.IdStoreInterface;
import storage.SchemaManager;
import strategy.AttributeOwnerTypeStrategy;
import strategy.AttributeStrategy;
import strategy.EntityStrategy;
import strategy.RelationshipStrategy;
import strategy.RolePlayerTypeStrategy;
import strategy.RouletteWheelCollection;
import strategy.TypeStrategyInterface;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class SocietalModelStrategy implements SpecificStrategy {

    private Random random;
    private SchemaManager schemaManager;
    private ConceptStore storage;

    private RouletteWheelCollection<TypeStrategyInterface> entityStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> attributeStrategies;
    private RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> operationStrategies;

    public SocietalModelStrategy(Random random, SchemaManager schemaManager, ConceptStore storage) {
        this.random = random;
        this.schemaManager = schemaManager;
        this.storage = storage;

        this.entityStrategies = new RouletteWheelCollection<>(random);
        this.relationshipStrategies = new RouletteWheelCollection<>(random);
        this.attributeStrategies = new RouletteWheelCollection<>(random);
        this.operationStrategies = new RouletteWheelCollection<>(random);

        setup();
    }

    @Override
    public RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> getStrategy() {
        return this.operationStrategies;
    }

    private void setup() {

        this.entityStrategies.add(
                0.5,
                new EntityStrategy(
                        schemaManager.getTypeFromString("person"),
                        new UniformPDF(random, 20, 40)
                ));

        this.entityStrategies.add(
                0.5,
                new EntityStrategy(
                        schemaManager.getTypeFromString("company"),
                        new UniformPDF(random, 1, 5)
                )
        );

        Set<RolePlayerTypeStrategy> employmentRoleStrategies = new HashSet<>();

        employmentRoleStrategies.add(
                new RolePlayerTypeStrategy(
                        schemaManager.getTypeFromString("employee"),
                        schemaManager.getTypeFromString("person"),
                        new ConstantPDF(1),
                        new StreamProvider<>(
                                new FromIdStoragePicker<>(
                                        random,
                                        (IdStoreInterface) this.storage,
                                        "person",
                                        ConceptId.class)
                        )
                )
        );

        employmentRoleStrategies.add(
                new RolePlayerTypeStrategy(
                        schemaManager.getTypeFromString("employer"),
                        schemaManager.getTypeFromString("company"),
                        new ConstantPDF(1),
                        new CentralStreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "employment",
                                        "employer",
                                        100,
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company",
                                                ConceptId.class)
                                )
                        )
                )
        );

        this.relationshipStrategies.add(
                0.3,
                new RelationshipStrategy(
                        schemaManager.getTypeFromString("employment"),
                        new DiscreteGaussianPDF(random, 30.0, 30.0),
                        employmentRoleStrategies)
        );

        RouletteWheelCollection<String> nameValueOptions = new RouletteWheelCollection<String>(random)
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
                        schemaManager.getTypeFromString("name"),
                        new UniformPDF(random, 3, 100),
                        new AttributeOwnerTypeStrategy<>(
                                schemaManager.getTypeFromString("company"),
                                new StreamProvider<>(
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company",
                                                ConceptId.class)
                                )
                        ),
                        new StreamProvider<>(
                                new PickableCollectionValuePicker<String>(nameValueOptions)
                        )
                )
        );


//            RouletteWheelCollection<String> genderValueOptions = new RouletteWheelCollection<String>(this.rand)
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

//            RouletteWheelCollection<Integer> ratingValueOptions = new RouletteWheelCollection<Integer>(this.rand)
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
                        schemaManager.getTypeFromString("rating"),
                        new UniformPDF(random, 10, 20),
                        new AttributeOwnerTypeStrategy<>(
                                schemaManager.getTypeFromString("name"),
                                new StreamProvider<>(
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "name",
                                                String.class)
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerPicker(random, 0, 100)
                        )
                )
        );


        this.attributeStrategies.add(
                5.0,
                new AttributeStrategy<>(
                        schemaManager.getTypeFromString("rating"),
                        new UniformPDF(random, 3, 40),
                        new AttributeOwnerTypeStrategy<>(
                                schemaManager.getTypeFromString("company"),
                                new StreamProvider<>(
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "company",
                                                ConceptId.class)
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerPicker(random, 0, 1000000)
                        )
                )
        );


        this.attributeStrategies.add(
                3.0,
                new AttributeStrategy<>(
                        schemaManager.getTypeFromString("rating"),
                        new UniformPDF(random, 40, 60),
                        new AttributeOwnerTypeStrategy<>(
                                schemaManager.getTypeFromString("employment"),  //TODO change this so that declaring the MetaType to search isn't necessary
                                new StreamProvider<>(
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "employment",
                                                ConceptId.class)
                                )
                        ),
                        new StreamProvider<>(
                                new IntegerPicker(random, 1, 10)
                        )
                )
        );

        this.operationStrategies.add(0.6, this.entityStrategies);
        this.operationStrategies.add(0.2, this.relationshipStrategies);
        this.operationStrategies.add(0.2, this.attributeStrategies);

    }

}
