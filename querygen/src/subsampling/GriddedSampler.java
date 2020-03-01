/*
 * Copyright (C) 2020 Grakn Labs
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 */

package grakn.benchmark.querygen.subsampling;

import grakn.benchmark.querygen.VectorisedQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * Split the vector space into a grid, assign each Vectorisable element a coordinate in the n-dimensional grid
 * Then sample from the populate grids
 */
public class GriddedSampler {

    private int divisionsPerAxis;
    private List<VectorisedQuery> population;

    private Map<GridCoordinate, List<VectorisedQuery>> coordinateMembers = new HashMap<>();

    public GriddedSampler(int divisionsPerAxis, List<VectorisedQuery> population) {
        this.divisionsPerAxis = divisionsPerAxis;
        this.population = population;
    }

    public void calculateGrid() {
        // do actual work
        List<Double> maxVector = calculateMaximumVector(this.population);

        for (VectorisedQuery member : population) {
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

    public int numberPopulatedGridCoordinates() {
        return coordinateMembers.size();
    }

    private List<Double> calculateMaximumVector(List<VectorisedQuery> population) {
        List<Double> maxVector = population.get(0).asVector();

        for (VectorisedQuery member : population) {
            List<Double> asVector = member.asVector();
            for (int i = 0; i < maxVector.size(); i++) {
                if (asVector.get(i) > maxVector.get(i)) {
                    maxVector.set(i, asVector.get(i));
                }
            }
        }

        return maxVector;
    }

    public List<VectorisedQuery> getSamples(int numSamples, Random random) {
        List<GridCoordinate> populatedCoordinates = new ArrayList<>(coordinateMembers.keySet());
        Collections.shuffle(populatedCoordinates, random);
        List<VectorisedQuery> samples = new ArrayList<>();
        for (int i = 0; i < numSamples; i++) {
            List<VectorisedQuery> subsamplable = coordinateMembers.get(populatedCoordinates.get(i%populatedCoordinates.size()));
            int subIndex = random.nextInt(subsamplable.size());
            samples.add(subsamplable.get(subIndex));
        }

        return samples;
    }


    private static class GridCoordinate {
        List<Integer> coordinates;

        GridCoordinate(List<Integer> coordinates) {
            this.coordinates = coordinates;
        }

        @Override
        public int hashCode() {
            return this.coordinates.hashCode();
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GridCoordinate) {
                return ((GridCoordinate) o).coordinates.equals(this.coordinates);
            }
            return false;
        }
    }
}
