package ch.fhnw.flow.maxflowalg;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlowEdgeTest {

    //the testConstructor method also tests the two getters
    @Test
    public void constructorTest() {
        FlowEdge edge = new FlowEdge(10, 5);
        assertEquals(10, edge.getMaxCapacity());
        assertEquals(5, edge.getCurrentCapacity());

        FlowEdge edge2 = new FlowEdge(20);
        assertEquals(20, edge2.getMaxCapacity());
        assertEquals(0, edge2.getCurrentCapacity());
    }

    @Test
    public void setCurrentCapacityTest() {
        FlowEdge edge = new FlowEdge(10, 5);
        edge.setCurrentCapacity(7);
        assertEquals(7, edge.getCurrentCapacity());
    }

    //The following tests handle "edge" cases that may fail if these
    //cases are not properly handled in the respective methods.
    @Test
    public void currentCapacityCannotExceedMaxCapacityTest() {
        FlowEdge edge = new FlowEdge(10);
        edge.setCurrentCapacity(15);
        assertNotEquals(15, edge.getCurrentCapacity());
    }

    @Test
    public void currentCapacityCannotBeNegativeTest() {
        FlowEdge edge = new FlowEdge(10);
        edge.setCurrentCapacity(-5);
        assertNotEquals(-5, edge.getCurrentCapacity());
        assertEquals(0, edge.getCurrentCapacity());
    }
}

