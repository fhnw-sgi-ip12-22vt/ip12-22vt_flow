package ch.fhnw.flow.game;

import ch.fhnw.flow.data.FlowData;
import ch.fhnw.flow.maxflowalg.MaxFlow;
import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import ch.fhnw.flow.pi.HardwareCommand;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

import org.json.*;

public class GameLogic {

    private Level[] allLevels;
    private Level curLevel;
    private final String LEVEL_FILE_NAME = "levels.json";
    private final int SCORE_INTERIM = 5;
    private final int SCORE_MAX = 10_000;


    public void Print() {

        System.out.println("EMPTY GRAPH \n ---------------------------------");
        this.printGraph(this.curLevel.getGraph());

        FlowNode fnFirstSource = this.curLevel.getSource().get(0);
        FlowNode fnSecondSource = this.curLevel.getSource().get(0);
        Level normalized = Level.getNormalizedLevel(this.curLevel, fnSecondSource);

        System.out.println("NORMALIZED GRAPH \n ---------------------------------");
        this.printGraph(normalized.getGraph());

        MaxFlow mf = new MaxFlow(normalized.getGraph(), normalized.getGraph().keySet().toArray(new FlowNode[0]));
        Map<FlowNode, Map<FlowNode, FlowEdge>> solvedGraph = mf.getSolvedGraph(fnFirstSource, normalized.getSink());

        System.out.println("SOLVED GRAPH \n ---------------------------------");
        this.printGraph(solvedGraph);

    }

    private void printGraph(Map<FlowNode, Map<FlowNode, FlowEdge>> graph) {

        Set<FlowNode> keysetFrom = graph.keySet();
        for(FlowNode nodeFrom : keysetFrom) {
            Map<FlowNode, FlowEdge> row = graph.get(nodeFrom);
            Set<FlowNode> keysetTo = row.keySet();
            for(FlowNode nodeTo : keysetTo) {
                if(graph.get(nodeFrom).get(nodeTo) != null) {
                    int keyNodeFrom = nodeFrom.getIndex();
                    int keyNodeTo = nodeTo.getIndex();
                    int edgeWeight = graph.get(nodeFrom).get(nodeTo).getMaxCapacity();
                    int edgeFlow = graph.get(nodeFrom).get(nodeTo).getCurrentCapacity();
                    System.out.println("(" + keyNodeFrom + ")--" + edgeFlow + "/" + edgeWeight + "->(" + keyNodeTo + ")");
                }
            }
        }

        for(FlowNode nodeFrom : keysetFrom) {
            System.out.println("Node " + nodeFrom.getIndex() + " is currently " + (nodeFrom.isUsed() ? "" : "not ") + "used");
        }
    }
    public GameLogic () {
        this.allLevels = new Level[3];
        SetLevelsFromFile(LEVEL_FILE_NAME);
    }

    public TreeMap<String, Integer> calcScore() {

        FlowNode fnFirstSource = this.curLevel.getSource().get(0);
        FlowNode fnSecondSource = this.curLevel.getSource().get(0);

        Level normalized = Level.getNormalizedLevel(this.curLevel, fnSecondSource);
        Level copiedGraph = normalized.getDeepCopy();

        MaxFlow mf = new MaxFlow(copiedGraph.getGraph(), copiedGraph.getGraph().keySet().toArray(new FlowNode[0]));
        Map<FlowNode, Map<FlowNode, FlowEdge>> solvedGraph = mf.getSolvedGraph(fnFirstSource, normalized.getSink());

        int reachableScoreTotal = 0;
        int playerScoreTotal = 0;

        Set<FlowNode> keyset = solvedGraph.keySet();
        for (FlowNode nodeFrom : keyset) {
            for(FlowNode nodeTo : keyset){ // Get points for correctly solved edges
                if(normalized.getGraph().get(nodeFrom).get(nodeTo) != null && solvedGraph.get(nodeFrom).get(nodeTo) != null) {
                    int capacityEdgeSolved = solvedGraph.get(nodeFrom).get(nodeTo).getCurrentCapacity();
                    int capacityEdgePlayer = normalized.getGraph().get(nodeFrom).get(nodeTo).getCurrentCapacity();
                    reachableScoreTotal = reachableScoreTotal + capacityEdgeSolved;
                    playerScoreTotal =  playerScoreTotal + (capacityEdgeSolved - Math.abs(capacityEdgeSolved - capacityEdgePlayer));
                }
            }
            if(nodeFrom.isInterimGoal()) {
                reachableScoreTotal = reachableScoreTotal + SCORE_INTERIM;
            }
        }

        keyset = normalized.getGraph().keySet();
        for (FlowNode nodeFrom : keyset) {
            if(nodeFrom.isInterimGoal() && nodeFrom.isUsed()) {
                playerScoreTotal = playerScoreTotal + SCORE_INTERIM;
            }
        }

        System.out.println(playerScoreTotal + " out of " + reachableScoreTotal + " reached, shown score is: " + (SCORE_MAX / reachableScoreTotal) * playerScoreTotal);
        TreeMap<String, Integer> tm = new TreeMap<>();
        tm.put("score", (SCORE_MAX / reachableScoreTotal) * playerScoreTotal);
        tm.put("achieved_maxflow", copiedGraph.getAvailableUnits(copiedGraph.getSink()));
        tm.put("maxflow", mf.getMaxFlow());
        return tm;
    }

