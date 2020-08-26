package com.robotzero.gamefx.renderengine.entity;

public enum EntityFlag {
    COLLIDES(1 << 0), ACTIVE(1 << 2), DELETED(1 << 4);

    int i;
    EntityFlag(int i) {
        this.i = i;
    }

    int getI() {
        return this.i;
    }
}
