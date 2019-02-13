package grakn.benchmark.profiler.generator.provider.concept;

import grakn.core.concept.ConceptId;

import java.util.Iterator;

public interface ConceptIdProvider extends Iterator<ConceptId> {
    boolean hasNextN(int n);
}
