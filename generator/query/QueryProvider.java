package grakn.benchmark.generator.query;

import grakn.benchmark.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.generator.strategy.AttributeStrategy;
import grakn.benchmark.generator.strategy.EntityStrategy;
import grakn.benchmark.generator.strategy.RelationStrategy;
import grakn.benchmark.generator.strategy.TypeStrategy;
import graql.lang.query.GraqlInsert;

import java.util.Iterator;

public class QueryProvider {
    private final DataGeneratorDefinition dataGeneratorDefinition;

    public QueryProvider(DataGeneratorDefinition dataGeneratorDefinition) {
        this.dataGeneratorDefinition = dataGeneratorDefinition;

    }

    public Iterator<GraqlInsert> nextQueryBatch() {
        QueryGenerator queryGenerator;
        TypeStrategy typeStrategy = dataGeneratorDefinition.sampleNextStrategy();


        if (typeStrategy instanceof EntityStrategy) {
            queryGenerator = new EntityGenerator((EntityStrategy) typeStrategy);
        } else if (typeStrategy instanceof RelationStrategy) {
            queryGenerator = new RelationGenerator((RelationStrategy) typeStrategy);
        } else if (typeStrategy instanceof AttributeStrategy) {
            queryGenerator = new AttributeGenerator((AttributeStrategy) typeStrategy);
        } else {
            throw new RuntimeException("Couldn't find a matching Generator for this strategy");
        }
        return queryGenerator.generate();
    }

}
