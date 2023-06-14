// TCA9548A.java
package ch.fhnw.flow.pi;

import com.pi4j.context.Context;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;
import com.pi4j.io.i2c.I2CProvider;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

public class TCA9548A {
    public enum Address {
        e0x70(0x70),
        e0x71(0x71),
        e0x72(0x72),
        e0x73(0x73),
        e0x74(0x74),
        e0x75(0x75),
        e0x76(0x76),
        e0x77(0x77);
        private int deviceAddr;

        Address(int deviceAddr) {
            this.deviceAddr = deviceAddr;
        }

        public int getDeviceAddr() {
            return deviceAddr;
        }
    }

    private final I2C tca9548a;
    private Map<Integer, CH423> gravityBoards = new HashMap<>();
    private Collection<Integer> listenerChannels = new HashSet<>();
    private int channel;

    public TCA9548A(Context pi4j, Address tcaAddress){
        I2CProvider i2CProvider = pi4j.provider("linuxfs-i2c");
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("TCA9548A")
                .bus(1).
                device(tcaAddress.getDeviceAddr()).
                build();
        this.tca9548a = i2CProvider.create(i2cConfig);
    }

    public void selectChannel(int newChannel) {
        if (newChannel >= 0 && newChannel <= 7) {
            tca9548a.writeRegister(tca9548a.device(), (byte) (1 << newChannel));
        } else {
            throw new IllegalArgumentException("Invalid TCA9548A channel: " + newChannel);
        }
        this.channel = newChannel;
    }


    public void addGravityBoard(CH423 ch423) {
        gravityBoards.put(ch423.getChannel(), ch423);
    }

    public void addGravityBoardToListener(int boardChannel) throws IllegalArgumentException {
        if(gravityBoards.containsKey(boardChannel)) {
            listenerChannels.add(boardChannel);
        }
        else {
            throw new IllegalArgumentException("Gravity Board on channel '" + boardChannel + "' does not exist");
        }

    }

    public CH423 getCurrentGravityBoard() {
        if(gravityBoards.size() > 0) {
           return gravityBoards.get(channel);
        }
        else {
            return null;
        }
    }

    public Collection<Integer> getListenerChannels() {
        return listenerChannels;
    }

    public Collection<CH423> getGravityBoards() {
        return gravityBoards.values();
    }

    public int getCurrentChannel() {
        if(gravityBoards.containsKey(channel)) {
            return channel;
        }
        else {
            return 0;
        }
    }
}
