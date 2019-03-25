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

package grakn.benchmark.metric;

import java.util.Arrays;

public class AllMetrics {
    public static void main(String[] args) {
        String keyspaceName = "societal_model_2k_scale_variant";
        System.out.println("Analyzing societal model 2k scale invariant");
        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);
        System.out.println("Total entities: " + graphProperties.numVertices());
//        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
//        System.out.println("Global transitivity: " + computedTransitivity);

        double assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        long[] degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 95});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
        graphProperties.close();


        keyspaceName = "societal_model_5k_scale_variant";
        System.out.println("Analyzing societal model 5k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);
        System.out.println("Total entities: " + graphProperties.numVertices());
//        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
//        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 95});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
        graphProperties.close();


        keyspaceName = "societal_model_10k_scale_variant";
        System.out.println("Analyzing societal model 10k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);
        System.out.println("Total entities: " + graphProperties.numVertices());

//        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
//        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 95});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
        graphProperties.close();



        keyspaceName = "societal_model_20k_scale_variant";
        System.out.println("Analyzing societal model 20k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);
        System.out.println("Total entities: " + graphProperties.numVertices());

//        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
//        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 95});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
        graphProperties.close();



        keyspaceName = "societal_model_40k_scale_variant";
        System.out.println("Analyzing societal model 40k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);
        System.out.println("Total entities: " + graphProperties.numVertices());

//        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
//        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 95});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
        graphProperties.close();
    }
}
