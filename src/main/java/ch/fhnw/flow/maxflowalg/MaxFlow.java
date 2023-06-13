package ch.fhnw.flow.maxflowalg;

import javafx.util.Pair;

import java.util.*;
import java.util.stream.Collectors;


public class MaxFlow {
    private int vertexCount; // Number of vertices in graph
    private int source, sink;
    private Map<FlowNode, Map<FlowNode, FlowEdge>> graph;
    // private SortedMap<String, MaxFlowAlgorithm.FlowEdge> adjacencyMatrix = new SortedMap<String, MaxFlowAlgorithm.FlowEdge>(); // key SortedMap = (String) indexNodeOrigin + (String) indexNodeDestination

    // graph that only contains the weighted, directed flow between nodes
    private int[][] flowGraph = new int[0][0]; // contains solution of inserted flow
    private int[] interimGoals = new int[0];
    private int maxFlow;

    private final int INTERIM_NEEDED_CAPACITY = 1;


    private final FlowNode[] nodes;

    public MaxFlow(Map<FlowNode, Map<FlowNode, FlowEdge>> graph, FlowNode[] nodes) {
        this.graph = graph;
        this.nodes = nodes;
    }
    
    public Map<FlowNode, Map<FlowNode, FlowEdge>> getSolvedGraph(FlowNode fnSource, FlowNode fnSink) {
        
        this.edmondsKarp(fnSource.getIndex(), fnSink.getIndex());

        int i = 0;
        Set<FlowNode> keyset = this.graph.keySet();
        for(FlowNode nodeFrom : keyset) {
            int j = 0;
            for (FlowNode nodeTo : keyset) {
                if (this.graph.get(nodeFrom).get(nodeTo) != null) {
                    this.graph.get(nodeFrom).get(nodeTo).setCurrentCapacity(this.flowGraph[i][j]);
                    if(this.flowGraph[i][j] > 0) {
                        nodeTo.setUsed(true);
                    }
                }
                j++;
            }
            i++;
        }

        for(FlowNode fn : keyset) {
            if(fn.isInterimGoal() && !fn.isUsed()) {
                fn.setUsed(this.divertFlow(fn, INTERIM_NEEDED_CAPACITY));
            }
        }

        return this.graph;
    }

    // Returns the maximum flow from source to sink in the given graph
    public int edmondsKarp(int indexSource, int indexSink) {
        int u = 0; // usually used as: u -> origin point/node index; v -> destination point/node index
        int v;
        this.source = indexSource;
        this.sink = indexSink;
        this.vertexCount = this.graph.size();
        this.flowGraph = new int[vertexCount][vertexCount];

        // set the list of all interim goals that have to be reached
        extractInterimGoals();

        int[][] adjacencyMatrix = new int[this.vertexCount][this.vertexCount];
        // create simplified graph which can be used for the breadth first search function (bfs)
        for (SortedMap.Entry<FlowNode, Map<FlowNode, FlowEdge>> entry : graph.entrySet()) {
            for (FlowNode nodeTo : this.nodes) {
                FlowNode nodeFrom = entry.getKey();
                FlowEdge edge = entry.getValue().get(nodeTo);
                if (edge == null) {
                    adjacencyMatrix[nodeFrom.getIndex()][nodeTo.getIndex()] = 0;
                    continue;
                }
                adjacencyMatrix[nodeFrom.getIndex()][nodeTo.getIndex()] = edge.getMaxCapacity();
            }
        }

        // This array is filled by BFS and to store path
        int[] parent = new int[vertexCount];
        int max_flow = 0; // There is no flow initially
        // Augment the flow while there is path from source to sink
        while (bfs(adjacencyMatrix, source, sink, parent)) {
            // Find minimum residual capacity of the edge along the path filled by BFS
            int path_flow = Integer.MAX_VALUE;
            for (v = sink; v != source; v = parent[v]) {
                u = parent[v];
                path_flow = Math.min(path_flow, adjacencyMatrix[u][v]);
            }
            // update residual capacities of the edges and
            // reverse edges along the path
            for (v = sink; v != source; v = parent[v]) {
                u = parent[v];
                adjacencyMatrix[u][v] -= path_flow;
                adjacencyMatrix[v][u] += path_flow;
                // previously empty flow graph being filled with the path_flow
                this.flowGraph[u][v] += path_flow;
            }
            // Add path flow to overall flow
            max_flow += path_flow;
        }
        // Return the overall flow
        this.maxFlow = max_flow;
        return max_flow;
    }

