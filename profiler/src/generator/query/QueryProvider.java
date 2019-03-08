package grakn.benchmark.profiler.generator.query;

import grakn.benchmark.profiler.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationStrategy;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;
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
