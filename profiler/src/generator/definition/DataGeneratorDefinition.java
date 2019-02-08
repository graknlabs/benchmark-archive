package grakn.benchmark.profiler.generator.definition;


import grakn.benchmark.profiler.generator.util.WeightedPicker;
import grakn.benchmark.profiler.generator.strategy.TypeStrategy;


/**
 * This describes the way Benchmark will generate data for a specific schema
 */

public abstract class DataGeneratorDefinition {

    protected abstract WeightedPicker<WeightedPicker<TypeStrategy>> getDefinition();

    public TypeStrategy sampleNextStrategy(){
        return getDefinition().sample().sample();
    }

}
