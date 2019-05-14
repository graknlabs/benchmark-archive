package grakn.benchmark.profiler.util;

import grakn.client.GraknClient;
import grakn.core.concept.answer.ConceptMap;
import graql.lang.query.GraqlInsert;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ConcurrentDataLoader {

    /**
     * Execute a set of graql insert queries into each given keyspace
     * Return the number of concepts imported into each keyspace (not in total)
     *
     * @return concepts imported per keyspace (all named variables will be counted, so eg. likely excluding implicit rels)
     */
    public static int concurrentDataImport(GraknClient client, List<String> keyspaces, List<GraqlInsert> dataImportQueries, int concurrency) {
        int importedConcepts = 0;
        for (String keyspace : keyspaces) {
            importedConcepts = 0;
            GraknClient.Session session = client.session(keyspace);
            try {
                List<List<GraqlInsert>> queryLists = splitList(dataImportQueries, concurrency);

                ExecutorService executorService = Executors.newFixedThreadPool(concurrency);

                List<CompletableFuture<Integer>> asyncInsertions = new ArrayList<>();

                queryLists.forEach((queryList) -> {
                    CompletableFuture<Integer> asyncInsert = CompletableFuture.supplyAsync(() -> {
                        int insertedConcepts = 0;
                        for (GraqlInsert insertQuery : queryList) {
                            GraknClient.Transaction writeTransaction = session.transaction().write();
                            List<ConceptMap> insertedIds = writeTransaction.execute(insertQuery);
                            writeTransaction.commit();
                            insertedConcepts += insertedIds.stream().map(map -> map.concepts().size()).reduce((a, b) -> a + b).orElse(0);
                        }
                        return insertedConcepts;
                    }, executorService);
                    asyncInsertions.add(asyncInsert);
                });

                for (CompletableFuture<Integer> insertFuture : asyncInsertions) {
                    importedConcepts += insertFuture.get();
                }
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
            session.close();
        }
        return importedConcepts;
    }

    private static List<List<GraqlInsert>> splitList(List<GraqlInsert> queries, int divisions) {
        List<List<GraqlInsert>> parts = new ArrayList<>();
        final int N = queries.size();
        for (int i = 0; i < N; i += divisions) {
            parts.add(new ArrayList<>(queries.subList(i, Math.min(N, i + divisions))));
        }
        return parts;
    }
}
