package ch.fhnw.flow.maxflowalg;

import org.junit.Test;
import java.util.*;
import static org.junit.Assert.*;

public class MaxFlowTest {

    private SortedMap<FlowNode, Map<FlowNode, FlowEdge>> generateSampleGraph() {
        FlowNode nodeA = new FlowNode(0);
        FlowNode nodeB = new FlowNode(1, true);
        FlowNode nodeC = new FlowNode(2);
        FlowNode nodeD = new FlowNode(3);
        FlowNode[] nodes = {nodeA, nodeB, nodeC, nodeD};

        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = new TreeMap<>();
        graph.put(nodeA, new HashMap<>());
        graph.put(nodeB, new HashMap<>());
        graph.put(nodeC, new HashMap<>());
        graph.put(nodeD, new HashMap<>());

        graph.get(nodeA).put(nodeB, new FlowEdge(10));
        graph.get(nodeA).put(nodeC, new FlowEdge(20));
        graph.get(nodeB).put(nodeD, new FlowEdge(5));
        graph.get(nodeC).put(nodeD, new FlowEdge(30));

        /*
        +---+       10        +---+
        | A | --------------> | B |
        +---+                 +---+
          |                     |
          |20                   |5
          |                     |
          v                     v
        +---+                 +---+
        | C | --------------> | D |
        +---+       30        +---+

        */
        return graph;
    }

    @Test
    public void edmondsKarpTest() {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = generateSampleGraph();
        FlowNode[] nodes = graph.keySet().toArray(new FlowNode[0]);
        MaxFlow maxFlow = new MaxFlow(graph, nodes);

        int result = maxFlow.edmondsKarp(0, 3);
        assertEquals(25, result);
    }

    @Test
    public void getInterimGoalsReachedTest() {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = generateSampleGraph();
        FlowNode[] nodes = graph.keySet().toArray(new FlowNode[0]);
        MaxFlow maxFlow = new MaxFlow(graph, nodes);

        maxFlow.edmondsKarp(0, 3);

        boolean[] interimGoalsReached = maxFlow.getInterimGoalsReached(maxFlow.getFlowGraph());
        assertTrue(interimGoalsReached[0]);
    }

    @Test
    public void bfsTest() {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = generateSampleGraph();
        FlowNode[] nodes = graph.keySet().toArray(new FlowNode[0]);
        MaxFlow maxFlow = new MaxFlow(graph, nodes);
        int source = 0, sink = 3;
        int[][] adjacencyMatrix = new int[nodes.length][nodes.length];
        for (SortedMap.Entry<FlowNode, Map<FlowNode, FlowEdge>> entry : graph.entrySet()) {
            for (FlowNode nodeTo : nodes) {
                FlowNode nodeFrom = entry.getKey();
                FlowEdge edge = entry.getValue().get(nodeTo);
                if (edge == null) {
                    adjacencyMatrix[nodeFrom.getIndex()][nodeTo.getIndex()] = 0;
                    continue;
                }
                adjacencyMatrix[nodeFrom.getIndex()][nodeTo.getIndex()] = edge.getMaxCapacity();
            }
        }
        int[] parent = new int[nodes.length];
        assertTrue(maxFlow.bfs(adjacencyMatrix, source, sink, parent));
    }

    @Test
    public void extractInterimGoalsTest() {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = generateSampleGraph();
        FlowNode[] nodes = graph.keySet().toArray(new FlowNode[0]);
        MaxFlow maxFlow = new MaxFlow(graph, nodes);

        maxFlow.extractInterimGoals();
        int[] interimGoals = maxFlow.getInterimGoals();
        assertEquals(1, interimGoals.length);
        assertEquals(1, interimGoals[0]);
    }

    @Test
    public void getGraphTest() {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = generateSampleGraph();
        FlowNode[] nodes = graph.keySet().toArray(new FlowNode[0]);
        MaxFlow maxFlow = new MaxFlow(graph, nodes);

        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> returnedGraph = (SortedMap<FlowNode, Map<FlowNode, FlowEdge>>) maxFlow.getGraph();

        assertEquals(graph, returnedGraph);
    }
}

