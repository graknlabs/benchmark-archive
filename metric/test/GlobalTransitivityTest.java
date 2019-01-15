package grakn.benchmark.metric.test;

import grakn.benchmark.metric.GlobalTransitivity;
import grakn.benchmark.metric.GraphProperties;
import org.apache.commons.math3.util.Pair;
import org.junit.Test;
import org.mockito.Mockito;

import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class GlobalTransitivityTest {

    @Test
    public void computeGlobalTransitivityStandardBinaryGraph() {

        GraphProperties mockProperties = Mockito.mock(GraphProperties.class);
        /*
        originating adjacency lists:
        ** only 1 closed triangle **
        adjacency = {
        1: [], # degree 1 connnected to degree 2
        2: [], # degree 1 connected to degree 2
        3: [1, 2],  # degree 2 connected to degree 1 twice
        4: [], # 1 -> 3
        5: [], # 1 -> 3
        6: [4, 5, 7], # 3 -> 1, 1, 2
        7: [], # 2 -> 3, 4
        8: [7, 9, 10], # 4 -> 2, 1, 1, 1
        9: [10], # 2 -> 4, 2
        10: [], # 1 -> 4
        }
        */

        LinkedList<HashSet<String>> edges = new LinkedList<HashSet<String>>();

        // index 0
        HashSet<String> edge = new HashSet<>();
        edge.add("1");
        edge.add("3");
        edges.addLast(edge);

        // index 1
        edge = new HashSet<>();
        edge.add("2");
        edge.add("3");
        edges.addLast(edge);

        // index 2
        edge = new HashSet<>();
        edge.add("4");
        edge.add("6");
        edges.addLast(edge);

        // index 3
        edge = new HashSet<>();
        edge.add("5");
        edge.add("6");
        edges.addLast(edge);

        // index 4
        edge = new HashSet<>();
        edge.add("6");
        edge.add("7");
        edges.addLast(edge);

        // index 5
        edge = new HashSet<>();
        edge = new HashSet<>();
        edge.add("7");
        edge.add("8");
        edges.addLast(edge);

        // index 6
        edge = new HashSet<>();
        edge.add("8");
        edge.add("9");
        edges.addLast(edge);

        // index 7
        edge = new HashSet<>();
        edge.add("8");
        edge.add("10");
        edges.addLast(edge);

        // index 8
        edge = new HashSet<>();
        edge.add("9");
        edge.add("10");
        edges.addLast(edge);

        LinkedList<Pair<Set<String>, Set<String>>> connectedEdgePairs = new LinkedList<>();
        connectedEdgePairs.add( new Pair<>(edges.get(0), edges.get(1)) ); // (1,3) -- (3,2)
        connectedEdgePairs.add( new Pair<>(edges.get(2), edges.get(3)) ); // (4,6) -- (6,5)
        connectedEdgePairs.add( new Pair<>(edges.get(2), edges.get(4)) ); // (4,6) -- (6,7)
        connectedEdgePairs.add( new Pair<>(edges.get(3), edges.get(4)) ); // (5,6) -- (6,7)
        connectedEdgePairs.add( new Pair<>(edges.get(4), edges.get(5)) ); // (6,7) -- (7,8)
        connectedEdgePairs.add( new Pair<>(edges.get(5), edges.get(6)) ); // (7,8) -- (8,9)
        connectedEdgePairs.add( new Pair<>(edges.get(5), edges.get(7)) ); // (7,8) -- (8,10)
        connectedEdgePairs.add( new Pair<>(edges.get(6), edges.get(7)) ); // (9,8) -- (8,10)
        connectedEdgePairs.add( new Pair<>(edges.get(6), edges.get(8)) ); // (9,8) -- (9,10)
        connectedEdgePairs.add( new Pair<>(edges.get(8), edges.get(7)) ); // (9,10) -- (8,10)

        when(mockProperties.connectedEdgePairs(true)).thenReturn(connectedEdgePairs);

        // also need to setup neighbors
        when(mockProperties.neighbors("1")).thenReturn(new HashSet<>(Arrays.asList("3")));
        when(mockProperties.neighbors("2")).thenReturn(new HashSet<>(Arrays.asList("3")));
        when(mockProperties.neighbors("3")).thenReturn(new HashSet<>(Arrays.asList("1", "2")));
        when(mockProperties.neighbors("4")).thenReturn(new HashSet<>(Arrays.asList("6")));
        when(mockProperties.neighbors("5")).thenReturn(new HashSet<>(Arrays.asList("6")));
        when(mockProperties.neighbors("6")).thenReturn(new HashSet<>(Arrays.asList("4", "5", "7")));
        when(mockProperties.neighbors("7")).thenReturn(new HashSet<>(Arrays.asList("6", "8")));
        when(mockProperties.neighbors("8")).thenReturn(new HashSet<>(Arrays.asList("7", "9", "10")));
        when(mockProperties.neighbors("9")).thenReturn(new HashSet<>(Arrays.asList("8", "10")));
        when(mockProperties.neighbors("10")).thenReturn(new HashSet<>(Arrays.asList("8", "9")));

        when(mockProperties.copy()).thenReturn(mockProperties);

        double transitivity = GlobalTransitivity.computeTransitivity(mockProperties);

//        ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);

        // (1+1+1)/(1+3+1+3+1+1) = 3/10
        double correctTransitivity = 0.3;

        double allowedDeviationFraction = 0.0000001;
        assertEquals(correctTransitivity, transitivity, allowedDeviationFraction * correctTransitivity);

    }
}
