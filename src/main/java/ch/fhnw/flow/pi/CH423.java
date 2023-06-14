package ch.fhnw.flow.pi;

import com.pi4j.exception.Pi4JException;
import com.pi4j.io.i2c.I2C;
import com.pi4j.io.i2c.I2CConfig;

import java.util.List;
import java.util.function.Consumer;

import static ch.fhnw.flow.pi.HardwareCommand.pi4j;

public class CH423 {

    private I2C CH423_CMD_SET_SYSTEMARGS;
    // Set low 8bit general-purpose output command
    private I2C CH423_CMD_SET_GPO_L;
    // Set high 8bit general-purpose output command
    private I2C CH423_CMD_SET_GPO_H;
    // Set bi-directional input/output pin command
    private I2C CH423_CMD_SET_GPIO;
    // Set bi-directional input/output pin command
    private I2C CH423_CMD_READ_GPIO;

    private final int eGPOTOTAL = GPO.values().length;

    // Bi-directional input/output pin, GPIO0
    public enum GPO {
        eGPO0(0, "GPO0"),
        eGPO1(1, "GPO1"),
        eGPO2(2, "GPO2"),
        eGPO3(3, "GPO3"),
        eGPO4(4, "GPO4"),
        eGPO5(5, "GPO5"),
        eGPO6(6, "GPO6"),
        eGPO7(7, "GPO7"),
        eGPO8(8, "GPO8"),
        eGPO9(9, "GPO9"),
        eGPO10(10, "GPO10"),
        eGPO11(11, "GPO11"),
        eGPO12(12, "GPO12"),
        eGPO13(13, "GPO13"),
        eGPO14(14, "GPO14"),
        eGPO15(15, "GPO15"),
        ;
        private int gpo;
        private String gpoStr;

        GPO(int gpo, String gpoStr) {
            this.gpo = gpo;
            this.gpoStr = gpoStr;
        }

        @Override
        public String toString() {
            return gpoStr;
        }

        public int toInteger() {
            return gpo;
        }
    }

    private final int eGPIO_TOTAL = GPIO.values().length;

    public enum GPIO {
        eGPIO0(0, "GPIO0"),
        eGPIO1(1, "GPIO1"),
        eGPIO2(2, "GPIO2"),
        eGPIO3(3, "GPIO3"),
        eGPIO4(4, "GPIO4"),
        eGPIO5(5, "GPIO5"),
        eGPIO6(6, "GPIO6"),
        eGPIO7(7, "GPIO7"),
        ;
        private int gpio;
        private String gpioStr;

        GPIO(int gpio, String gpioStr) {
            this.gpio = gpio;
            this.gpioStr = gpioStr;
        }

        @Override
        public String toString() {
            return gpioStr;
        }

        public int toInteger() {
            return gpio;
        }
    }

    // Bi-directional input/output pin, GPIO0~GPIO7
    public static final int eGPIO = 0;
    // General output pin, GPO0~GPO15
    public static final int eGPO = 1;
    // General output pin, GPO0~GPO7
    public static final int eGPO0To7 = 2;
    // General output pin, GPO8~GPO15
    public static final int eGPO8To15 = 3;

    // GPIO pin input mode, at high level when floating
    public static final int eINPUT = 0;
    // GPIO pin output mode, can output high/low level
    public static final int eOUTPUT = 1;
    // GPO pin open-drain output mode, can only be used for eGPO07 and eGPO815 digital ports. And GPO can only output low level or do not output in this mode
    public static final int eOPENDRAIN = 2;
    // GPO pin push-pull output mode, can only be used for eGPO07 and eGPO815 digital ports. And GPO can output high or low level.
    public static final int ePUSHPULL = 3;

    // configure pin interrupt, low level interrupt
    public static final int eLOW = 0;
    // configure pin interrupt, high level interrupt
    public static final int eHIGH = 1;
    // configure pin interrupt, rising edge interrupt
    public static final int eRISING = 2;
    // configure pin interrupt, falling edge interrupt
    public static final int eFALLING = 3;
    public static final int eCHANGE = 4;

    private static final int ARGS_BIT_IO_EN = 0;
    private static final int ARGS_BIT_DEC_L = 1;
    private static final int ARGS_BIT_DEC_H = 2;
    private static final int ARGS_BIT_INT_EN = 3;
    private static final int ARGS_BIT_OD_EN = 4;
    private static final int ARGS_BIT_SLEEP = 6;
    private int args;
    private int[] modes =  new int[8];
    private Consumer<Integer>[] consumers;
    private int intValue;
    private int gpo0To7;
    private int gpo8To15;
    private static final int busAddr = 1;
    private int channel;
    private TCA9548A tca9548A;

