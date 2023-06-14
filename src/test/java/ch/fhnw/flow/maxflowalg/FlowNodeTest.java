package ch.fhnw.flow.maxflowalg;

import org.junit.Test;
import static org.junit.Assert.*;

public class FlowNodeTest {

    @Test
    public void constructorTest() {
        FlowNode node = new FlowNode(1, true);
        assertEquals(1, node.getIndex());
        assertTrue(node.isInterimGoal());
        assertFalse(node.isUsed());

        FlowNode node2 = new FlowNode(2);
        assertEquals(2, node2.getIndex());
        assertFalse(node2.isInterimGoal());
        assertFalse(node2.isUsed());
    }

    @Test
    public void setUsedTest() {
        FlowNode node = new FlowNode(1);
        node.setUsed(true);
        assertTrue(node.isUsed());
    }

    @Test
    public void compareToTest() {
        FlowNode node1 = new FlowNode(1);
        FlowNode node2 = new FlowNode(2);
        FlowNode node3 = new FlowNode(1);

        assertEquals(-1, node1.compareTo(node2));
        assertEquals(1, node2.compareTo(node1));
        assertEquals(0, node1.compareTo(node3));
    }

    @Test
    public void compareToWithSameObjectTest() {
        FlowNode node = new FlowNode(1);
        assertEquals(0, node.compareTo(node));
    }

    @Test(expected = ClassCastException.class)
    public void compareToWithNonFlowNodeObjectTest() {
        FlowNode node = new FlowNode(1);
        node.compareTo("Not a FlowNode");
    }

    @Test
    public void setUsedMultipleTimesTest() {
        FlowNode node = new FlowNode(1);
        node.setUsed(true);
        assertTrue(node.isUsed());
        node.setUsed(false);
        assertFalse(node.isUsed());
    }

    @Test
    public void interimGoalInConstructorTest() {
        FlowNode node = new FlowNode(1, false);
        assertFalse(node.isInterimGoal());
    }
}
