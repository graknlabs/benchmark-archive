/*
 *  GRAKN.AI - THE KNOWLEDGE GRAPH
 *  Copyright (C) 2019 Grakn Labs Ltd
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
