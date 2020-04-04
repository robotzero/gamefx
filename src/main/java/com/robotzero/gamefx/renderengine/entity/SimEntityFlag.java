package com.robotzero.gamefx.renderengine.entity;

public enum SimEntityFlag {
    COLLIDES(1 << 0), NONSPATIAL(1 << 1), SIMMING(1 << 30), MOVEABLE(1 << 2), ZSUPPORTED(1 << 4), TRAVERSABLE(0);

    int i;
    SimEntityFlag(int i) {
        this.i = i;
    }

    int getI() {
        return this.i;
    }
}
