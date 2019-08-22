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

import grakn.benchmark.querygen.subsampling.GriddedSampler;
import grakn.benchmark.querygen.subsampling.KMeans;
import grakn.benchmark.querygen.util.Arguments;
import grakn.client.GraknClient;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.ParseException;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

public class QuerySampler {


    public static void main(String[] args) throws ParseException, IOException {
        CommandLine arguments = Arguments.parse(args);
        String graknUri = arguments.getOptionValue(Arguments.GRAKN_URI);
        String keyspace = arguments.getOptionValue(Arguments.KEYSPACE_ARGUMENT);

        int generate = Integer.parseInt(arguments.getOptionValue(Arguments.GENERATE_ARGUMENT));
        int sampled = Integer.parseInt(arguments.getOptionValue(Arguments.SAMPLE_ARGUMENT));

        List<VectorisedQuery> sampledQueries;
        try (GraknClient client = new GraknClient(graknUri);
             GraknClient.Session session = client.session(keyspace)) {
            if (arguments.hasOption(Arguments.KMEANS_SAMPLING_ARGUMENT)) {
                sampledQueries = querySampleKMeans(session, generate, sampled, 2);
            } else {
                sampledQueries = querySampleGridded(session, generate, sampled, 5);
            }
        }

        Path outputPath = Paths.get(arguments.getOptionValue(Arguments.OUTPUT_ARGUMENT));
        Path outputFile = outputPath.resolve("queries_" + keyspace + "_" + sampledQueries.size() + ".gql");
        if (Files.exists(outputFile)) {
            throw new RuntimeException("Output file " + outputFile.toFile() + " exists, aborting");
        }

        writeQueries(outputFile, sampledQueries);

        System.out.println("done");
    }

    private static void writeQueries(Path outputFile, List<VectorisedQuery> sampledQueries) throws IOException {
        outputFile.toFile().createNewFile();
        Files.write(
                outputFile,
                sampledQueries.stream().map(query -> query.graqlQuery.toString()).collect(Collectors.toList())
        );
    }

    /**
     * @param generate          - how many queries to generate overall, before downsampling
     * @param targetSamples     - maximum, target number of queries to downsample to
     * @param samplesPerCluster - how many samples to choose from each KMeans cluster
     * @return
     */
    public static List<VectorisedQuery> querySampleKMeans(GraknClient.Session session, int generate, int targetSamples, int samplesPerCluster) {
        int threads = 4;
        System.out.println("Starting generation of " + generate + " queries in " + threads + " threads");
        List<VectorisedQuery> rawQueries = parallelQueryGeneration(session, generate, threads);

        int clusters = targetSamples / samplesPerCluster;
        System.out.println("Initialising KMeans...");
        KMeans<VectorisedQuery> clustering = new KMeans<>(rawQueries, clusters);
        System.out.println("Running KMeans...");
        int steps = clustering.run(100);
        System.out.println("Clustering completed in " + steps + " iterations");
        List<KMeans.Cluster<VectorisedQuery>> computedClusters = clustering.getClusters();

        List<VectorisedQuery> sampledQueries = sampleFromClusters(computedClusters, samplesPerCluster);

        return sampledQueries;
    }

    public static List<VectorisedQuery> querySampleGridded(GraknClient.Session session, int generate, int targetSamples, int divisionsPerAxis) {
        System.out.println("Starting query generation...");
        List<VectorisedQuery> rawQueries = parallelQueryGeneration(session, generate, 4);
        GriddedSampler<VectorisedQuery> sampler = new GriddedSampler<>(divisionsPerAxis, rawQueries);

        System.out.println("Calculating grid...");
        sampler.calculateGrid();

        System.out.println("Number of populated grid positions: " + sampler.numberPopulatedGridCoordinates());

        Random random = new Random(0);
        List<VectorisedQuery> sampledQueries = sampler.getSamples(targetSamples, random);

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

    private static List<VectorisedQuery> parallelQueryGeneration(GraknClient.Session session, int target, int concurrency) {
        int queriesPerThread = target / concurrency;

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

        executor.shutdown();

        return allQueries;
    }


}
