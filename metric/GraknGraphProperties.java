package grakn.benchmark.metric;

import grakn.core.client.GraknClient;
import grakn.core.concept.Concept;
import grakn.core.concept.answer.ConceptMap;
import grakn.core.concept.answer.ConceptSetMeasure;
import grakn.core.concept.answer.Numeric;
import graql.lang.Graql;
import graql.lang.query.GraqlCompute;
import graql.lang.query.GraqlGet;
import org.apache.commons.math3.util.Pair;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static graql.lang.Graql.var;


public class GraknGraphProperties implements GraphProperties {

    String uri;
    String keyspace;
    GraknClient client;
    GraknClient.Session session;

    public GraknGraphProperties(String uri, String keyspace) {
        this.uri = uri;
        this.keyspace = keyspace;
        this.client = new GraknClient(uri);
        this.session = client.session(keyspace);
    }

    public GraknGraphProperties copy() {
        return new GraknGraphProperties(uri, keyspace);
    }


    public void close() {
        this.session.close();
    }

    private GraknClient.Transaction getTx(boolean useWriteTx) {
        if (useWriteTx) {
            return session.transaction().write();
        } else {
            return session.transaction().read();
        }
    }

    @Override
    public long maxDegree() {
        // TODO do we need inference here?
        try (GraknClient.Transaction tx = getTx(false)) {
            GraqlCompute.Centrality centralities = Graql.compute().centrality().of("entity");
            return tx.stream(centralities)
                    .map(conceptSetMeasure -> conceptSetMeasure.measurement().longValue())
                    .max(Comparator.naturalOrder())
                    .orElse(0l);
        }
    }

