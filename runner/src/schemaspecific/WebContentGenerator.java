package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.probdensity.*;
import grakn.benchmark.runner.storage.*;
import grakn.core.concept.ConceptId;
import grakn.benchmark.runner.pick.CentralStreamProvider;
import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.pick.StreamProviderInterface;
import grakn.benchmark.runner.pick.StringStreamGenerator;
import grakn.benchmark.runner.strategy.AttributeStrategy;
import grakn.benchmark.runner.strategy.EntityStrategy;
import grakn.benchmark.runner.strategy.RelationshipStrategy;
import grakn.benchmark.runner.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

@Deprecated
public class WebContentGenerator implements SchemaSpecificDataGenerator {

    private Random random;
    private ConceptStore storage;

    private RouletteWheel<TypeStrategyInterface> entityStrategies;
    private RouletteWheel<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheel<TypeStrategyInterface> attributeStrategies;
    private RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies;

    public WebContentGenerator(Random random, ConceptStore storage) {
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
        primarySetup();

        this.operationStrategies.add(1.0, entityStrategies);
        this.operationStrategies.add(1.2, relationshipStrategies);
        this.operationStrategies.add(0.4, attributeStrategies);
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
                        fixedUniform(10, 70) // on avg, for every 40 people
                ));

        // --- company organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "company",
                        fixedUniform( 1, 5) // ...create 3 companies
                ));

        // --- university organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "university",
                        fixedUniform( 1, 3) // ...2 universities
                ));

        // --- department organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "department",
                        fixedUniform(3, 7) // 5 departments
                ));

        // --- team organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "team",
                        fixedUniform( 4, 10) // 7 teams
                ));

        // --- project ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "project",
                        fixedUniform( 5, 20) // 12.5 projects
                ));

        /*

            relationships

            general idea: add relationships to bottom level entities of the various hierarchies
            (people to teams, projects to teams) plus build the hierarchies (connect departments to companies etc)

         */

        // people (any) - company employment (1 with no previous employments (central stream picker(NotInRelationship...))),
        // Zipf, 1-2k employments to the company
        // NOTE: will will also add people to teams that belong to companies as members etc
        // but its too complicated to conditionally add new employees if the person is already a member etc.
        add(2, relationshipStrategy(
                "employment",
                scalingZipf(0.6,1.5),
                rolePlayerTypeStrategy(
                        "employee",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "company",
                        fixedConstant(1),
                        new CentralStreamProvider<>(
                                fixedConstant(1),
                                notInRelationshipConceptIdStoragePicker(
                                        "company",
                                        "employment",
                                        "employer"
                                )
                        )
                )
        ));


        // people (any) - university employment (1 with no previous employments) (same as above)
        add(2, relationshipStrategy(
                "employment",
                scalingZipf(0.1, 1.6),
                rolePlayerTypeStrategy(
                        "employee",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "university",
                        fixedConstant(1),
                        new CentralStreamProvider<>(
                                fixedConstant(1),
                                notInRelationshipConceptIdStoragePicker(
                                        "university",
                                        "employment",
                                        "employer"
                                )
                        )
                )
         ));

        // person (any) - project (any) membership
        // Normal, mu=9, sigma=2.5
        add(2, relationshipStrategy(
                "membership",
                scalingGaussian(9/40.0, 2.5/40.0),
                rolePlayerTypeStrategy(
                        "member",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group_",
                        "project",
                        fixedConstant(1),
                        new CentralStreamProvider<>(fixedConstant(1), fromIdStorageConceptIdPicker("project"))
                )
        ));

        // person (any) - team membership (all to 1 (centralstream picker))
        // Normal, mu=10, sigma=3 => fraction
        add(2, relationshipStrategy(
                "membership",
                scalingGaussian( 10/40.0, 3/40.0),
                rolePlayerTypeStrategy(
                        "member",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group_",
                        "team",
                        fixedConstant(1),
                        new CentralStreamProvider<>(fixedConstant(1), fromIdStorageConceptIdPicker("team"))
                )
        ));


        // company (any 1) - department (not owned) ownership
        // Uniform, [1,8], Central stream picker , NotInRelationship
        // ie. pick any company, assign N unassigned departments to it
        add(1, relationshipStrategy(
                "ownership",
                scalingUniform( 1.0/40, 8/40.0),
                rolePlayerTypeStrategy(
                        "owner",
                        "company",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("company"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "department",
                        fixedConstant(1),
                        new StreamProvider<>(
                                notInRelationshipConceptIdStoragePicker(
                                        "department",
                                        "ownership",
                                        "property"
                                )
                        )
                )
        ));

        // university (any 1) - department (not owned) ownership
        // Uniform, [1,4]
        // ie. pick a university, assign N unassigned departments to it
        add(1, relationshipStrategy(
                "ownership",
                scalingUniform( 1/40.0, 4/40.0),
                rolePlayerTypeStrategy(
                        "owner",
                        "university",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("company"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "department",
                        fixedConstant(1),
                        new StreamProvider<>(
                                notInRelationshipConceptIdStoragePicker(
                                        "department",
                                        "ownership",
                                        "property"
                                )
                        )
                )
        ));

        // department (any 1) - team (any not owned) ownership
        // normal mu=5, sigma^2=1.5^2 teams in a department
        // ie. pick a department, assign N teams that don't aren't owned yet
        add(1, relationshipStrategy(
                "ownership",
                scalingGaussian( 5/40.0, 1.5/40.0),
                rolePlayerTypeStrategy(
                        "owner",
                        "department",
                        fixedConstant(1),  // pick 1 department for this n from fixedUniform(2,10)
                        new CentralStreamProvider<>(fixedConstant(1),fromIdStorageConceptIdPicker("department"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "team",
                        fixedConstant(1),
                        new StreamProvider<>(
                                notInRelationshipConceptIdStoragePicker(
                                        "team",
                                        "ownership",
                                        "property"
                                )
                        )
                )
        ));

        // team (any) - project ownership (any)
        // Uniform [1,3]
        // ie. pick some team and some project and assign ownership, teams can share projects OK
        add(1, relationshipStrategy(
                "ownership",
                scalingUniform( 1/40.0, 3/40.0),
                rolePlayerTypeStrategy(
                        "owner",
                        "team",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("team"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "project",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("project"))
                ))
        );



        /*

               attributes

         */

        // person.forename
        // person.middle-name
        // person.surname
        // above can all be generated from same set of names

        StringStreamGenerator sixCharStringGenerator = new StringStreamGenerator(random, 6);
        StringStreamGenerator fourCharStringGenerator = new StringStreamGenerator(random, 4);
        StringStreamGenerator twoCharStringGenerator = new StringStreamGenerator(random, 2);

        addAttributes(
                1.0,
                "forename",
                fixedUniform(5, 20),
                new StreamProvider<>(sixCharStringGenerator)
        );
        // attach attribute "forename" to "people", choosing people without names yet to give them names. Names may be reused
        // NOT scaling -- want approximately 1 per person entity
        add(1, relationshipStrategy(
                "@has-forename",
                scalingUniform(5/40.0, 40/40.0),
                rolePlayerTypeStrategy(
                        "@has-forename-owner",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "@has-forename-value",
                        "forename",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("forename"))
                ))
        );

        addAttributes(
                1.0,
                "surname",
                fixedUniform( 5, 20),
                new StreamProvider<>(sixCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-surname",
                scalingUniform(5/40.0, 40/40.0),
                rolePlayerTypeStrategy(
                        "@has-surname-owner",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "@has-surname-value",
                        "surname",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("surname"))
                ))
        );

        addAttributes(
                1.0,
                "middle-name",
                fixedUniform(5, 20),
                new StreamProvider<>(fourCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-middle-name",
                scalingUniform(5/40.0, 40/40.0),
                rolePlayerTypeStrategy(
                        "@has-middle-name-owner",
                        "person",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "@has-middle-name-value",
                        "middle-name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("middle-name"))
                ))
        );

        addAttributes(
                1.0,
                "job-title",
                fixedGaussian(20, 5),
                new StreamProvider<>(sixCharStringGenerator)
        );
        // attach attribute "surname" to "people", choosing people without names yet to give them names. Names may be reused
        add(1, relationshipStrategy(
                "@has-job-title",
                scalingZipf(0.8,1.47),
                rolePlayerTypeStrategy(
                        "@has-job-title-owner",
                        "employment",
                        fixedConstant(1),
                        new StreamProvider<>(notInRelationshipConceptIdStoragePicker("employment", "@has-job-title", "@has-job-title-owner"))
                ),
                rolePlayerTypeStrategy(
                        "@has-job-title-value",
                        "job-title",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("job-title"))
                ))
        );

        // job-title.abbreviation (job-title is a type of name, which `has abbreviation`)
        addAttributes(
                1.0,
                "abbreviation",
                fixedGaussian(10, 3),
                new StreamProvider<>(twoCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-abbreviation",
                scalingGaussian(10/40.0, 3/40.0),
                rolePlayerTypeStrategy(
                        "@has-abbreviation-owner",
                        "job-title",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("job-title"))
                ),
                rolePlayerTypeStrategy(
                        "@has-abbreviation-value",
                        "abbreviation",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("abbreviation"))
                ))
        );

        // company.name
        addAttributes(
                1.0,
                "name",
                fixedUniform(1, 5),
                new StreamProvider<>(sixCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-name",
                scalingUniform( 1/40.0, 5/40.0),
                rolePlayerTypeStrategy(
                        "@has-name-owner",
                        "company",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("company"))
                ),
                rolePlayerTypeStrategy(
                        "@has-name-value",
                        "name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("name"))
                ))
        );

        // university.name
        addAttributes(
                1.0,
                "name",
                fixedUniform(1, 3),
                new StreamProvider<>(sixCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-name",
                scalingUniform(1/40.0, 3/40.0),
                rolePlayerTypeStrategy(
                        "@has-name-owner",
                        "university",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("university"))
                ),
                rolePlayerTypeStrategy(
                        "@has-name-value",
                        "name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("name"))
                ))
        );

         // team.name
         addAttributes(
                1.0,
                "name",
                fixedUniform(2, 5),
                new StreamProvider<>(fourCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-name",
                scalingUniform(2/40.0, 5/40.0),
                rolePlayerTypeStrategy(
                        "@has-name-owner",
                        "team",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("team"))
                ),
                rolePlayerTypeStrategy(
                        "@has-name-value",
                        "name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("name"))
                ))
        );

        // department.name
        addAttributes(
                1.0,
                "name",
                fixedUniform(3, 7),
                new StreamProvider<>(fourCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-name",
                scalingUniform(3/40.0, 7/40.0),
                rolePlayerTypeStrategy(
                        "@has-name-owner",
                        "department",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("department"))
                ),
                rolePlayerTypeStrategy(
                        "@has-name-value",
                        "name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("name"))
                ))
        );

        // project.name
        addAttributes(
                1.0,
                "name",
                fixedUniform(5, 20),
                new StreamProvider<>(fourCharStringGenerator)
        );
        add(1, relationshipStrategy(
                "@has-name",
                scalingUniform(5/40.0, 20/40.0),
                rolePlayerTypeStrategy(
                        "@has-name-owner",
                        "project",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("project"))
                ),
                rolePlayerTypeStrategy(
                        "@has-name-value",
                        "name",
                        fixedConstant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("name"))
                ))
        );
    }


    // ---- helpers ----
    private FixedUniform fixedUniform(int lowerBound, int upperBound) {
        return new FixedUniform(random, lowerBound, upperBound);
    }

    private FixedDiscreteGaussian fixedGaussian(double mean, double stddev) {
        return new FixedDiscreteGaussian(random, mean, stddev);
    }

    private FixedBoundedZipf fixedZipf(int rangeLimit, double exponent) {
        return new FixedBoundedZipf(random, rangeLimit, exponent);
    }

    private FixedConstant fixedConstant(int constant) {
        return new FixedConstant(constant);
    }

    private ScalingUniform scalingUniform(double lowerBoundFraction, double upperBoundFraction) {
        return new ScalingUniform(random, () -> getGraphScale(), lowerBoundFraction, upperBoundFraction);
    }

    private ScalingDiscreteGaussian scalingGaussian(double meanScaleFraction, double stddevScaleFraction) {
        return new ScalingDiscreteGaussian(random, () -> getGraphScale(), meanScaleFraction, stddevScaleFraction);
    }

    private ScalingBoundedZipf scalingZipf(double rangeLimitFraction, double initialExponentForScale40) {
        return new ScalingBoundedZipf(random, () -> getGraphScale(), rangeLimitFraction, initialExponentForScale40);
    }

    private ScalingConstant scalingConstant(double constantFraction) {
        return new ScalingConstant(() -> getGraphScale(), constantFraction);
    }

    private FromIdStoragePicker<ConceptId> fromIdStorageConceptIdPicker(String typeLabel) {
        return new FromIdStorageConceptIdPicker(random, (IdStoreInterface) this.storage, typeLabel);
    }
    private FromIdStoragePicker<String> fromIdStorageStringAttrPicker(String typeLabel) {
        return new FromIdStorageStringAttrPicker(random, (IdStoreInterface) this.storage, typeLabel);
    }

    private NotInRelationshipConceptIdPicker notInRelationshipConceptIdStoragePicker(String typeLabel, String relationshiplabel, String roleLabel) {
        return new NotInRelationshipConceptIdPicker(random, (IdStoreInterface) this.storage, typeLabel, relationshiplabel, roleLabel);
    }

    private RolePlayerTypeStrategy rolePlayerTypeStrategy(
            String roleLabel,
            String rolePlayerLabel,
            ProbabilityDensityFunction pdf,
            StreamProviderInterface<ConceptId> conceptIdProvider) {
        return new RolePlayerTypeStrategy(roleLabel, rolePlayerLabel, pdf, conceptIdProvider);
    }

    private RelationshipStrategy relationshipStrategy(String relationshipTypeLabel, ProbabilityDensityFunction pdf, RolePlayerTypeStrategy... roleStrategiesList) {
        Set<RolePlayerTypeStrategy> roleStrategies = new HashSet<>(Arrays.asList(roleStrategiesList));
        return new RelationshipStrategy(relationshipTypeLabel, pdf, roleStrategies);
    }


    private void add(double weight, RelationshipStrategy relationshipStrategy) {
        this.relationshipStrategies.add(weight, relationshipStrategy);
    }


    private <C, T> void addAttributes(double weight, String attributeLabel, ProbabilityDensityFunction quantityPDF, StreamProviderInterface<T> valueProvider) {
        this.attributeStrategies.add(
            weight,
            new AttributeStrategy<>(
                 attributeLabel,
                 quantityPDF,
                 valueProvider
            ));
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
                        new FixedUniform(random, 10, 50)
        ));

        // --- scientific-publication publication
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-publication",
                        new FixedUniform(random, 10, 50)
        ));

        // --- medium-post publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "medium-post",
                        new FixedUniform(random, 10, 50)
        ));

        // - article medium-post -
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "article",
                        new FixedUniform(random, 10, 50)
        ));

        // --- book publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "book",
                        new FixedUniform(random, 10, 50)
                ));

        // --- scientific-journal publication ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "scientific-journal",
                        new FixedUniform(random, 10, 50)
                ));


        // ----- symposium -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "symposium",
                        new FixedUniform(random, 1, 3)
        ));

        // ----- publishing-platform -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("publishing-platform",
                        new FixedUniform(random, 1, 3)
        ));

        // --- web-service publishing-platform ---
        this.entityStrategies.add(
                1,
                new EntityStrategy("web-service",
                        new FixedUniform(random, 1, 3)
        ));

        // --- website publishing-platform
        this.entityStrategies.add(
                1,
                new EntityStrategy("website",
                        new FixedUniform(random, 1, 3)
        ));

        // ----- Object -----
        this.entityStrategies.add(
                1,
                new EntityStrategy("object",
                        new FixedUniform(random, 10, 100)
        ));


    }


    public ConceptStore getConceptStore() {
        return this.storage;
    }

}
