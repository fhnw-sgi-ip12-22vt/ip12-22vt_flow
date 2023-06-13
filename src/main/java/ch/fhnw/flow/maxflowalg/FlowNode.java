package ch.fhnw.flow.maxflowalg;

public class FlowNode implements Comparable<Object> {
    private int index;
    private boolean isInterimGoal;
    private boolean isUsed;

    public FlowNode(int index, boolean isInterimGoal) {
        this.index = index;
        this.isInterimGoal = isInterimGoal;
        this.isUsed = false;
    }

    public FlowNode(int index, boolean isInterimGoal, boolean isUsed) {
        this.index = index;
        this.isInterimGoal = isInterimGoal;
        this.isUsed = isUsed;
    }

    public FlowNode(int index) {
        this.index = index;
        this.isInterimGoal = false;
        this.isUsed = false;
    }

    public int getIndex() {
        return index;
    }

    public boolean isInterimGoal() {
        return isInterimGoal;
    }

    public void setUsed(boolean isUsed) {
        this.isUsed = isUsed;
    }

    public boolean isUsed() {
        return isUsed;
    }

    @Override
    public int compareTo(Object o) {
        FlowNode fn = (FlowNode) o;
        return Integer.compare(this.getIndex(), fn.getIndex());
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