    public CH423(int channel, TCA9548A tca, boolean listener) {
        this.channel = channel;
        tca.addGravityBoard(this);
        if (listener) {
            tca.addGravityBoardToListener(this.channel);
        }
        tca.selectChannel(channel);
        setAddressPorts();
        setConstrParams();
        this.tca9548A = tca;
        this.begin();
    }

    public void setAddressPorts() {
        CH423_CMD_SET_SYSTEMARGS = createI2C(0x48 >> 1);
        // Set low 8bit general-purpose output command
        CH423_CMD_SET_GPO_L = createI2C(0x44 >> 1);
        // Set high 8bit general-purpose output command
        CH423_CMD_SET_GPO_H = createI2C(0x46 >> 1);
        // Set bi-directional input/output pin command
        CH423_CMD_SET_GPIO = createI2C(0x30);
        // Set bi-directional input/output pin command
        CH423_CMD_READ_GPIO = createI2C(0x4D >> 1);
    }

    public I2C createI2C(int deviceAddr) {
        I2CConfig i2cConfig = I2C.newConfigBuilder(pi4j)
                .id("CH423" + channel + "" + deviceAddr)
                .bus(busAddr)
                .device(deviceAddr)
                .provider("linuxfs-i2c")
                .build();
        return pi4j.create(i2cConfig);
    }

    public void setConstrParams() {
        this.args = 0;
        this.modes = new int[8];
        this.consumers = new Consumer[8];
        this.intValue = 0;
        this.gpo0To7 = 0;
        this.gpo8To15 = 0;
    }

    public void begin() {
        begin(eINPUT, ePUSHPULL);
    }

    public void begin(int gpioMode, int gpoMode) {
        args = 0;
        if (gpioMode < eOPENDRAIN) {
            if (gpioMode == eOUTPUT)
                args |= 1 << ARGS_BIT_IO_EN;
        }
        if (gpoMode > eOUTPUT && gpoMode <= ePUSHPULL) {
            if (gpoMode == eOPENDRAIN)
                args |= 1 << ARGS_BIT_OD_EN;
        }
        setSystemArgs();
        intValue = 0xFF;
    }

    public void pinMode(int group, int mode) {
        if (group == eGPIO && mode <= eOPENDRAIN) {
            if (mode == eINPUT) {
                args &= ((~(1 << ARGS_BIT_IO_EN)) & 0xFF);
            } else {
                args |= 1 << ARGS_BIT_IO_EN;
            }
            setSystemArgs();
        } else if (group > eGPIO && mode > eOPENDRAIN) {
            if (mode == eOPENDRAIN) {
                args |= 1 << ARGS_BIT_OD_EN;
            } else {
                args &= ((~(1 << ARGS_BIT_OD_EN)) & 0xFF);
            }
        }
    }

    public void gpioDigitalWrite(int gpio, int level) {
        if (gpio < GPIO.eGPIO0.toInteger() || gpio > eGPIO_TOTAL) {
            throw new RuntimeException("gpio argument range error.");
        }
        if (level < 0 || level > 0xFF) {
            throw new RuntimeException("level argument range(0~0xFF) error.");
        }
        if (gpio == eGPIO_TOTAL) {
            CH423_CMD_SET_GPIO.write(level);
            return;
        }
        int state = readGpio();
        if (level != 0) {
            level = state | (1 << gpio);
        } else {
            level = state & (~(1 << gpio));
        }
        CH423_CMD_SET_GPIO.write(level);
    }

    public void gpoDigitalWrite(int gpo, int level) {
        if (gpo < GPO.eGPO0.toInteger() || gpo > eGPOTOTAL) {
            throw new RuntimeException("gpo argument range error.");
        }
        if (level < 0 || level > 0xFF) {
            throw new RuntimeException("level argument range(0~0xFF) error.");
        }
        if (gpo == eGPOTOTAL) {
            gpo8To15 = level;
            gpo0To7 = level;
            CH423_CMD_SET_GPO_H.write(gpo8To15);
            CH423_CMD_SET_GPO_L.write(gpo0To7);
            return;
        }
        if (gpo > GPO.eGPO7.toInteger()) {
            if (level != 0) {
                gpo8To15 |= (1 << (gpo - 8));
            } else {
                gpo8To15 &= (~(1 << (gpo - 8)));
            }
            CH423_CMD_SET_GPO_H.write(gpo8To15);
        } else {
            if (level != 0) {
                gpo0To7 |= (1 << gpo);
            } else {
                gpo0To7 &= (~(1 << gpo));
            }
            CH423_CMD_SET_GPO_L.write(gpo0To7);
        }
    }

