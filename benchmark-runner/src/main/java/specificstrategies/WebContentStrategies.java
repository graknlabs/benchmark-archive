package specificstrategies;

import ai.grakn.concept.ConceptId;
import com.google.common.collect.ImmutableSet;
import pdf.ConstantPDF;
import pdf.PDF;
import pdf.UniformPDF;
import pick.CentralStreamProvider;
import pick.FromIdStoragePicker;
import pick.NotInRelationshipConceptIdStream;
import pick.StreamProvider;
import pick.StreamProviderInterface;
import storage.ConceptStore;
import storage.IdStoreInterface;
import storage.SchemaManager;
import strategy.EntityStrategy;
import strategy.RelationshipStrategy;
import strategy.RolePlayerTypeStrategy;
import strategy.RouletteWheelCollection;
import strategy.TypeStrategyInterface;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

public class WebContentStrategies implements SpecificStrategy {

    private Random random;
    private SchemaManager schemaManager;
    private ConceptStore storage;

    private RouletteWheelCollection<TypeStrategyInterface> entityStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> relationshipStrategies;
    private RouletteWheelCollection<TypeStrategyInterface> attributeStrategies;
    private RouletteWheelCollection<RouletteWheelCollection<TypeStrategyInterface>> operationStrategies;

    public WebContentStrategies(Random random, SchemaManager schemaManager, ConceptStore storage) {
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

         */

        // people (any) - company employment (1 with no previous employments (central stream picker(NotInRelationship...))),
        // Zipf, 1-10k employments to the company


        // people (any) - university employment (1 with no previous employments) (same as above)
        // Normal, mu=200, sigma^2=50^2 employments to the university

        // person (any) - project (any) membership
        // Normal, mu=6, sigma^2=3

        // person (any) - team membership (all to 1 (centralstream picker))
        // Normal, mu=10, sigma^2=3^2

        // person (any) - department membership (all to 1 (centralstream picker))
        // Normal, mu=30, sigma^2=5^2

        // company (any 1) - department (not owned) ownership
        // Uniform, [1,15], Central stream picker , NotInRelationship

        // university (any 1) - department (not owned) ownership
        // Uniform, [1,10]

        // department (any 1) - team (not owned) ownership
        // Uniform [2,10]
        add(1, relationshipStrategy(
                "ownership",
                uniform(2, 10),
                rolePlayerTypeStrategy(
                        "owner",
                        "department",
                        constant(1),
                        new CentralStreamProvider<ConceptId>(fromIdStoragePicker("department"))
                ),
                rolePlayerTypeStrategy(
                        "property",
                        "project",
                        constant(1),
                        new StreamProvider<>(
                                new NotInRelationshipConceptIdStream(
                                        "ownership",
                                        "property",
                                        100,
                                        fromIdStoragePicker("project")
                                ))
                )
        ))

        // team (any) - project ownership (any)
        // Uniform [1,3]
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

        // employment.job-title

        // job-title.abbreviation

        // company.name

        // university.name

        // team.name

        // department.name

        // project.name

    }

    private UniformPDF uniform(int lowerBound, int upperBound) {
        return new UniformPDF(random, lowerBound, upperBound);
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
