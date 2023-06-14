#!/usr/bin/env bash
export DISPLAY=:0 && xhost local:root
i2cdetect -y 1
i2cset -y 1 0x70 0x4
i2cdetect -y 1
i2cset -y 1 0x70 0x4
java --module-path . --module ch.fhnw.flow/ch.fhnw.flow.game.GUI $@