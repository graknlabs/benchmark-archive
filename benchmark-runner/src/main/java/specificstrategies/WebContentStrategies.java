package specificstrategies;

import ai.grakn.client.Grakn;
import ai.grakn.concept.ConceptId;
import pdf.BoundedZipfPDF;
import pdf.ConstantPDF;
import pdf.DiscreteGaussianPDF;
import pdf.PDF;
import pdf.UniformPDF;
import pick.CentralStreamProvider;
import storage.FromIdStoragePicker;
import pick.NotInRelationshipConceptIdStream;
import pick.PickableCollectionValuePicker;
import pick.StreamProvider;
import pick.StreamProviderInterface;
import pick.StringStreamGenerator;
import storage.ConceptStore;
import storage.IdStoreInterface;
import storage.SchemaManager;
import strategy.AttributeOwnerTypeStrategy;
import strategy.AttributeStrategy;
import strategy.EntityStrategy;
import strategy.GrowableGeneratedRouletteWheel;
import strategy.RelationshipStrategy;
import strategy.RolePlayerTypeStrategy;
import strategy.RouletteWheel;
import strategy.TypeStrategyInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class WebContentStrategies implements SpecificStrategy {

    private Random random;
    private SchemaManager schemaManager;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public WebContentStrategies(Random random, SchemaManager schemaManager, ConceptStore storage) {
        this.random = random;
        this.schemaManager = schemaManager;
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

    private void setup(Grakn.Transaction tx) {

        primarySetup();

    }


    /**
     * primary instances eg people/companies/employment etc.
     */
    private void primarySetup() {

        /*

            entities

         */
        // ----- Person -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "person",
                        new UniformPDF(random, 10, 70) // on avg, per 40 people
                ));

        // --- company organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "company",
                        new UniformPDF(random, 1, 5) // create 3 companies
                ));

        // --- university organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "university",
                        new UniformPDF(random, 1, 3) // 2 universities
                ));

        // --- department organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "department",
                        new UniformPDF(random, 3, 9) // 6 departments
                ));

        // --- team organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "team",
                        new UniformPDF(random, 10, 16) // 13 teams
                ));


        /*

            relationships

            general idea: add relationships to bottom level entities of the various hierarchies
            (people to teams, projects to teams) plus build the hierarchies (connect departments to companies etc)

         */

        // people (any) - company employment (1 with no previous employments (central stream picker(NotInRelationship...))),
        // Zipf, 1-5k employments to the company
        // NOTE: will will also add people to teams that belong to companies as members etc
        // but its too complicated to conditionally add new employees if the person is already a member etc.
        add(1, relationshipStrategy(
                "employment",
                zipf(5000,1.5),
                rolePlayerTypeStrategy(
                        "employee",
                        "person",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "company",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("company"))
                )
        ));


        // people (any) - university employment (1 with no previous employments) (same as above)
        // Normal, mu=200, sigma^2=50^2 employments to the university
        // NOTE: same as above except with universities
        add(1, relationshipStrategy(
                "employment",
                gaussian(200, 50*50),
                rolePlayerTypeStrategy(
                        "employee",
                        "person",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "university",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("university"))
                )
         ));

        // person (any) - project (any) membership
        // Normal, mu=6, sigma^2=3
        add(1, relationshipStrategy(
                "membership",
                gaussian(6, 3),
                rolePlayerTypeStrategy(
                        "member",
                        "person",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group",
                        "team",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("team"))
                )
        ));

        // person (any) - team membership (all to 1 (centralstream picker))
        // Normal, mu=10, sigma^2=3^2
        add(1, relationshipStrategy(
                "membership",
                gaussian(10, 9),
                rolePlayerTypeStrategy(
                        "member",
                        "person",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group",
                        "team",
                        constant(1),
                        new CentralStreamProvider<>(fromIdStoragePicker("team"))
                )
        ));


        // company (any 1) - department (not owned) ownership
        // Uniform, [1,8], Central stream picker , NotInRelationship
        // ie. pick any company, assign N unassigned departments to it
        add(1, relationshipStrategy(
                "ownership",
                uniform(1, 8),
                rolePlayerTypeStrategy(
                        "owner",
                        "company",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("company"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "department",
                        constant(1),
                        new StreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "ownership",
                                        "property",
                                        100,
                                        fromIdStoragePicker("department")
                                )
                        )
                )
        ));

        // university (any 1) - department (not owned) ownership
        // Uniform, [1,4]
        // ie. pick a university, assign N unassigned departments to it
        add(1, relationshipStrategy(
                "ownership",
                uniform(1, 4),
                rolePlayerTypeStrategy(
                        "owner",
                        "university",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("company"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "department",
                        constant(1),
                        new StreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "ownership",
                                        "property",
                                        100,
                                        fromIdStoragePicker("department")
                                )
                        )
                )
        ));

        // department (any 1) - team (any not owned) ownership
        // normal mu=5, sigma^2=1.5^2 teams in a department
        // ie. pick a department, assign N teams that don't aren't owned yet
        add(1, relationshipStrategy(
                "ownership",
                gaussian(5, 1.5*1.5),
                rolePlayerTypeStrategy(
                        "owner",
                        "department",
                        constant(1),  // pick 1 department for this n from uniform(2,10)
                        new CentralStreamProvider<>(fromIdStoragePicker("department"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "team",
                        constant(1),
                        new StreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "ownership",
                                        "property",
                                        100,
                                        fromIdStoragePicker("project")
                                ))
                )
        ));

        // team (any) - project ownership (any)
        // Uniform [1,3]
        // ie. pick some team and some project and assign ownership, teams can share projects OK
        add(1, relationshipStrategy(
                "ownership",
                uniform( 1, 3),
                rolePlayerTypeStrategy(
                        "owner",
                        "team",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("team"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "project",
                        constant(1),
                        new StreamProvider<>(fromIdStoragePicker("project"))
                ))
        );



        /*

               attributes

         */

        // person.name
        // person.forename
        // person.middle-name
        // person.surname
        // above can all be generated from same set of names

        StringStreamGenerator stringGenerator = new StringStreamGenerator(random, 6);
        // Populate 100 random names for use as forename/middle/surname
        // all with equal weights (PDF = constant(1))
        GrowableGeneratedRouletteWheel<String> attributeNames = new GrowableGeneratedRouletteWheel<>(random, stringGenerator, constant(1));
        attributeNames.growTo(100);

        this.<String, String>addAttributes(1.0, "name", uniform(10, 70), "person", String.class, new StreamProvider<>(new PickableCollectionValuePicker<>(attributeNames)));


        this.attributeStrategies.add(
                1.0,
                new AttributeStrategy<>(
                        "name",
                        new UniformPDF(random, 10, 70),     // same as people
                        new AttributeOwnerTypeStrategy<>(
                                "person",
                                new StreamProvider<> (
                                        new FromIdStoragePicker<>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                "person",
                                                ConceptId.class
                                        )
                                )
                        ),
                        new StreamProvider<>(
                                new PickableCollectionValuePicker<String>(attributeNames)
                        )
                )
        );




        // employment.job-title

        // job-title.abbreviation

        // company.name

        // university.name

        // team.name

        // department.name

        // project.name

    }


    // ---- helpers ----
    private UniformPDF uniform(int lowerBound, int upperBound) {
        return new UniformPDF(random, lowerBound, upperBound);
    }

    private DiscreteGaussianPDF gaussian(double mean, double variance) {
        return new DiscreteGaussianPDF(random, mean, variance);
    }

    private BoundedZipfPDF zipf(int rangeLimit, double exponent) {
        return new BoundedZipfPDF(random, rangeLimit, exponent);
    }

    private ConstantPDF constant(int constant) {
        return new ConstantPDF(constant);
    }

    private FromIdStoragePicker<ConceptId> fromIdStoragePicker(String typeLabel) {
        return new FromIdStoragePicker<>(random, (IdStoreInterface) this.storage, typeLabel, ConceptId.class);
    }

    private RolePlayerTypeStrategy rolePlayerTypeStrategy(
            String roleLabel,
            String rolePlayerLabel,
            PDF pdf,
            StreamProviderInterface<ConceptId> conceptIdProvider) {
        return new RolePlayerTypeStrategy(roleLabel, rolePlayerLabel, pdf, conceptIdProvider);
    }

    private RelationshipStrategy relationshipStrategy(String relationshipTypeLabel, PDF pdf, RolePlayerTypeStrategy... roleStrategiesList) {
        Set<RolePlayerTypeStrategy> roleStrategies = new HashSet<>(Arrays.asList(roleStrategiesList));
        return new RelationshipStrategy(relationshipTypeLabel, pdf, roleStrategies);
    }


    private void add(double weight, RelationshipStrategy relationshipStrategy) {
        this.relationshipStrategies.add(weight, relationshipStrategy);
    }


    private <C, T> void addAttributes(double weight, String attributeLabel, PDF quantityPDF, String ownerLabel, String ownerType, StreamProviderInterface<T> valueProvider) {

        this.attributeStrategies.add(
            weight,
            new AttributeStrategy<>(
                 attributeLabel,
                 quantityPDF,
                 new AttributeOwnerTypeStrategy<>(
                                ownerLabel,
                                new StreamProvider<> (
                                        new FromIdStoragePicker<C>(
                                                random,
                                                (IdStoreInterface) this.storage,
                                                ownerLabel,
                                                ownerType
                                        )
                                )
                        ),
                        valueProvider
                        )
                );
    }


    // ---- end helpders ----

    /**
     * secondary instances of the schema, related to publishing/web content
     */
    private void secondarySetup() {
        // ----- Publication -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "publication",
                        new UniformPDF(random, 10, 50)
        ));

        // --- scientific-publication publication
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-publication",
                        new UniformPDF(random, 10, 50)
        ));

        // --- medium-post publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "medium-post",
                        new UniformPDF(random, 10, 50)
        ));

        // - article medium-post -
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "article",
                        new UniformPDF(random, 10, 50)
        ));

        // --- book publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "book",
                        new UniformPDF(random, 10, 50)
                ));

        // --- scientific-journal publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-journal",
                        new UniformPDF(random, 10, 50)
                ));


        // ----- symposium -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "symposium",
                        new UniformPDF(random, 1, 3)
        ));

        // ----- publishing-platform -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("publishing-platform",
                        new UniformPDF(random, 1, 3)
        ));

        // --- web-service publishing-platform ---
        this.entityStrategies.add(
                1,
                new EntityStrategy("web-service",
                        new UniformPDF(random, 1, 3)
        ));

        // --- website publishing-platform
        this.entityStrategies.add(
                1,
                new EntityStrategy("website",
                        new UniformPDF(random, 1, 3)
        ));

        // ----- Object -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("object",
                        new UniformPDF(random, 10, 100)
        ));


    }

}
