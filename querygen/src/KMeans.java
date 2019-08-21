package grakn.benchmark.querygen;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class KMeans {


    List<Cluster> clusters;
    List<? extends Vectorisable> items;

    public KMeans(List<? extends Vectorisable> items, int clusters) {
        this.items = items;


        while (this.clusters == null || anyEmptyClusters(this.clusters)) {
            Set<Vectorisable> picked = new HashSet<>();

            Collections.shuffle(items);

            // initialise n clusters;
            this.clusters = new ArrayList<>(clusters);
            int index = 0;
            while (this.clusters.size() != clusters && index < items.size()) {
                if (!picked.contains(items.get(index))) {
                    Vectorisable chosen = items.get(index);
                    picked.add(chosen);
                    // init clusters with the value of the item chosen
                    this.clusters.add(new Cluster(new Centroid(chosen.asVector())));
                }
                index++;
            }

            assignItemsToClusters(this.clusters, items);
            System.out.println(anyEmptyClusters(this.clusters));
        }
    }

    public int run(int maxSteps) {
        int step = 0;
        boolean settled = false;

        while (step < maxSteps && !settled) {
            // DO k-means

            // create new clusters based on centroids
            List<Cluster> newClusters = new ArrayList<>(clusters.size());
            for (Cluster c : clusters) {
                newClusters.add(new Cluster(c.newCentroid()));
            }

            assignItemsToClusters(newClusters, items);

            settled = clustersUnchanged(newClusters, this.clusters);

            this.clusters = newClusters;

            step++;
        }
        return step;
    }

    private boolean clustersUnchanged(List<Cluster> newClusters, List<Cluster> oldClusters) {
        Set<List<Double>> newClusterCentroids = newClusters.stream().map(cluster -> cluster.newCentroid().asVector()).collect(Collectors.toSet());
        Set<List<Double>> oldClusterCentroids = oldClusters.stream().map(cluster -> cluster.newCentroid().asVector()).collect(Collectors.toSet());

        return newClusterCentroids.equals(oldClusterCentroids);
    }

    private boolean anyEmptyClusters(List<Cluster> clusters) {
        for (Cluster c : clusters) {
            if (c.members.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void assignItemsToClusters(List<Cluster> clusters, List<? extends Vectorisable> items) {
        // assign new ownerships
        for (Vectorisable vectorisable : items) {
            // find the closest cluster
            Cluster closestCluster = clusters.get(0);
            double closestDistance = distance(vectorisable, closestCluster.centroid());
            for (int i = 1; i < clusters.size(); i++) {
                double distance = distance(vectorisable, clusters.get(i).centroid());
                if (distance < closestDistance) {
                    closestCluster = clusters.get(i);
                    closestDistance = distance;
                }
            }

            closestCluster.add(vectorisable);
        }
        System.out.println("assigned");
    }

    List<Cluster> getClusters() {
        return clusters;
    }


    static class Cluster {
        private List<Vectorisable> members = new ArrayList<>();
        private Centroid centroid;
        private Centroid newCentroid = null;

        Cluster(Centroid centroid) {
            this.centroid = centroid;
        }

        public void add(Vectorisable a) {
            members.add(a);
        }

        // retrieve the centroid that was initialised
        Centroid centroid() {
            return centroid;
        }

        Centroid newCentroid() {
            if (newCentroid == null) {
                // shortcut to initialise N arrays to 0.0
                double[] vector = new double[centroid.asVector().size()];

                for (int i = 0; i < members.size(); i++) {
                    List<Double> memberVector = members.get(i).asVector();
                    for (int j = 0; j < vector.length; j++) {
                        vector[j] = vector[j] + memberVector.get(j);
                    }
                }

                // take the mean of each element
                for (int i = 0; i < vector.length; i++) {
                    vector[i] = vector[i] / members.size();
                }

                newCentroid = new Centroid(Arrays.stream(vector).boxed().collect(Collectors.toList()));
            }
            return newCentroid;
        }

    }


    private static class Centroid implements Vectorisable {
        List<Double> vector;

        Centroid(List<Double> vector) {
            this.vector = vector;
        }

        @Override
        public List<Double> asVector() {
            return vector;
        }

        @Override
        public int hashCode() {
            return vector.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return this.vector.equals(((Vectorisable) o).asVector());
        }
    }


    private double distance(Vectorisable a, Vectorisable b) {
        double distance = 0.0;
        List<Double> dA = a.asVector();
        List<Double> dB = b.asVector();
        for (int i = 0; i < dA.size(); i++) {
            distance += Math.pow(dA.get(i) - dB.get(i), 2);
        }
        return Math.pow(distance, 0.5);
    }

}
