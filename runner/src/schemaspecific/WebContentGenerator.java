package grakn.benchmark.runner.schemaspecific;

import grakn.benchmark.runner.probdensity.*;
import grakn.core.concept.ConceptId;
import grakn.benchmark.runner.pick.CentralStreamProvider;
import grakn.benchmark.runner.storage.FromIdStorageConceptIdPicker;
import grakn.benchmark.runner.storage.FromIdStoragePicker;
import grakn.benchmark.runner.pick.NotInRelationshipConceptIdStream;
import grakn.benchmark.runner.pick.PickableCollectionValuePicker;
import grakn.benchmark.runner.pick.StreamProvider;
import grakn.benchmark.runner.pick.StreamProviderInterface;
import grakn.benchmark.runner.pick.StringStreamGenerator;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.FromIdStorageStringAttrPicker;
import grakn.benchmark.runner.storage.IdStoreInterface;
import grakn.benchmark.runner.strategy.AttributeOwnerTypeStrategy;
import grakn.benchmark.runner.strategy.AttributeStrategy;
import grakn.benchmark.runner.strategy.EntityStrategy;
import grakn.benchmark.runner.strategy.GrowableGeneratedRouletteWheel;
import grakn.benchmark.runner.strategy.RelationshipStrategy;
import grakn.benchmark.runner.strategy.RolePlayerTypeStrategy;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

        //TODO needs tweaks to make nice outputs
        this.operationStrategies.add(1.0, entityStrategies);
        this.operationStrategies.add(1.2, relationshipStrategies);
        this.operationStrategies.add(0.4, attributeStrategies);
    }


    /**
     * primary instances eg people/companies/employment etc.
     */
    private void primarySetup() {
        // TODO needs tweaks to make nice outputs

        /*

            entities

         */
        // ----- Person -----
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "person",
                        uniform(10, 70) // on avg, for every 40 people
                ));

        // --- company organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "company",
                        uniform( 1, 5) // ...create 3 companies
                ));

        // --- university organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "university",
                        uniform( 1, 3) // ...2 universities
                ));

        // --- department organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "department",
                        uniform(3, 7) // 5 departments
                ));

        // --- team organisation ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "team",
                        uniform( 4, 10) // 7 teams
                ));

        // --- project ---
        this.entityStrategies.add(
                1,
                new EntityStrategy(
                        "project",
                        uniform( 5, 19) // 12 projects
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "company",
                        constant(1),
                        new CentralStreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "employment",
                                        "employer",
                                        100,
                                        fromIdStorageConceptIdPicker("company")
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "employer",
                        "university",
                        constant(1),
                        new CentralStreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "employment",
                                        "employer",
                                        100,
                                        fromIdStorageConceptIdPicker("university"))
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group",
                        "project",
                        constant(1),
                        new CentralStreamProvider<>(fromIdStorageConceptIdPicker("project"))
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("person"))
                ),
                rolePlayerTypeStrategy(
                        "group",
                        "team",
                        constant(1),
                        new CentralStreamProvider<>(fromIdStorageConceptIdPicker("team"))
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("company"))
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
                                        fromIdStorageConceptIdPicker("department")
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("company"))
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
                                        fromIdStorageConceptIdPicker("department")
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
                        constant(1),  // pick 1 department for this n from uniform(2,10)
                        new CentralStreamProvider<>(fromIdStorageConceptIdPicker("department"))
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
                                        fromIdStorageConceptIdPicker("team")
                                ))
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
                        constant(1),
                        new StreamProvider<>(fromIdStorageConceptIdPicker("team"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "project",
                        constant(1),
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
        // Populate 200 random names for use as forename/middle/surname, company name etc.
        // all with equal weights (ProbabilityDensityFunction = constant(1))
        GrowableGeneratedRouletteWheel<String> names = new GrowableGeneratedRouletteWheel<>(random, sixCharStringGenerator, constant(1));
        names.growTo(200);

        addAttributes(
                1.0,
                "forename",
                scalingUniform(5/40.0, 20/40.0),
                "person",
                fromIdStorageConceptIdPicker("person"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(names))
        );

        addAttributes(
                1.0,
                "surname",
                scalingUniform( 5/40.0, 20/40.0),
                "person",
                fromIdStorageConceptIdPicker("person"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(names))
        );

        addAttributes(
                1.0,
                "middle-name",
                scalingUniform(5/40.0, 20/40.0),
                "person",
                fromIdStorageConceptIdPicker("person"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(names))
        );

        // employment.job-title
        GrowableGeneratedRouletteWheel<String> jobtitles = new GrowableGeneratedRouletteWheel<>(random, sixCharStringGenerator, constant(1));
        jobtitles.growTo(50);
        addAttributes(
                1.0,
                "job-title",
                scalingGaussian(20/40.0, 5/40.0),
                "employment",
                fromIdStorageConceptIdPicker("employment"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(jobtitles))
        );

        // job-title.abbreviation (job-title is a type of name, which `has abbreviation`
        StringStreamGenerator twoCharStringGenerator = new StringStreamGenerator(random, 2);
        GrowableGeneratedRouletteWheel<String> jobtitleAbbrs = new GrowableGeneratedRouletteWheel<>(random, twoCharStringGenerator, constant(1));
        jobtitleAbbrs.growTo(50);
        addAttributes(
                1.0,
                "abbreviation",
                scalingGaussian(20/40.0, 5/40.0),
                "job-title",
                fromIdStorageStringAttrPicker("job-title"), // NOTE we need to retrieve StringAttrs from storage!
                new StreamProvider<>(new PickableCollectionValuePicker<String>(jobtitleAbbrs))
        );

        // company.name
        addAttributes(
                1.0,
                "name",
                scalingUniform(1/40.0, 5/40.0),
                "company",
                fromIdStorageConceptIdPicker("company"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(names))
        );

        // university.name
        addAttributes(
                1.0,
                "name",
                scalingUniform(1/40.0, 3/40.0),
                "university",
                fromIdStorageConceptIdPicker("university"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(names))
        );

         // team.name
         StringStreamGenerator fourCharStringGenerator = new StringStreamGenerator(random, 4);
         GrowableGeneratedRouletteWheel<String> shortNames = new GrowableGeneratedRouletteWheel<>(random, fourCharStringGenerator, constant(1));
         shortNames.growTo(100);
         addAttributes(
                1.0,
                "name",
                scalingUniform(2/40.0, 5/40.0),
                "team",
                fromIdStorageConceptIdPicker("team"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(shortNames))
        );

        // department.name
        addAttributes(
                1.0,
                "name",
                scalingUniform(3/40.0, 9/40.0),
                "department",
                fromIdStorageConceptIdPicker("department"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(shortNames))
        );

        // project.name
        addAttributes(
                1.0,
                "name",
                scalingUniform(5/40.0, 20/40.0),
                "project",
                fromIdStorageConceptIdPicker("project"),
                new StreamProvider<>(new PickableCollectionValuePicker<String>(shortNames))
        );

    }


    // ---- helpers ----
    private FixedUniform uniform(int lowerBound, int upperBound) {
        return new FixedUniform(random, lowerBound, upperBound);
    }

    private FixedDiscreteGaussian gaussian(double mean, double stddev) {
        return new FixedDiscreteGaussian(random, mean, stddev);
    }

    private FixedBoundedZipf zipf(int rangeLimit, double exponent) {
        return new FixedBoundedZipf(random, rangeLimit, exponent);
    }

    private FixedConstant constant(int constant) {
        return new FixedConstant(constant);
    }

    private ScalingUniform scalingUniform(double lowerBoundFraction, double upperBoundFraction) {
        return new ScalingUniform(random, () -> storage.totalEntities(), lowerBoundFraction, upperBoundFraction);
    }

    private ScalingDiscreteGaussian scalingGaussian(double meanScaleFraction, double stddevScaleFraction) {
        return new ScalingDiscreteGaussian(random, () -> storage.totalEntities(), meanScaleFraction, stddevScaleFraction);
    }

    private ScalingBoundedZipf scalingZipf(double rangeLimitFraction, double initialExponentForScale40) {
        return new ScalingBoundedZipf(random, () -> storage.totalEntities(), rangeLimitFraction, initialExponentForScale40);
    }

    private ScalingConstant scalingConstant(double constantFraction) {
        return new ScalingConstant(() -> storage.totalEntities(), constantFraction);
    }

    private FromIdStoragePicker<ConceptId> fromIdStorageConceptIdPicker(String typeLabel) {
        return new FromIdStorageConceptIdPicker(random, (IdStoreInterface) this.storage, typeLabel);
    }
    private FromIdStoragePicker<String> fromIdStorageStringAttrPicker(String typeLabel) {
        return new FromIdStorageStringAttrPicker(random, (IdStoreInterface) this.storage, typeLabel);
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


    private <C, T> void addAttributes(double weight, String attributeLabel, ProbabilityDensityFunction quantityPDF, String ownerLabel, FromIdStoragePicker<C> ownerPicker, StreamProviderInterface<T> valueProvider) {
        this.attributeStrategies.add(
            weight,
            new AttributeStrategy<>(
                 attributeLabel,
                 quantityPDF,
                 new AttributeOwnerTypeStrategy<>(
                                ownerLabel,
                                new StreamProvider<> (
                                       ownerPicker
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

}
