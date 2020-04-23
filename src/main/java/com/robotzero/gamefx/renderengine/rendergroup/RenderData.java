package com.robotzero.gamefx.renderengine.rendergroup;

import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderData {
    private Vector3f min;
    private Vector3f max;
    private Vector4f Color;

    public RenderData(Vector3f min, Vector3f max, Vector4f Color) {
        this.min = min;
        this.max = max;
        this.Color = Color;
    }

    public Vector3f getMin() {
        return min;
    }

    public Vector3f getMax() {
        return max;
    }

    public Vector4f getColor() {
        return Color;
    }
}
