package grakn.benchmark.querygen;

import grakn.benchmark.querygen.kmeans.KMeans;
import grakn.client.GraknClient;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class QuerySampler {

    /**
     *
     * @param generate - how many queries to generate overall, before downsampling
     * @param targetSamples - maximum, target number of queries to downsample to
     * @param samplesPerCluster - how many samples to choose from each KMeans cluster
     * @return
     */
    public static List<VectorisedQuery> querySampleKMeans(GraknClient.Session session, int generate, int targetSamples, int samplesPerCluster) {
        System.out.println("Starting query generation");
        List<VectorisedQuery> rawQueries = parallelQueryGeneration(session, generate, 4);

        int clusters = targetSamples/samplesPerCluster;
        System.out.println("Initialising KMeans...");
        KMeans<VectorisedQuery> clustering = new KMeans<>(rawQueries, clusters);
        System.out.println("Running KMeans...");
        int steps = clustering.run(100);
        System.out.println("Clustering completed in " + steps + " iterations");
        List<KMeans.Cluster<VectorisedQuery>> computedClusters = clustering.getClusters();

        List<VectorisedQuery> sampledQueries = sampleFromClusters(computedClusters, samplesPerCluster);

        System.out.println(sampledQueries);
        return sampledQueries;
    }

    private static List<VectorisedQuery> sampleFromClusters(List<KMeans.Cluster<VectorisedQuery>> computedClusters, int samplesPerCluster) {
        List<VectorisedQuery> sampled = new ArrayList<>();
        for (KMeans.Cluster<VectorisedQuery> cluster : computedClusters) {
            List<VectorisedQuery> members = cluster.getMembers();

            Collections.shuffle(members);
            for (int i = 0; i < samplesPerCluster; i++) {
                sampled.add(members.get(i));
            }
        }
        return sampled;
    }

    private static List<VectorisedQuery> parallelQueryGeneration(GraknClient.Session session, int target, int concurrency)  {
        int queriesPerThread = target/concurrency;

        ExecutorService executor = Executors.newFixedThreadPool(concurrency);

        List<Future<List<VectorisedQuery>>> executors = new ArrayList<>();
        for (int i = 0; i < concurrency; i++) {
            executors.add(executor.submit(() -> {
                QueryGenerator generator = new QueryGenerator(session);
                return generator.generate(queriesPerThread);
            }));
        }

        List<VectorisedQuery> allQueries = new ArrayList<>();
        for (Future<List<VectorisedQuery>> future : executors) {
            try {
                allQueries.addAll(future.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        return allQueries;
    }


}
