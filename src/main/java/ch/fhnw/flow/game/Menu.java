package ch.fhnw.flow.game;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import ch.fhnw.flow.pi.HardwareCommand;
import javafx.event.ActionEvent;
import javafx.util.Pair;

import java.util.*;

public class Menu {

    public static ActionEvent specialEvent;
    private HardwareCommand hwC;
    private GameLogic gl;

    public FlowEdge getLastPressed() {
        return lastPressed;
    }

    private static FlowEdge lastPressed;
    private static FlowNode lastPressedFromNode;
    private static FlowNode lastPressedToNode;

    public static Pair<Integer, Integer> lastPressedInt;

    public static boolean isChoosingIntensity = false;

    public ArrayList<PlayerScore> playerScores;

    public ArrayList<PlayerScore> getPlayerScores() {
        return this.playerScores;
    }

    class PlayerScore {

        public String username;
        public int score;
        public int maxflow;
        public int achieved_maxflow;
        public PlayerScore(int score, int achieved_maxflow, int maxflow) {

            Random rn = new Random();
            int rand = rn.nextInt() % 10_000 + 10_000;
            this.username = "Spieler_" + rand;
            this.score = score;
        }

        @Override
        public String toString() {
            return this.username + "; Punktzahl: " + this.score + "; MaxFlow: " + this.achieved_maxflow + "/" + this.maxflow;
        }
    }

    public Menu(GameLogic gl) {
        this.hwC = new HardwareCommand();
        this.gl = gl;
        this.playerScores = new ArrayList<>();
    }

    public void setEasyLevel() {
        this.gl.setCurLevel(0);
        this.startLevel();
    }

    public void setMidLevel() {
        this.gl.setCurLevel(1);
        this.startLevel();
    }

    public void setHardLevel() {
        this.gl.setCurLevel(2);
        this.startLevel();
    }

    private void setLastPressed(FlowNode nodeFrom, FlowNode nodeTo) {
        lastPressed = this.gl.getEdge(nodeFrom, nodeTo);
        lastPressedFromNode = nodeFrom;
        lastPressedToNode = nodeTo;
        lastPressedInt = new Pair<>(nodeFrom.getIndex(), nodeTo.getIndex());
    }

    private void startLevel() {
        hwC.onLevelStart(this.gl.getCurLevel());
        // this.gl.Print();
    }

    public String edgeSelectByIndex(int nodeFromIndex, int nodeToIndex) {
        FlowNode nodeFrom = null;
        FlowNode nodeTo = null;
        Set<FlowNode> keyset = this.gl.getCurLevel().getGraph().keySet();
        for (FlowNode fn : keyset) {
            if (fn.getIndex() == nodeFromIndex) {
                nodeFrom = fn;
            }
            if (fn.getIndex() == nodeToIndex) {
                nodeTo = fn;
            }
        }

        return this.onEdgeSelect(nodeFrom, nodeTo);
    }

    public String onEdgeSelect(FlowNode nodeFrom, FlowNode nodeTo) {
        if(!nodeFrom.isUsed()) {
            hwC.showError(new Pair<>(nodeFrom.getIndex(), nodeTo.getIndex()));
            setLastPressed(nodeFrom, nodeTo);
            lastPressedInt = null;
            return "Unerreichbare Kante!";
        }
        else {
            this.setLastPressed(nodeFrom, nodeTo);
            hwC.edgeBlink(lastPressedInt);
            return "";
        }
    }

    public Pair<FlowNode, FlowNode> getEdgeNodes(Pair<Integer, Integer> edgeIndex) {
        List<FlowNode> nodes = gl.getCurLevel().getGraph().keySet().stream().toList();
        return new Pair<>(nodes.get(edgeIndex.getKey()), nodes.get(edgeIndex.getValue()));
    }

    public String onIntensityCancel() {
        return onIntensitySelect(lastPressed.getCurrentCapacity());
    }
    public String onIntensity0() {
        return this.onIntensitySelect(0);
    }

    public String onIntensity1() {
        return this.onIntensitySelect(1);
    }

    public String onIntensity2() {
        return this.onIntensitySelect(2);
    }

    public String onIntensity3() {
        return this.onIntensitySelect(3);

    }

    private String onIntensitySelect(int intensity) {
        if(lastPressed == null) {
            Pair<FlowNode, FlowNode> nodes = getEdgeNodes(lastPressedInt);
            setLastPressed(nodes.getKey(), nodes.getValue());
        }
        int delta = intensity - lastPressed.getCurrentCapacity();
        if(lastPressed.getMaxCapacity() < intensity) {
            this.confirmEdge();
            return "Die maximale Kapazität wurde überschritten!";
        } else if (this.gl.getCurLevel().getAvailableUnits(lastPressedFromNode) < delta) {
            this.confirmEdge();
            return "Diese Kante hat nicht genug Energie Zufuhr!";
        }
        else if (this.gl.getCurLevel().getAvailableUnits(lastPressedToNode) + delta < 0) {
            this.confirmEdge();
            return "Diese Kante hat Abhängigkeiten!";
        }
        lastPressedFromNode.setUsed(true);
        lastPressedToNode.setUsed(intensity != 0);
        lastPressed.setCurrentCapacity(intensity);
        this.confirmEdge();
        return "";
    }

    private void confirmEdge() {
        hwC.edgeBlinkStop(lastPressedInt);
        hwC.updateEdge(lastPressedInt, lastPressed);
        if(lastPressedToNode.isInterimGoal() && lastPressedToNode.isUsed()) {
            hwC.setNodeReached(lastPressedToNode);
        } else if(lastPressedToNode.isInterimGoal() && !lastPressedToNode.isUsed()) {
            hwC.setNodeNotReached(lastPressedToNode);
        }
        lastPressed = null;
        lastPressedFromNode = null;
        lastPressedToNode = null;
        lastPressedInt = null;
        // this.hwC.update(this.gl.getCurLevel());
        Menu.isChoosingIntensity = false;
        hwC.activateListeners();
    }

    public boolean isEndable() {
        return this.gl.getCurLevel() != null && this.gl.getCurLevel().getSink().isUsed();
    }

    public boolean isBeingPlayed() {
        return this.gl.getCurLevel() != null;
    }

    public boolean tryEndLevel() { // return boolean to check if end was valid
        if(this.isEndable()) { // Only able to end if the sink has electricity
            TreeMap<String, Integer> tm = this.gl.calcScore();
            this.playerScores.add(new PlayerScore(tm.get("score"), tm.get("achieved_maxflow"), tm.get("maxflow")));
            this.endGame();
            return true;
        }
        return false;
    }

    public void onHome() {
        this.hwC.onLevelEnd();
    }

    public void endGame() {
        this.hwC.onLevelEnd();
        // this.gl.unsetCurLevel();
    }

    public HardwareCommand getHwC() {
        return hwC;
    }
}
