
package ch.fhnw.flow.game;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class GameLogicTest {
    private GameLogic gameLogic;

    @Before
    public void setUp() {
        gameLogic = new GameLogic();
        gameLogic.setCurLevel(0);
    }

    @Test
    public void setLevelsFromFileTest(){
        assertEquals(3, gameLogic.getAllLevels().length);
        assertEquals(1, gameLogic.getCurLevel().getSource().get(0).getIndex());
        assertEquals(11, gameLogic.getCurLevel().getSink().getIndex());
    }

    @Test
    public void getEdgeTest() {
        FlowNode nodeFrom = gameLogic.getCurLevel().getSource().get(0);
        FlowNode nodeTo = gameLogic.getCurLevel().getSink();
        FlowEdge edge = gameLogic.getEdge(nodeFrom, nodeTo);
        assertNull(edge); // There should be no direct edge between source and sink for level 0
    }

    @Test
    public void setCurLevelTest() {
        gameLogic.setCurLevel(1);
        Level level = gameLogic.getCurLevel();
        assertEquals(2, level.getSource().size());
        assertEquals(0, level.getSource().get(0).getIndex());
        assertEquals(1, level.getSource().get(1).getIndex());
        assertEquals(11, level.getSink().getIndex());
    }

    @Test
    public void getCurLevelTest() {
        Level level = gameLogic.getCurLevel();
        assertNotNull(level);
        assertEquals(1, level.getSource().size());
        assertEquals(11, level.getSink().getIndex());
    }
}