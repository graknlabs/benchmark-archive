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

import org.apache.commons.math3.util.Pair;

import java.util.List;
import java.util.Set;

public interface GraphProperties {

     void close();
     GraphProperties copy();

     long maxDegree();

     /*
     stream the undirected edge's endpoint's degrees twice each - in each direction
     IE. an edge between a vertex of degree 1 and degree 2 produces two connected vertex degrees: (1,2) and (2,1)
      */
     List<Pair<Long, Long>> connectedVertexDegrees();
     List<Long> vertexDegree();

     /*
     Stream pairs of sets of vertex/entity IDs, each of which represents a hyperedge
     In other words, get all pairs of connected (hyper)edges sharing at least one vertex - this
     pair should only be returned once!

     if edgeCardinalityGreaterThanOne is set to true, then we ignore pure looping edges
      */
     List<Pair<Set<String>, Set<String>>> connectedEdgePairs(boolean edgeCardinalitiesGreaterThanOne);
     Set<String> neighbors(String vertexId);
}