    @Override
    public List<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean edgeCardinalitesGreaterThanOne) {
        List<Pair<Set<String>, Set<String>>> edgePairs;


        // TODO do we need inference here?
        try (GraknClient.Transaction tx = getTx(false)) {

            // `match $r1 ($x) isa edge; $r2 ($x) isa edge; $r1 != $r2; get $r1, $r2;`
            GraqlGet query = Graql.match(
                    var("r1").isa("relation").rel(var("x")),
                    var("r2").isa("relation").rel(var("x")),
                    var("r1").neq(var("r2")),
                    var("x").isa("entity")
            ).get("r1", "r2");

            Stream<Pair<Set<String>, Set<String>>> edgePairsStream = tx.stream(query).map(
                    conceptMap -> {
                        Concept r1 = conceptMap.get("r1");
                        Concept r2 = conceptMap.get("r2");

                        // retrieve all the entities attached to r1, since r1 may be a hyperedge
                        Set<String> edge1 = r1.asRelation()
                                .rolePlayers()
                                .filter(thing -> thing.isEntity())
                                .map(thing -> thing.id().toString())
                                .collect(Collectors.toSet());

                        // retrieve all the entities attached to r2, since r2 may be a hyperedge
                        Set<String> edge2 = r2.asRelation()
                                .rolePlayers()
                                .filter(thing -> thing.isEntity())
                                .map(thing -> thing.id().toString())
                                .collect(Collectors.toSet());

                        return new Pair<>(edge1, edge2);
                    });

            if (edgeCardinalitesGreaterThanOne) {
                // filter out edge pairs that don't touch at least 3 vertices
                // we use a slightly stronger condition: each edge needs to touch more than 1 vertex each
                // this also eliminates
                edgePairsStream = edgePairsStream.filter(
                        pair -> (pair.getFirst().size() > 1 && pair.getSecond().size() > 1)
                );
            }
            edgePairs = edgePairsStream.collect(Collectors.toList());
        }
        return edgePairs;
    }

    @Override
    public List<Pair<Long, Long>> connectedVertexDegrees() {
        List<Pair<Long, Long>> connectedVertexDegrees;

        // TODO do we need inference enabled here?
        try (GraknClient.Transaction tx = getTx(false)) {
            // compute degree of each entity
            // compute degree of each  entitiy
            // returns mapping { deg_n : set(entity ids) }
            // does NOT return degree 0 entity IDs
            GraqlCompute.Centrality graqlCompute = Graql.compute().centrality().of("entity").in("relation");

            // create a mapping from ID -> degree (not containing 0 degree entities)
            Map<String, Long> entityDegreeMap = tx.stream(graqlCompute)
                    .map(conceptSetMeasure ->
                            conceptSetMeasure.set().stream()
                                    .map(conceptId -> new Pair<>(conceptId.toString(), conceptSetMeasure.measurement().longValue()))
                    )
                    .flatMap(e -> e)
                    .collect(Collectors.toMap(pair -> pair.getFirst(), pair -> pair.getSecond()));

            // query for all connected entities, which by definition never have degree 0
            GraqlGet edgeList = Graql.match(
                    var("x").isa("entity"),
                    var("y").isa("entity"),
                    var().isa("relation").rel(var("x")).rel(var("y"))
            ).get("x", "y");

            connectedVertexDegrees = tx.stream(edgeList)
                    .map(conceptMap -> new Pair<>(
                                    entityDegreeMap.get(conceptMap.get("x").id().toString()),
                                    entityDegreeMap.get(conceptMap.get("y").id().toString())
                            )
                    )
                    .collect(Collectors.toList());
        }
        return connectedVertexDegrees;
    }

    @Override
    public List<Long> vertexDegree() {

        Stream<Long> nonzeroDegrees = Stream.empty();
        long numNonzeroDegrees = 0;
        long numZeroDegrees = 0;

        // TODO do we need inference enabled here?
        try (GraknClient.Transaction tx = getTx(false)) {

            // compute degree of each  entitiy
            // returns mapping { deg_n : set(entity ids) }
            // does NOT return degree 0 entity IDs
            GraqlCompute.Centrality graqlCompute = Graql.compute().centrality().of("entity");

            // collect it to a list so we don't have to execute it twice
            List<ConceptSetMeasure> answerMap = tx.execute(graqlCompute);

            // repeatedly emit the degree
            nonzeroDegrees = answerMap.stream().map(
                    // take each degree and the number of vertices associated with it, then emit the degree that many times
                    conceptSetMeasure ->
                            IntStream.range(0, conceptSetMeasure.set().size()).
                                    mapToLong(i -> conceptSetMeasure.measurement().longValue())

            ).flatMap(e -> e.boxed()); // convert Stream<LongStream> to Stream<Long>

            // count how many entities have non-zero degree
            numNonzeroDegrees = answerMap.stream().
                    map(conceptSetMeasure -> conceptSetMeasure.set().size()).
                    reduce((a, b) -> a + b).
                    orElse(0);

            // ***** `compute centrality using degree` doesn't return 0 for disconnected entities *****
            // count total number of vertices to see how many have degree == 0
            List<Numeric> conceptCounts = tx.execute(Graql.compute().count().in("entity"));
            long totalVertices = conceptCounts.get(0).number().longValue();
            // compute how many vertices have zero degree
            numZeroDegrees = totalVertices - numNonzeroDegrees;
        }

        return Stream.concat(nonzeroDegrees,
                IntStream.range(0, (int) (numZeroDegrees)).map(i -> 0).asLongStream().boxed())
                .collect(Collectors.toList());
    }

    @Override
    public Set<String> neighbors(String vertexId) {
        Set<String> neighborIds = new HashSet<>();

        // TODO do we need inference enabled here?
        try (GraknClient.Transaction tx = getTx(false)) {
            List<ConceptMap> neighbors = tx.execute(
                    Graql.match(
                            var("x").id(vertexId),
                            var("r").rel(var("x")).rel(var("y"))
                    ).get("y"));

            for (ConceptMap conceptMap : neighbors) {
                neighborIds.add(conceptMap.get("y").id().toString());
            }
        }
        return neighborIds;
    }

    public long numVertices() {
        try (GraknClient.Transaction tx = getTx(false)) {
            return tx.stream(Graql.compute().count().in("entity")).findFirst().get().number().longValue();
        }
    }
}
