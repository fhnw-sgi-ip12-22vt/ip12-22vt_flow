package ch.fhnw.flow.pi;

import ch.fhnw.flow.maxflowalg.FlowEdge;
import ch.fhnw.flow.maxflowalg.FlowNode;
import com.pi4j.io.Input;
import javafx.util.Pair;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.util.*;
import java.util.stream.Collectors;

public class ComponentComposer {
    public static Map<Pair<Integer, Integer>, Pair<ButtonComponent, Collection<LEDRGBComponent>>>
    createEdgeComponents(CH423[] boards, List<PrimitiveMapping> edges) {
        Map<Pair<Integer, Integer>, Pair<ButtonComponent, Collection<LEDRGBComponent>>> edgeMapping = new HashMap<>();
        Iterator<PrimitiveMapping> iterator = edges.iterator();
        while (iterator.hasNext()) {
            PrimitiveMapping pm = iterator.next();
            // Edge creation:
            Pair<Integer, Integer> edge = new Pair<>(pm.edge[0], pm.edge[1]);
            // LED creation:
            Collection<LEDRGBComponent> ledCollection = new HashSet<>();
            for (int ledInd = 0; ledInd < pm.leds.length; ledInd++) {
                ledCollection.add(new LEDRGBComponent(boards[pm.leds[ledInd][0]], pm.leds[ledInd][1], pm.leds[ledInd][2], pm.leds[ledInd][3]));
            }
            //Button creation:
            ButtonComponent button = new ButtonComponent(boards[pm.button[0]], pm.button[1], edge);

            edgeMapping.put(edge, new Pair<>(button, ledCollection));
        }
        return edgeMapping;
    }

    public static Map<Integer, LEDRGComponent> getLEDsFromFile(String filename, CH423[] boards) {
        BufferedReader bf = new BufferedReader(new InputStreamReader(ComponentComposer.class.getResourceAsStream(filename)));
        String JSONString = bf.lines().collect(Collectors.joining());
        JSONArray boardsJSON = new JSONArray(JSONString);

        Map<Integer, LEDRGComponent> ledMap = new HashMap<>();
        for (int i = 0; i < boardsJSON.length(); i++) {
            JSONArray ledsJSON = boardsJSON.getJSONArray(i);
            int[] pins = intArrFromJSONArr(ledsJSON);
            if(pins.length > 0) {
                int board = pins[0];
                ledMap.put(i, new LEDRGComponent(boards[board], pins[1], pins[2]));
            }
        }
        return ledMap;
    }

    public static List<PrimitiveMapping> getPrimitiveMappingFromFile(String filename) {
        InputStreamReader isr = new InputStreamReader(ComponentComposer.class.getResourceAsStream(filename));
        BufferedReader bf = new BufferedReader(isr);
        String JSONString = bf.lines().collect(Collectors.joining());
        JSONArray mappingsJSON = new JSONArray(JSONString);

        ArrayList<PrimitiveMapping> mappingsList = new ArrayList<>();

        for (int i = 0; i < mappingsJSON.length(); i++) {
            JSONObject mapJSON = mappingsJSON.getJSONObject(i);

            JSONArray edgeJSON = mapJSON.getJSONArray("edge");
            JSONArray ledsJSON = mapJSON.getJSONArray("leds");
            JSONArray buttonJSON = mapJSON.getJSONArray("button");

            ArrayList<int[]> ledsList = new ArrayList<>();
            for (int j = 0; j < ledsJSON.length(); j++) {
                JSONArray ledJSON = ledsJSON.getJSONArray(j);
                int[] led = intArrFromJSONArr(ledJSON);
                ledsList.add(led);
            }

            int[] edge = intArrFromJSONArr(edgeJSON);
            int[][] leds = ledsList.toArray(new int[0][0]);
            int[] button = intArrFromJSONArr(buttonJSON);

            PrimitiveMapping mapping = new PrimitiveMapping(edge, leds, button);
            mappingsList.add(mapping);
        }
        return mappingsList;
    }

    private static int[] intArrFromJSONArr(JSONArray jArr) {
        int[] arr = new int[jArr.length()];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = jArr.getInt(i);
        }
        return arr;
    }

    static class PrimitiveMapping {

        public int[] edge; // [0] = fromNode, [1] = toNode
        public int[][] leds; // [0][0] = first pin first led, [0][1] = seconds pin first led, [1][0] = first pin seconds led, etc
        public int[] button;

        public PrimitiveMapping(int[] edge, int[][] leds, int[] button) {
            this.edge = edge;
            this.leds = leds;
            this.button = button;
        }
    }

}
