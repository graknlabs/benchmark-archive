/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2018 Grakn Labs Ltd
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Affero General Public License as
 *  published by the Free Software Foundation, either version 3 of the
 *  License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Affero General Public License for more details.
 *
 *  You should have received a copy of the GNU Affero General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package grakn.benchmark.runner.generator;

import grakn.core.GraknTxType;
import grakn.core.client.Grakn;
import grakn.core.concept.AttributeType;
import grakn.core.concept.Concept;
import grakn.core.concept.EntityType;
import grakn.core.concept.RelationshipType;
import grakn.core.graql.InsertQuery;
import grakn.core.graql.Query;
import grakn.core.graql.answer.ConceptMap;
import grakn.benchmark.runner.specificstrategies.SpecificStrategy;
import grakn.benchmark.runner.specificstrategies.SpecificStrategyFactory;
import grakn.benchmark.runner.storage.ConceptStore;
import grakn.benchmark.runner.storage.IgniteConceptIdStore;
import grakn.benchmark.runner.storage.InsertionAnalysis;
import grakn.benchmark.runner.storage.SchemaManager;
import grakn.benchmark.runner.strategy.RouletteWheel;
import grakn.benchmark.runner.strategy.TypeStrategyInterface;

import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.stream.Stream;

/**
 *
 */
public class DataGenerator {

    Grakn.Session session;
    String executionName;
    List<String> schemaDefinition;


    private int iteration = 0;
    private Random rand;

    private ConceptStore storage;

    private SpecificStrategy dataStrategies;

    public DataGenerator(Grakn.Session session, String executionName, List<String> schemaDefinition, int randomSeed) {
        this.session = session;
        this.executionName = executionName;
        this.rand = new Random(randomSeed);
        this.iteration = 0;
        this.schemaDefinition = schemaDefinition;
        initializeGeneration();
    }

    public void loadSchema() {
        System.out.println("Initialising keyspace `" + this.session.keyspace() + "`...");
        SchemaManager.initialiseKeyspace(this.session, this.schemaDefinition);
        System.out.println("done");
    }

    private void initializeGeneration() {
        try (Grakn.Transaction tx = session.transaction(GraknTxType.READ)) {
            HashSet<EntityType> entityTypes = SchemaManager.getTypesOfMetaType(tx, "entity");
            HashSet<RelationshipType> relationshipTypes = SchemaManager.getTypesOfMetaType(tx, "relationship");
            HashSet<AttributeType> attributeTypes = SchemaManager.getTypesOfMetaType(tx, "attribute");
            this.storage = new IgniteConceptIdStore(entityTypes, relationshipTypes, attributeTypes);
        }

        this.dataStrategies = SpecificStrategyFactory.getSpecificStrategy(this.executionName, this.rand, this.storage);
    }

    public void generate(int numConceptsLimit) {

        RouletteWheel<RouletteWheel<TypeStrategyInterface>> operationStrategies = this.dataStrategies.getStrategy();
        /*
        This method can be called multiple times, with a higher numConceptsLimit each time, so that the generation can be
        effectively paused while benchmarking takes place
        */

        GeneratorFactory gf = new GeneratorFactory();
        int conceptTotal = this.storage.total();

        while (conceptTotal < numConceptsLimit) {
            System.out.printf("---- Iteration %d ----\n", this.iteration);
            try (Grakn.Transaction tx = session.transaction(GraknTxType.WRITE)) {

                //TODO Deal with this being an Object. TypeStrategy should be/have an interface for this purpose?
                TypeStrategyInterface typeStrategy = operationStrategies.next().next();
                System.out.print("Generating instances of concept type \"" + typeStrategy.getTypeLabel() + "\"\n");

                GeneratorInterface generator = gf.create(typeStrategy, tx); // TODO Can we do without creating a new generator each iteration

                System.out.println("Using generator " + generator.getClass().toString());
                Stream<Query> queryStream = generator.generate();
                
                this.processQueryStream(queryStream);

                iteration++;
                conceptTotal = this.storage.total();
                System.out.printf(String.format("---- %d concepts ----\n", conceptTotal), this.iteration);
                tx.commit();
            }
        }
    }

    private void processQueryStream(Stream<Query> queryStream) {
        /*
        Make the data insertions from the stream of queries generated
         */
        queryStream.map(q -> (InsertQuery) q)
                .forEach(q -> {
                    List<ConceptMap> insertions = q.execute();
                    insertions.forEach(insert -> {
                        HashSet<Concept> insertedConcepts = InsertionAnalysis.getInsertedConcepts(q, insertions);
                        if (insertedConcepts.isEmpty()) {
                            throw new RuntimeException("No concepts were inserted");
                        }
                        insertedConcepts.forEach(concept -> this.storage.add(concept));
                    });
                });
    }
}
