package ch.fhnw.flow.game;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;

import java.util.*;
import java.util.concurrent.Flow;

public class Level {
    private Map<FlowNode, Map<FlowNode, FlowEdge>> graph;
    private ArrayList<FlowNode> sourceNodes;
    private FlowNode sink;
    private Set<FlowNode> deactivatedNodes;

    private final int MAX_UNITS = 64;

    public Level(Map<FlowNode, Map<FlowNode, FlowEdge>> graph, FlowNode sourceNode, FlowNode sink) {
        this(graph, (ArrayList<FlowNode>) List.of(sourceNode), sink);
    }

    public Level(Map<FlowNode, Map<FlowNode, FlowEdge>> graph, ArrayList<FlowNode> sourceNodes, FlowNode sink) {
        this(graph, sourceNodes, sink, new HashSet<>());
    }

    public Level(Map<FlowNode, Map<FlowNode, FlowEdge>> graph, ArrayList<FlowNode> sourceNodes, FlowNode sink, Set<FlowNode> deactivatedNodes) {
        this.graph = graph;
        this.sourceNodes = sourceNodes;
        this.sink = sink;
        for(FlowNode fn : this.sourceNodes) {
            fn.setUsed(true);
        }
        this.deactivatedNodes = deactivatedNodes;
    }

    public static Level getNormalizedLevel(Level l, FlowNode toDelete) {

        if(l.sourceNodes.size() < 2) { // if there is only one source, the Level is already normalized
            return l;
        }

        FlowNode toMerge = null;
        for(FlowNode fn : l.sourceNodes) {
            if(toDelete.getIndex() != fn.getIndex()) {
                toMerge = fn;
            }
        }

        Map<FlowNode, Map<FlowNode, FlowEdge>> normalizedGraph = l.graph;
        for(FlowNode nodeTo : normalizedGraph.get(toDelete).keySet()) {
            FlowEdge flowEdgeToAdd = normalizedGraph.get(toDelete).get(nodeTo);
            FlowEdge flowEdgeToMerge = normalizedGraph.get(toMerge).get(nodeTo);
            if(flowEdgeToMerge == null) {
                normalizedGraph.get(toMerge).put(nodeTo, flowEdgeToAdd);
            }
            else {
                normalizedGraph.get(toMerge).put(nodeTo, new FlowEdge(
            flowEdgeToMerge.getMaxCapacity() + flowEdgeToAdd.getMaxCapacity(),
          flowEdgeToMerge.getCurrentCapacity() + flowEdgeToAdd.getCurrentCapacity()
                    )
                );
            }
        }

        ArrayList<FlowNode> normalizedSources = new ArrayList<FlowNode>();
        normalizedSources.add(toMerge);

        normalizedGraph.remove(toDelete);
        for (FlowNode fn : normalizedGraph.keySet()) {
            normalizedGraph.get(fn).remove(toDelete);
        }
        normalizedGraph.remove(toDelete);

        Set<FlowNode> normalizedGraphKeyset = normalizedGraph.keySet();
        int newIndex = 0;
        for (FlowNode fn : normalizedGraphKeyset) {
            fn.setIndex(newIndex);
            newIndex++;
        }

        return new Level(normalizedGraph, normalizedSources, l.sink, l.deactivatedNodes);
    }

    public int getAvailableUnits(FlowNode node) {
        if (this.sourceNodes.contains(node)) {
            return MAX_UNITS;
        }
        // Count incoming
        int incomingUnits = 0;
        for (Map<FlowNode, FlowEdge> row : this.graph.values()) {
            if(row.get(node) != null) { // Check for all edges that end in desired Node
                incomingUnits += row.get(node).getCurrentCapacity();
            }
        }

        int outgoingUnits = 0;
        Set<FlowNode> keyset = this.graph.get(node).keySet();
        for(FlowNode nodeTo : keyset) {
            if(this.graph.get(node).get(nodeTo) != null) {
                outgoingUnits += this.graph.get(node).get(nodeTo).getCurrentCapacity();
            }
        }
        return incomingUnits - outgoingUnits;
    }

    public Map<FlowNode, Map<FlowNode, FlowEdge>> getGraph() {
        return graph;
    }

    public ArrayList<FlowNode> getSource() {
        return sourceNodes;
    }

    public FlowNode getSink() {
        return sink;
    }


    private Map<FlowNode, Map<FlowNode, FlowEdge>> getGraphCopy(ArrayList<FlowNode> newNodes) {

        Map<FlowNode, Map<FlowNode, FlowEdge>> deepCopy = new TreeMap<>();

        Set<FlowNode> keyset = this.graph.keySet();


        for(FlowNode newNode : newNodes) {
            deepCopy.put(newNode, new TreeMap<>());
        }

        int i = 0;
        for(FlowNode nodeFrom : keyset) {
            int j = 0;
            for(FlowNode nodeTo : keyset) {
                if(this.graph.get(nodeFrom).get(nodeTo) != null) {
                    FlowEdge oldEdge = this.graph.get(nodeFrom).get(nodeTo);
                    deepCopy.get(newNodes.get(i)).put(newNodes.get(j), new FlowEdge(oldEdge.getMaxCapacity(), oldEdge.getCurrentCapacity()));
                }
                j++;
            }
            i++;
        }

        return deepCopy;
    }

    public Level getDeepCopy() {

        Set<FlowNode> keyset = this.graph.keySet();
        ArrayList<FlowNode> newNodes = new ArrayList<>();
        for (FlowNode oldNode : keyset) {
            newNodes.add(new FlowNode(oldNode.getIndex(), oldNode.isInterimGoal(), oldNode.isUsed()));
        }

        FlowNode newSink = null;
        ArrayList<FlowNode> newSources = new ArrayList<>();
        Set<FlowNode> newDeNodes = new HashSet<>();
        for(FlowNode node : newNodes) {
            for(FlowNode oldSource : this.sourceNodes) {
                if(node.getIndex() == oldSource.getIndex()) {
                    node.setUsed(true);
                    newSources.add(node);
                }
            }
            if (node.getIndex() == this.sink.getIndex()) {
                newSink = node;
            }
        }

        Map<FlowNode, Map<FlowNode, FlowEdge>> newGraph = this.getGraphCopy(newNodes);

        return new Level(newGraph, newSources, newSink, this.deactivatedNodes);
    }

    public Set<FlowNode> getDeactivatedNodes() {
        return deactivatedNodes;
    }
}
