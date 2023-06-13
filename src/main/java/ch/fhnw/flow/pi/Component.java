package ch.fhnw.flow.pi;

public abstract class Component {
    private CH423 ch423;
    public Component(CH423 ch423) {
        this.ch423 = ch423;
        ch423.getTca9548A().selectChannel(ch423.getChannel());
    }
    public CH423 getCh423() {
        return ch423;
    }
}