    public void groupDigitalWrite(int group, int level) {
        if (group < eGPIO || group > eGPO8To15)
            throw new RuntimeException("group argument range error.");
        if (level < 0 || level > 0xFFFF)
            throw new RuntimeException("level argument range(0~0xFF) error.");
        if (group == eGPIO) {
            int cmd = level & 0xFF;
            CH423_CMD_SET_GPIO.write(cmd);
        } else if (group == eGPO) {
            gpo8To15 = (level >> 8) & 0xFF;
            gpo0To7 = level & 0xFF;
            CH423_CMD_SET_GPO_H.write(gpo8To15);
            CH423_CMD_SET_GPO_L.write(gpo0To7);
        } else if (group == eGPO0To7) {
            gpo0To7 = level & 0xFF;
            CH423_CMD_SET_GPO_L.write(gpo0To7);
        } else if (group == eGPO8To15) {
            gpo8To15 = (level >> 8) & 0xFF;
            CH423_CMD_SET_GPO_H.write(gpo8To15);
        }
    }

    public int gpioDigitalRead(int gpio) {
        if (gpio < GPIO.eGPIO0.toInteger() || gpio > eGPIO_TOTAL) return 0; // gpio argument range error
        int result = readGpio();
        return (gpio == eGPIO_TOTAL) ? result : (result >> gpio) & 1;
    }

    public void gpioAttachInterrupt(int gpio, int mode, Consumer<Integer> consumer) {
        if (gpio < GPIO.eGPIO0.toInteger() || gpio > eGPIO_TOTAL)
            throw new RuntimeException("gpio argument range error.");
        if (mode < eLOW | mode > eCHANGE)
            throw new RuntimeException("mode argument range error.");
        int bit = (mode == eLOW || mode == eRISING) ? 0 : 1;
        if (gpio == eGPIO_TOTAL) {
            if (bit != 0) intValue = 0xFF;
            else intValue = 0x00;
            int i = 0;
            while (i < 8) {
                modes[i] = mode;
                consumers[i] = consumer;
                i += 1;
            }
            return;
        }
        if (bit != 0) intValue |= (1 << gpio);
        else intValue &= (~(1 << gpio));
        modes[gpio] = mode;
        consumers[gpio] = consumer;
    }

    public void enable_interrupt() {
        args |= (1 << ARGS_BIT_INT_EN);
        args &= ~(1 << ARGS_BIT_DEC_H);
        setSystemArgs();
        gpioDigitalWrite(eGPIO_TOTAL, intValue);
    }

    public void disableInterrupt() {
        args &= ~(1 << ARGS_BIT_INT_EN);
        setSystemArgs();
    }

    public boolean pollInterrupts() {
        boolean isInterrupted = false;
        int state = readGpio();
        int temp = intValue;
        int i = 0;
        Boolean flag = false;
        while (i < 8) {
            int bit = (state >> i) & 1;
            int bit1 = (intValue >> i) & 1;
            if (consumers[i] != null && bit != bit1) {
                boolean isModeDown = (modes[i] == eLOW || modes[i] == eFALLING) && (bit != 0);
                boolean isModeUp = (((modes[i] == eHIGH) || (modes[i] == eRISING)) && (bit != 1));
                if (modes[i] == eCHANGE && (isModeDown || isModeUp)) {
                    flag = true;
                    if (bit != 0) temp |= 1 << i;
                    else temp &= ~(1 << i);
                }
                consumers[i].accept(i);
                isInterrupted = true;
            }
            i += 1;
        }
        intValue = temp;
        if (flag) {
            gpioDigitalWrite(eGPIO_TOTAL, temp);
        }
        return isInterrupted;
    }

    public void sleep() {
        args |= 1 << ARGS_BIT_SLEEP;
        setSystemArgs();
        args &= ~(1 << ARGS_BIT_SLEEP);
    }

    public void addEventListener(int gpio, int mode, Consumer<Integer> consumer) {
        gpioAttachInterrupt(gpio, mode, consumer);
    }

    public void setSystemArgs() {
        CH423_CMD_SET_SYSTEMARGS.write(args);
    }

    public int readGpio() {
        try {
            return CH423_CMD_READ_GPIO.read();
        } catch (Pi4JException e) {
            // TODO: log
            return 0;
        }
    }

    public int getChannel() {
        return this.channel;
    }

    public TCA9548A getTca9548A() {
        return tca9548A;
    }
}