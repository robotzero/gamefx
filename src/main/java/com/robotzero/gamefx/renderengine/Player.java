package com.robotzero.gamefx.renderengine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Player {
    private static final float PLAYER_POS_STEP = 10.40f;

    private Vector3f position = new Vector3f(200f, 300f, 1f);

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(position.x, position.y, position.z);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        position.x += offsetX * PLAYER_POS_STEP;
        position.y += offsetY * PLAYER_POS_STEP;
        position.z += offsetZ * PLAYER_POS_STEP;
    }
}
