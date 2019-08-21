package grakn.benchmark.querygen.subsampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class GriddedSampler<K extends Vectorisable> {

    private int divisionsPerAxis;
    private List<K> population;

    private Map<GridCoordinate, List<K>> coordinateMembers = new HashMap<>();

    public GriddedSampler(int divisionsPerAxis, List<K> population) {
        this.divisionsPerAxis = divisionsPerAxis;
        this.population = population;
    }

    public void calculateGrid() {
        // do actual work
        List<Double> maxVector = calculateMaximumVector(this.population);

        for (K member : population) {
            List<Double> vector = member.asVector();
            List<Integer> coordinates = new ArrayList<>();
            // normalise, then form into bins
            for (int i = 0; i < vector.size(); i++) {
                Double normalisedValue = vector.get(i)/maxVector.get(i);
                int bin = (int)(normalisedValue * divisionsPerAxis);
                coordinates.add(bin);
            }

            GridCoordinate coordinate = new GridCoordinate(coordinates);
            coordinateMembers.putIfAbsent(coordinate, new ArrayList<>());
            coordinateMembers.get(coordinate).add(member);
        }
    }

    private List<Double> calculateMaximumVector(List<K> population) {
        List<Double> maxVector = population.get(0).asVector();

        for (K member : population) {
            List<Double> asVector = member.asVector();
            for (int i = 0; i < maxVector.size(); i++) {
                if (asVector.get(i) > maxVector.get(i)) {
                    maxVector.set(i, asVector.get(i));
                }
            }
        }

        return maxVector;
    }

    public List<K> getSamples(int numSamples, Random random) {
        List<GridCoordinate> populatedCoordinates = new ArrayList<>(coordinateMembers.keySet());
        List<K> samples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            int index = random.nextInt(populatedCoordinates.size());
            List<K> subsamplable = coordinateMembers.get(populatedCoordinates.get(index));

            int subIndex = random.nextInt(subsamplable.size());
            samples.add(subsamplable.get(subIndex));
        }

        return samples;
    }


    private static class GridCoordinate {
        List<Integer> coordinates;

        public GridCoordinate(List<Integer> coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public int hashCode() {
            return this.coordinates.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            return ((GridCoordinate) o).coordinates.equals(this.coordinates);
        }

    }
}
