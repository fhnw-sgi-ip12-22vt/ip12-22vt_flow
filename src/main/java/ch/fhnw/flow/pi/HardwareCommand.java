package ch.fhnw.flow.pi;

import ch.fhnw.flow.game.Level;
import ch.fhnw.flow.game.Menu;
import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import com.pi4j.Pi4J;
import com.pi4j.context.Context;
import javafx.util.Pair;

import java.util.*;
import java.util.List;

public class HardwareCommand {
    private final TCA9548A tca9548A;
    public static Context pi4j;
    private Map<Pair<Integer, Integer>, Pair<ButtonComponent, Collection<LEDRGBComponent>>> edgeMap;
    private Map<Integer, LEDRGComponent> nodeMap;
    private Set<FlowNode> irrelevantNodes;

    public HardwareCommand() {
        if (pi4j == null) initContext();
        this.tca9548A = new TCA9548A(pi4j, TCA9548A.Address.e0x70);
        CH423[] gravityBoards = new CH423[8];
        gravityBoards[1] = new CH423(1, tca9548A, true);
        gravityBoards[2] = new CH423(2, tca9548A, true);
        gravityBoards[3] = new CH423(3, tca9548A, false);
        gravityBoards[4] = new CH423(4, tca9548A, true);
        gravityBoards[5] = new CH423(5, tca9548A, false);
        // /src/main/resources
        List<ComponentComposer.PrimitiveMapping> primitiveMappings = ComponentComposer
                .getPrimitiveMappingFromFile("/ch/fhnw/flow/pi/edges.json");
        this.edgeMap = ComponentComposer.createEdgeComponents(gravityBoards, primitiveMappings);
        // /src/main/resources/ch/fhnw/flow/pi
        this.nodeMap = ComponentComposer.getLEDsFromFile("/ch/fhnw/flow/pi/nodes.json", gravityBoards);

        this.edgeMap.entrySet().forEach(entry -> {
            Pair<Integer, Integer> edgeIndex = entry.getKey();
            ButtonComponent button = entry.getValue().getKey();
            button.addEventListener((i) -> {
                if (irrelevantNodes.stream().noneMatch(node -> node.getIndex() == edgeIndex.getKey())) {
                    Menu.lastPressedInt = edgeIndex;
                    deactivateListeners();
                }
            });
        });
    }

    public static void initContext() {
        var context = Pi4J.newAutoContext();
        pi4j = context;
    }

    public void onLevelStart(Level level) {
        // signal Interim Goals:
        level.getGraph().keySet().stream().forEach(flowNode -> {
            LEDRGComponent led = nodeMap.get(flowNode.getIndex());
            if (led != null) {
                if (flowNode.isInterimGoal()) {
                    led.turnOrange();
                } else {
                    led.turnOff();
                }
            }
        });
        // Set default for all edges
        level.getGraph().entrySet().stream().forEach(nodeEntry -> {
            int u = nodeEntry.getKey().getIndex();
            nodeEntry.getValue().forEach((key, value) -> {
                int v = key.getIndex();
                Pair<ButtonComponent, Collection<LEDRGBComponent>> edgeComponentPair = edgeMap.get(new Pair<>(u, v));
                if (edgeComponentPair != null) {
                    edgeComponentPair.getValue().forEach(rgbLED -> {
                        getLEDRGBColorRunnable(0, rgbLED).run();
                    });
                }
            });
        });
        irrelevantNodes = level.getDeactivatedNodes();
        // deactivate irrelevant edges
        edgeMap.entrySet().stream()
                .filter(edge -> irrelevantNodes.stream()
                        .anyMatch(node -> node.getIndex() == edge.getKey().getKey()))
                .forEach(edge -> {
                    Collection<LEDRGBComponent> leds = edge.getValue().getValue();
                    leds.forEach(LEDRGBComponent::turnOff);
                });
        activateListeners();
    }

    public void edgeBlink(Pair<Integer, Integer> edge) {
        // Let this edge flash/blink
        Pair<ButtonComponent, Collection<LEDRGBComponent>> edgeComponents = edgeMap.get(edge);
        edgeComponents.getValue().stream().forEach(LEDRGBComponent::startBlinking);
    }

    public void edgeBlinkStop(Pair<Integer, Integer> edge) {
        // Stop blinking of edge
        Pair<ButtonComponent, Collection<LEDRGBComponent>> edgeComponents = edgeMap.get(edge);
        edgeComponents.getValue().stream().forEach(LEDRGBComponent::stopBlinking);
    }

    public void updateEdge(Pair<Integer, Integer> edge, FlowEdge flowEdge) {
        edgeMap.get(edge).getValue().forEach(led -> getLEDRGBColorRunnable(flowEdge.getCurrentCapacity(), led).run());
    }

    public void showError(Pair<Integer, Integer> edge) {
        Thread errorThread = new Thread(() -> {
            Pair<ButtonComponent, Collection<LEDRGBComponent>> edgeComponents = edgeMap.get(edge);
            if (edgeComponents == null) return;
            edgeComponents.getValue().forEach(led -> {
                Collection<Integer> prevPins = new HashSet<>();
                prevPins.addAll(led.getActivePins());
                LEDRGComponent ledRG = nodeMap.get(edge.getKey());
                if (ledRG != null) {
                    led.turnRed();
                    ledRG.turnRed();
                    try {
                        Thread.sleep(LEDComponent.DEFAULT_BLINK_INTERVAL);
                        LEDComponent.blink(List.of(led, ledRG));
                    } catch (InterruptedException e) {
                        // TODO: log
                    }
                    led.turnOn(prevPins);
                    ledRG.turnOff();
                }
            });
        });
        errorThread.start();
        new Thread(() -> {
            try {
                errorThread.join();
            } catch (InterruptedException e) {
                System.out.println(e.getMessage());
            }
            activateListeners();
        });
    }

    public void activateListeners() {  // E funktion wo dr Buttons erlaubt, actually Sache uszlöse
        EventHandler.start(tca9548A);
    }

    public void deactivateListeners() {  // E funktion wo dr Buttons verbietet Sache uszlöse
        EventHandler.stop();
    }

    public boolean isListening() {
        return EventHandler.isHandlingEvents && !EventHandler.isStopHandling;
    }

    public void onLevelEnd() {
        deactivateListeners();
        edgeMap.values().forEach(edgeComponents -> {
            edgeComponents.getValue().forEach(rgbLED -> getLEDRGBColorRunnable(0, rgbLED).run());
        });
        nodeMap.values().forEach(LEDComponent::turnOff);
    }

    public Runnable getLEDRGBColorRunnable(int flow, LEDRGBComponent led) {
        switch (flow) {
            case 0:
                return led::turnWhite;
            case 1:
                return led::turnViolet;
            case 2:
                return led::turnGreen;
            case 3:
                return led::turnBlue;
            default:
                return led::turnOff;
        }
    }

    public void setNodeReached(FlowNode node) {
        nodeMap.get(node.getIndex()).turnGreen();
    }

    public void setNodeNotReached(FlowNode node) {
        nodeMap.get(node.getIndex()).turnOrange();
    }
}
