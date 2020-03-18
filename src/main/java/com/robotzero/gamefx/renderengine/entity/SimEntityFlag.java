package com.robotzero.gamefx.renderengine.entity;

public enum SimEntityFlag {
    COLLIDES(1 << 1), NONSPATIAL(1 << 2), SIMMING(1 << 30);

    int i;
    SimEntityFlag(int i) {
        this.i = i;
    }

    int getI() {
        return this.i;
    }
}
