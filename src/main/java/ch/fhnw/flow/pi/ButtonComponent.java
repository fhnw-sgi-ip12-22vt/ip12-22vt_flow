package ch.fhnw.flow.pi;

import javafx.util.Pair;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

public class ButtonComponent extends Component {
    private int pinAddr;
    private Pair<Integer, Integer> edge;

    public ButtonComponent(CH423 ch423, int pinAddr, Pair<Integer, Integer> edge) {
        super(ch423);
        getCh423().pinMode(CH423.eGPIO, CH423.eINPUT);
        this.pinAddr = pinAddr;
        this.edge = edge;
    }

    public ButtonComponent(CH423 ch423, int pinAddr, Consumer<Integer> consumer) {
        super(ch423);
        getCh423().pinMode(CH423.eGPIO, CH423.eINPUT);
        getCh423().enable_interrupt();
        this.pinAddr = pinAddr;
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            // TODO: log
        }
        addEventListener(consumer);
    }

    public void addEventListener(Consumer<Integer> consumer) {
        getCh423().addEventListener(pinAddr, CH423.eFALLING, consumer);
    }

    public Pair<Integer, Integer> getEdge() {
        return edge;
    }

    public int getPinAddr() {
        return pinAddr;
    }
}
