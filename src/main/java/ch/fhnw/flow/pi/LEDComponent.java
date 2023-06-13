package ch.fhnw.flow.pi;

import java.util.*;
import java.util.stream.Collectors;

public class LEDComponent extends Component {
    public static final int DEFAULT_BLINK_INTERVAL = 800;
    private boolean isBlinking;
    private boolean isOn;
    private Map<Integer, Boolean> pinAddrMap = new HashMap<>(); // Boolean -> true: light is on; false: light is off;
    private Map<Integer, Boolean> pinPrevAddrMap = new HashMap<>();
    private Thread blinker;

    public LEDComponent(CH423 ch423, Collection<Integer> pins) {
        super(ch423);
        pins.forEach(pin -> {
            pinAddrMap.put(pin, false);
            pinPrevAddrMap.put(pin, false);
        });
        ch423.pinMode(CH423.eGPO, CH423.ePUSHPULL);
    }

    public void toggleOnOff() {
        if(isOn) {
            turnOff();
        }
        else {
            Set<Integer> pins = pinPrevAddrMap.keySet().stream().filter(i -> pinPrevAddrMap.get(i)).collect(Collectors.toSet());
            if(pins.isEmpty()) {
                turnOn(pinAddrMap.keySet());
            } else {
                turnOn(pins);
            }
        }
    }

    public void turnOn(Collection<Integer> pins) {
        super.getCh423().getTca9548A().selectChannel(super.getCh423().getChannel());
        pins.forEach(pin -> {
            if(!pinAddrMap.get(pin)) {
                super.getCh423().gpoDigitalWrite(pin, CH423.eHIGH);
                pinAddrMap.put(pin, true);
            }
        });
        isOn = true;
    }

    public void turnOff() {
        super.getCh423().getTca9548A().selectChannel(super.getCh423().getChannel());
        pinAddrMap.forEach((pin, on) -> {
            if(on) {
                super.getCh423().gpoDigitalWrite(pin, CH423.eLOW);
                pinAddrMap.put(pin, false);
                pinPrevAddrMap.put(pin, true);
            }
            else {
                pinPrevAddrMap.put(pin, false);
            }
        });
        isOn = false;
    }

    public void turnOffForced() {
        pinAddrMap.keySet().forEach(pin -> {
            super.getCh423().gpoDigitalWrite(pin, CH423.eLOW);
        });
    }

    public void blink() throws InterruptedException {
        blink(DEFAULT_BLINK_INTERVAL);
    }
    public void blink(int time) throws InterruptedException {
        toggleOnOff();
        Thread.sleep(time);
        toggleOnOff();
        Thread.sleep(time);
    }
    public static void blink(Collection<LEDComponent> leds) throws InterruptedException {
        blink(leds, DEFAULT_BLINK_INTERVAL);
    }

    public static void blink(Collection<LEDComponent> leds, int time) throws InterruptedException {
        leds.forEach(LEDComponent::toggleOnOff);
        Thread.sleep(time);
        leds.forEach(LEDComponent::toggleOnOff);
        Thread.sleep(time);
    }

    public void blinking(int time) throws InterruptedException {
        if(!isBlinking) { return; }
        toggleOnOff();
        if(!isBlinking) { return; }
        Thread.sleep(time);
    }

    public void startBlinking() {
        startBlinking(DEFAULT_BLINK_INTERVAL);
    }

    public void startBlinking(int interval) {
        if(isBlinking) return;
        isBlinking = true;
        blinker = new Thread(() -> {
            boolean initOn = isOn;
            int i = 0;
            while (isBlinking && i < 15) {
                try {
                    blinking(interval);
                } catch (InterruptedException e) {
                    // TODO: log
                    isBlinking = false;
                }
                i++;
            }
            if(initOn && !isOn) {
                turnOn(pinPrevAddrMap.keySet());
            }
        });
        blinker.start();
    }
    public void addPin(int pin) {
        pinAddrMap.put(pin, false);
        pinPrevAddrMap.put(pin, false);
    }
    public void stopBlinking() {
        isBlinking = false;
        if(blinker.isAlive()) {
            try {
                blinker.join();
            } catch(InterruptedException exception) {
                // TODO: LOG
                System.out.println(exception.getMessage());
            }
        }
    }
    public boolean isBlinking() {
        return isBlinking;
    }
    public boolean isOn() {
        return isOn;
    }

    public Collection<Integer> getPins() {
        return pinAddrMap.keySet();
    }
    public Collection<Integer> getActivePins() {
        return pinAddrMap.entrySet().stream().filter(entry -> entry.getValue()).map(Map.Entry::getKey)
                .collect(Collectors.toCollection(HashSet::new));
    }
}
