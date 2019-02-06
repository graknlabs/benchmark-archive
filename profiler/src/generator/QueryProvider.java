package grakn.benchmark.profiler.generator;

import grakn.benchmark.profiler.generator.concept.AttributeGenerator;
import grakn.benchmark.profiler.generator.concept.EntityGenerator;
import grakn.benchmark.profiler.generator.concept.QueryGenerator;
import grakn.benchmark.profiler.generator.concept.RelationshipGenerator;
import grakn.benchmark.profiler.generator.definition.DataGeneratorDefinition;
import grakn.benchmark.profiler.generator.strategy.AttributeStrategy;
import grakn.benchmark.profiler.generator.strategy.EntityStrategy;
import grakn.benchmark.profiler.generator.strategy.RelationshipStrategy;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;
import grakn.core.graql.InsertQuery;

import java.util.stream.Stream;

public class QueryProvider {
    private final DataGeneratorDefinition dataGeneratorDefinition;

    public QueryProvider(DataGeneratorDefinition dataGeneratorDefinition) {
        this.dataGeneratorDefinition = dataGeneratorDefinition;

    }

    public Stream<InsertQuery> nextQueryBatch(){
        QueryGenerator<? extends TypeStrategy> queryGenerator;
        TypeStrategy typeStrategy = dataGeneratorDefinition.sampleNextStrategy();


        if (typeStrategy instanceof EntityStrategy) {
            queryGenerator = new EntityGenerator((EntityStrategy) typeStrategy);
        } else if (typeStrategy instanceof RelationshipStrategy) {
            queryGenerator = new RelationshipGenerator((RelationshipStrategy) typeStrategy);
        } else if (typeStrategy instanceof AttributeStrategy) {
            queryGenerator = new AttributeGenerator((AttributeStrategy) typeStrategy);
        } else {
            throw new RuntimeException("Couldn't find a matching Generator for this strategy");
        }
        return queryGenerator.generate();
    }

}
