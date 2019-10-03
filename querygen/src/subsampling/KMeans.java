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

package grakn.benchmark.querygen.subsampling;


import grakn.benchmark.querygen.VectorisedQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implment K-Means clustering
 */
public class KMeans {

    private static Logger LOG = LoggerFactory.getLogger(KMeans.class);

    List<Cluster> clusters;
    List<VectorisedQuery> items;

    public KMeans(List<VectorisedQuery> items, int clusters) {
        this.items = items;

        int initialisationAttemptsLimit = 1000;
        int initialisations = 0;
        int bestEmptyClusters = clusters;
        List<Cluster> bestClusters = null;

        // try to find a good initial assignment of vectors to clusters that leaves as few as possible
        // empty clusters
        while (bestEmptyClusters > 0 && initialisations < initialisationAttemptsLimit) {
            Set<VectorisedQuery> picked = new HashSet<>();

            Collections.shuffle(items);

            this.clusters = new ArrayList<>(clusters);
            int index = 0;
            while (this.clusters.size() != clusters && index < items.size()) {
                if (!picked.contains(items.get(index))) {
                    VectorisedQuery chosen = items.get(index);
                    picked.add(chosen);
                    // init clusters with the value of the item chosen
                    this.clusters.add(new Cluster(new Centroid(chosen.asVector())));
                }
                index++;
            }

            assignItemsToClusters(this.clusters, items);
            int emptyClusters = numberOfEmptyClusters(this.clusters);
            LOG.trace("Initialising clusters: "  + emptyClusters + " out of " + clusters + " are empty.");

            // keep the lowest number of empty clusters found
            if (emptyClusters < bestEmptyClusters) {
                bestEmptyClusters = emptyClusters;
                bestClusters = this.clusters;
            }

            initialisations++;
        }
        LOG.trace("Best non-empty clusters found: " + (clusters - bestEmptyClusters));
        this.clusters = bestClusters.stream().filter(cluster -> !cluster.members.isEmpty()).collect(Collectors.toList());
    }


    public int run(int maxSteps) {
        int step = 0;
        boolean settled = false;

        while (step < maxSteps && !settled) {
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

    public List<Cluster> getClusters() {
        return clusters;
    }

    private boolean clustersUnchanged(List<Cluster> newClusters, List<Cluster> oldClusters) {
        Set<List<Double>> newClusterCentroids = newClusters.stream().map(cluster -> cluster.newCentroid().asVector()).collect(Collectors.toSet());
        Set<List<Double>> oldClusterCentroids = oldClusters.stream().map(cluster -> cluster.newCentroid().asVector()).collect(Collectors.toSet());

        return newClusterCentroids.equals(oldClusterCentroids);
    }

    private int numberOfEmptyClusters(List<Cluster> clusters) {
        int empty = 0;
        for (Cluster c : clusters) {
            if (c.members.isEmpty()) {
                empty++;
            }
        }

        return empty;
    }

    private void assignItemsToClusters(List<Cluster> clusters, List<VectorisedQuery> items) {
        // assign new ownerships
        for (VectorisedQuery vectorisable : items) {
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
    }

    private double distance(VectorisedQuery a, Centroid b) {
        double distance = 0.0;
        List<Double> dA = a.asVector();
        List<Double> dB = b.asVector();
        for (int i = 0; i < dA.size(); i++) {
            distance += Math.pow(dA.get(i) - dB.get(i), 2);
        }
        return Math.pow(distance, 0.5);
    }

    //VisibleForTesting
    public class Cluster {
        private List<VectorisedQuery> members = new ArrayList<>();
        private Centroid centroid;
        private Centroid newCentroid = null;

        Cluster(Centroid centroid) {
            this.centroid = centroid;
        }

        public List<VectorisedQuery> getMembers() {
            return members;
        }

        public void add(VectorisedQuery a) {
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

    private class Centroid {
        List<Double> vector;

        Centroid(List<Double> vector) {
            this.vector = vector;
        }

        List<Double> asVector() {
            return vector;
        }

        @Override
        public int hashCode() {
            return vector.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof Centroid) {
                return this.vector.equals(((Centroid)o).asVector());
            } else if (o instanceof VectorisedQuery) {
                return this.vector.equals(((VectorisedQuery)o).asVector());
            }
            return false;
        }
    }
}
