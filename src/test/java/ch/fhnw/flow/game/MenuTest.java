package ch.fhnw.flow.game;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import ch.fhnw.flow.pi.HardwareCommand;
import javafx.util.Pair;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class MenuTest {

    private Menu menu;
    private GameLogic gameLogicMock;
    private HardwareCommand hwCommandMock;

    //Most test currently fail, due to improper mocking

    @Before
    public void setup() throws IllegalAccessException, NoSuchFieldException{
        gameLogicMock = Mockito.mock(GameLogic.class);
        hwCommandMock = Mockito.mock(HardwareCommand.class);
        menu = Mockito.mock(Menu.class);
        //menu = new Menu(gameLogicMock);
        // Use reflection to set the private field hwC in Menu class
        Field hwCField = Menu.class.getDeclaredField("hwC");
        hwCField.setAccessible(true);
        hwCField.set(menu, hwCommandMock);
    }
    @Test
    public void endLevel_invalidEndTest() {
        FlowNode sinkNode = Mockito.mock(FlowNode.class);
        Level levelMock = Mockito.mock(Level.class);

        when(gameLogicMock.getCurLevel()).thenReturn(levelMock);
        when(levelMock.getSink()).thenReturn(sinkNode);
        when(sinkNode.isUsed()).thenReturn(false);

        //boolean result = menu.endLevel();
        boolean result = menu.tryEndLevel();

        assertFalse(result);
        Mockito.verify(hwCommandMock, Mockito.never()).onLevelEnd();
    }
}
