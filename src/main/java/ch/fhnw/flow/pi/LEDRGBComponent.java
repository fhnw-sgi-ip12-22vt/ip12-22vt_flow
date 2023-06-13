package ch.fhnw.flow.pi;

import java.util.Collections;
import java.util.List;

public class LEDRGBComponent extends LEDRGComponent {
    private int bluePin;
    public LEDRGBComponent(CH423 ch423, Integer redPin, Integer greenPin, int bluePin) {
        super(ch423, redPin, greenPin);
        super.addPin(bluePin);
        this.bluePin = bluePin;
    }

    public void turnBlue() {
        if(super.isOn()) super.turnOff();
        super.turnOn(Collections.singleton(bluePin));
    }

    public void turnLightBlue() {
        if(super.isOn()) super.turnOff();
        super.turnOn(List.of(getGreenPin(), bluePin));
    }

    public void turnViolet() {
        if(super.isOn()) super.turnOff();
        super.turnOn(List.of(getRedPin(), bluePin));
    }

    public void turnWhite() {
        if(super.isOn()) super.turnOff();
        super.turnOn(List.of(getRedPin(), getGreenPin(), bluePin));
    }

    public void turnOrange() {
        if(super.isOn()) super.turnOff();
        super.turnOrange();
    }
}
