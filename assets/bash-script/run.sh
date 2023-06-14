#!/bin/bash
I2CNOTSET=true
i2cset -y 1 0x70 0x4 && I2CNOTSET=false
if $I2CNOTSET
then
        sleep 1
        i2cdetect -y 1
        sleep 1
        i2cset -y 1 0x70 0x4
        sleep 1
fi
export DISPLAY=:0
xhost local:root
sudo mvn -f /opt/flow javafx:run
sudo sh /opt/flow/run.sh