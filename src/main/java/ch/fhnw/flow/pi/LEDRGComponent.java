package ch.fhnw.flow.pi;

import java.util.Collections;
import java.util.List;

public class LEDRGComponent extends LEDComponent{
    private int redPin;
    private int greenPin;
    public LEDRGComponent(CH423 ch423, Integer redPin, Integer greenPin) {
        super(ch423, List.of(redPin, greenPin));
        this.redPin = redPin;
        this.greenPin = greenPin;
    }

    public void turnGreen() {
        if(super.isOn()) super.turnOff();
        super.turnOn(Collections.singleton(greenPin));
    }

    public void turnRed() {
        if(super.isOn()) super.turnOff();
        super.turnOn(Collections.singleton(redPin));
    }

    public void turnOrange() {
        super.turnOn(List.of(greenPin, redPin));
    }

    public int getRedPin() {
        return redPin;
    }

    public int getGreenPin() {
        return greenPin;
    }
}
