package com.robotzero.gamefx.renderengine.rendergroup;

import org.joml.Vector3f;

public class RenderData {
    private Vector3f min;
    private Vector3f max;

    public RenderData(Vector3f min, Vector3f max) {
        this.min = min;
        this.max = max;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }
}
