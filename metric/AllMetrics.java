package grakn.benchmark.metric;

import java.util.Arrays;

public class AllMetrics {
    public static void main(String[] args) {
        String keyspaceName = "societal_model_5k_scale_variant";
        System.out.println("Analyzing societal model 5k scale invariant");
        GraknGraphProperties graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        double computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        System.out.println("Global transitivity: " + computedTransitivity);

        double assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        long[] degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 100});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));



        keyspaceName = "societal_model_10k_scale_variant";
        System.out.println("Analyzing societal model 10k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 100});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));



        keyspaceName = "societal_model_20k_scale_variant";
        System.out.println("Analyzing societal model 20k scale invariant");
        graphProperties = new GraknGraphProperties("localhost:48555", keyspaceName);

        computedTransitivity = GlobalTransitivity.computeTransitivity(graphProperties);
        System.out.println("Global transitivity: " + computedTransitivity);

        assortativity = Assortativity.computeAssortativity(Assortativity.jointDegreeOccurrence(graphProperties));
        System.out.println("Degree assortativity: " + assortativity);

        degreeDistribution = DegreeDistribution.discreteDistribution(graphProperties, new double[] {0, 25, 50, 75, 100});
        System.out.println("Degree distribution:");
        System.out.println(String.join(" ", Arrays.toString(degreeDistribution)));
    }
}