    public void unsetCurLevel() {
        this.curLevel = null;
    }
    public void setCurLevel(int key) {
        this.unsetCurLevel();
        this.curLevel = this.allLevels[key].getDeepCopy();
    }

    public Level getCurLevel() {
        return this.curLevel;
    }

    public FlowEdge getEdge(FlowNode nodeFrom, FlowNode nodeTo) {
        return this.curLevel.getGraph().get(nodeFrom).get(nodeTo);
    }

    public Level[] getAllLevels() {
        return allLevels;
    }

    private void SetLevelsFromFile(String filename) {

        BufferedReader bf = new BufferedReader(new InputStreamReader(getClass().getResourceAsStream(filename)));
        String JSONString = bf.lines().collect(Collectors.joining());
        JSONArray arrJSON = new JSONArray(JSONString);

        // For each Level
        for (int i = 0; i < arrJSON.length(); i++) {

            JSONObject levelJSON = arrJSON.getJSONObject(i); // Get Level from Level List

            int[] sources = IntArrFromJSONArr(levelJSON.getJSONArray("sources"));
            int[] deactivated = IntArrFromJSONArr(levelJSON.getJSONArray("irrelevant"));
            int sink = levelJSON.getInt("sink");
            JSONArray matrixJSON = levelJSON.getJSONArray("matrix");

            int[] interims = IntArrFromJSONArr(levelJSON.getJSONArray("interimNodes")); // Get index of interims
            ArrayList<FlowNode> fnList = CreateNodes(matrixJSON.length(), interims); // Create List of nodes with interims

            int[][] matrix = new int[matrixJSON.length()][matrixJSON.length()];
            for(int j = 0; j < matrixJSON.length(); j++) {
                JSONArray rowJSON = matrixJSON.getJSONArray(j);
                for (int k = 0; k < rowJSON.length(); k++) {
                    matrix[j][k] = rowJSON.getInt(k);
                }
            }
            ArrayList<FlowNode> sourceNodes = new ArrayList<FlowNode>();
            for(int j = 0; j < fnList.size(); j++) {
                if(ArrayContains(sources, j)) {
                    sourceNodes.add(fnList.get(j));
                }
            }

            Set<FlowNode> deactivatedNodes = new HashSet<>();
            for(int j = 0; j < deactivated.length; j++) {
                deactivatedNodes.add(fnList.get(deactivated[j]));
            }

            this.allLevels[i] = new Level(
                    GraphFromJSON(matrix, fnList),
                    sourceNodes,
                    fnList.get(sink),
                    deactivatedNodes
            );
        }
    }

    private SortedMap<FlowNode, Map<FlowNode, FlowEdge>> GraphFromJSON(int[][] matrix, ArrayList<FlowNode> nodeList) {
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = GetIndexedMap(nodeList);
        for (int i = 0; i < nodeList.size(); i++) {
            for (int j = 0; j < nodeList.size(); j++) {
                if(matrix[i][j] != 0) {
                    graph.get(nodeList.get(i)).put(nodeList.get(j), new FlowEdge(matrix[i][j]));
                }
            }
        }
        return graph;
    }

    private SortedMap<FlowNode, Map<FlowNode, FlowEdge>> GetIndexedMap(ArrayList<FlowNode> fnList) { // List must be indexed to be accessible
        SortedMap<FlowNode, Map<FlowNode, FlowEdge>> graph = new TreeMap<FlowNode, Map<FlowNode, FlowEdge>>();
        for (FlowNode fn : fnList) {
            graph.put(fn, new TreeMap<FlowNode, FlowEdge>());
        }
        return graph;
    }

    private ArrayList<FlowNode> CreateNodes(int nodeCount, int[] interims) {
        ArrayList<FlowNode> fnList = new ArrayList<FlowNode>();
        for(int i = 0; i < nodeCount; i++) {
            fnList.add(new FlowNode(i, ArrayContains(interims, i)));
        }
        return fnList;
    }

    private int[] IntArrFromJSONArr(JSONArray jArr) {
        int[] arr = new int[jArr.length()];
        for(int i = 0; i < arr.length; i++) {
            arr[i] = jArr.getInt(i);
        }
        return arr;
    }

    private boolean ArrayContains(int[] arr, int v) {
        for(int i : arr) {
            if(i == v) {
                return true;
            }
        }
        return false;
    }
}
