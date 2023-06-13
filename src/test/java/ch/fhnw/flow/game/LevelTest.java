package ch.fhnw.flow.game;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class LevelTest {
    private Level level;
    private Map<FlowNode, Map<FlowNode, FlowEdge>> graph;
    private ArrayList<FlowNode> sourceNodes;
    private FlowNode sink;

    @Before
    public void setUp() {
        /*
        Visualization of the graph:
        node0 --10--> node2 --7--> sink <-5-- node4 <-15-- node1
          |                       ^
          |                       |
          5                       10
          |                       |
          v                       |
        node3 --------------------|
        */
        graph = new TreeMap<>();
        sourceNodes = new ArrayList<>();
        FlowNode node0 = new FlowNode(0, false);
        FlowNode node1 = new FlowNode(1, false);
        sourceNodes.add(node0);
        sourceNodes.add(node1);
        sink = new FlowNode(5, false);

        // Create graph
        graph.put(node0, new TreeMap<>());
        graph.put(node1, new TreeMap<>());
        graph.put(sink, new TreeMap<>());

        FlowNode node2 = new FlowNode(2, true);
        FlowNode node3 = new FlowNode(3, true);
        FlowNode node4 = new FlowNode(4, true);

        graph.put(node2, new TreeMap<>());
        graph.put(node3, new TreeMap<>());
        graph.put(node4, new TreeMap<>());

        graph.get(node0).put(node2, new FlowEdge(10));
        graph.get(node0).put(node3, new FlowEdge(5));
        graph.get(node1).put(node4, new FlowEdge(15));
        graph.get(node2).put(sink, new FlowEdge(7));
        graph.get(node3).put(sink, new FlowEdge(10));
        graph.get(node4).put(sink, new FlowEdge(5));

        level = new Level(graph, sourceNodes, sink);
    }

    @Test
    public void constructorTest() {
        assertNotNull(level);
        assertEquals(graph, level.getGraph());
        assertEquals(sourceNodes, level.getSource());
        assertEquals(sink, level.getSink());
    }

    @Test
    public void getAvailableUnitsTest() {
        FlowNode node2 = new FlowNode(2, true);
        FlowNode node3 = new FlowNode(3, true);

        graph.get(new FlowNode(0, false)).get(node2).setCurrentCapacity(5);
        graph.get(new FlowNode(0, false)).get(node3).setCurrentCapacity(3);
        graph.get(node2).get(sink).setCurrentCapacity(2);
        graph.get(node3).get(sink).setCurrentCapacity(1);

        assertEquals(3, level.getAvailableUnits(node2));
        assertEquals(2, level.getAvailableUnits(node3));
    }

    @Test
    public void getGraphTest() {
        assertEquals(graph, level.getGraph());
    }

    @Test
    public void getSourceTest() {
        assertEquals(sourceNodes, level.getSource());
    }

    @Test
    public void getSinkTest() {
        assertEquals(sink, level.getSink());
    }
}