    // Returns true if there is a path from source to sink in residual graph. Also fills parent[] to store the path
    boolean bfs(int[][] adjacencyMatrix, int source, int sink, int[] parent) {
        this.vertexCount = parent.length;
        // Create a visited array
        boolean[] visited = new boolean[vertexCount];

        LinkedList<Integer> queue = new LinkedList<Integer>();
        // start at source
        queue.add(source);
        visited[source] = true;
        parent[source] = -1;

        while (queue.size() != 0) {
            int u = queue.poll();
            for (int v = 0; v < vertexCount; v++) {
                if (!visited[v] && adjacencyMatrix[u][v] > 0) {
                    // If we find a connection to the sink node then we have reached the end
                    if (v == sink) {
                        parent[v] = u;
                        return true;
                    }
                    queue.add(v);
                    parent[v] = u;
                    visited[v] = true;
                }
            }
        }
        // didn't reach the sink in BFS starting from the source,
        return false;
    }

    // does not check if more flow is being outputted than inputted
    public boolean[] getInterimGoalsReached(int[][] flowGraph) {
        // loop through all interim goals
        boolean[] visitedInterimGoals = new boolean[this.interimGoals.length];
        for (int i = 0; i < this.interimGoals.length; i++) {
            // looping through all the edges originating from the current node (index = i)
            for (int v = 0; v < vertexCount; v++) {
                // if there is a value > 0 within flowGraph[originNodeIndex][currentNodeIndex] there is at least 1 unit flowing to the current node
                if (flowGraph[v][this.interimGoals[i]] > 0 || this.interimGoals[i] == source) { // Always: source > 1
                    visitedInterimGoals[i] = true;
                    break;
                }
            }
        }
        return visitedInterimGoals;
    }

    public void extractInterimGoals() {
        List<FlowNode> tempNodeList = graph.keySet()
                .stream()
                .filter((FlowNode node) -> node.isInterimGoal() && node.getIndex() != this.source && node.getIndex() != this.sink).toList();
        this.interimGoals = new int[tempNodeList.size()];
        for (int i = 0; i < tempNodeList.size(); i++) {
            FlowNode node = tempNodeList.get(i);
            this.interimGoals[i++] = node.getIndex();
        }
    }

    private boolean divertFlow(FlowNode fnDivertTo, int capacityNeeded) { // Returns the success state

        ArrayList<FlowNode> validFromNodes = new ArrayList<FlowNode>();
        ArrayList<FlowNode> validToNodes = new ArrayList<FlowNode>();

        Set<FlowNode> keyset = this.graph.keySet();
        for(FlowNode node : keyset) { // Get all Nodes that have an Edge that points to the node to divert to
            if(node.isUsed()) {
                if (this.graph.get(node).get(fnDivertTo) != null) {
                    validFromNodes.add(node);
                }
                if (this.graph.get(fnDivertTo).get(node) != null) {
                    validToNodes.add(node);
                }
            }
        }

        for (FlowNode nodeFrom : validFromNodes) {
            for(FlowNode nodeTo : validToNodes) {
                if(nodeFrom.getIndex() != nodeTo.getIndex() && this.graph.get(nodeFrom).get(nodeTo) != null) {
                    int capacity = this.graph.get(nodeFrom).get(nodeTo).getCurrentCapacity();
                    if(capacity >= capacityNeeded) {
                        this.graph.get(nodeFrom).get(nodeTo).setCurrentCapacity(capacity - capacityNeeded);
                        this.graph.get(nodeFrom).get(fnDivertTo).setCurrentCapacity(capacityNeeded);
                        this.graph.get(fnDivertTo).get(nodeTo).setCurrentCapacity(capacityNeeded);
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public int getMaxFlow() {
        return maxFlow;
    }

    public int[][] getFlowGraph() {
        return flowGraph;
    }

    public Map<FlowNode, Map<FlowNode, FlowEdge>> getGraph() {
        return this.graph;
    }

    public int[] getInterimGoals() {
        return interimGoals;
    }
}
