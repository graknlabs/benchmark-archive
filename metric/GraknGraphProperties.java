package grakn.benchmark.metric;

import grakn.core.GraknTxType;
import grakn.core.Keyspace;
import grakn.core.client.Grakn;
import grakn.core.graql.Syntax;
import grakn.core.graql.answer.ConceptSetMeasure;
import grakn.core.graql.answer.Value;
import grakn.core.util.SimpleURI;
import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class GraknGraphProperties implements GraphProperties {

    Grakn client;
    Grakn.Session session;
    Grakn.Transaction tx;

    public GraknGraphProperties(String uri, String keyspace) {
        this.client = new Grakn(new SimpleURI(uri));
        this.session = client.session(Keyspace.of(keyspace));
    }

    private Grakn.Transaction getTx(boolean useWriteTx) {
        if (useWriteTx) {
            return session.transaction(GraknTxType.WRITE);
        } else {
            return session.transaction(GraknTxType.READ);
        }
    }


    @Override
    public long maxDegree() {
        return 0;
    }

    @Override
    public Stream<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean requireAtLeastThreeUniqueVertices) {
        return null;
    }

    @Override
    public Stream<Pair<Integer, Integer>> connectedVertexDegrees() {
        return null;
    }

    @Override
    public Stream<Long> vertexDegree() {

        // TODO `compute centrality using degree` doesn't return 0 for disconnected entities

        // TODO do we need inference enabled here?
        Grakn.Transaction tx = getTx(false);

        List<ConceptSetMeasure> answerMap = tx.graql().compute(Syntax.Compute.Method.CENTRALITY).of("vertex").execute();

        // repeatedly emit the degree
        Stream<Long> nonzeroDegrees = answerMap.stream().map(
                // take each degree and the number of vertices associated with it, then emit the degree that many times
                conceptSetMeasure -> IntStream.range(0, conceptSetMeasure.set().size()).
                        mapToLong(i -> conceptSetMeasure.measurement().longValue())
        ).flatMap(e -> e.boxed()); // convert LongStream to Stream<Long>

        long numNonzeroDegrees = answerMap.stream().map(conceptSetMeasure -> conceptSetMeasure.set().size()).reduce((a,b) -> a+b).orElse(0);

        // count total number of vertices to see how many have degree == 0
        List<Value> conceptCounts = tx.graql().compute(Syntax.Compute.Method.COUNT).in("vertex").execute();
        long totalVertices = conceptCounts.get(0).asValue().number().longValue();

        return Stream.concat(nonzeroDegrees, IntStream.range(0, (int)(totalVertices-numNonzeroDegrees)).map(i->0).asLongStream().boxed());
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        return null;
    }
}
