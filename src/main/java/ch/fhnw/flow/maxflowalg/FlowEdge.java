package ch.fhnw.flow.maxflowalg;

public class FlowEdge {
    private int maxCapacity;
    private int currentCapacity;

    public FlowEdge(int maxCapacity, int currentCapacity) {
        this.maxCapacity = maxCapacity;
        this.currentCapacity = currentCapacity;
    }

    public FlowEdge(int maxCapacity) {
        this.maxCapacity = maxCapacity;
        this.currentCapacity = 0;
    }

    public int getMaxCapacity() {
        return maxCapacity;
    }

    public int getCurrentCapacity() {
        return currentCapacity;
    }

    public void setCurrentCapacity(int capacity) {
        if(capacity <= maxCapacity && capacity >= 0) this.currentCapacity = capacity;
    }
}

