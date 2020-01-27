package com.robotzero.gamefx.renderengine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Camera {
    private static final float CAMERA_POS_STEP = 10.40f;
    private Vector3f position = new Vector3f(0f, 0f, 0f);

    public Matrix4f updateViewMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(new Vector3f(-position.x, -position.y, -position.z));
    }

    public Matrix4f getProjectionMatrix() {
        final Matrix4f p = new Matrix4f();
        return p.ortho(0.0f, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f, -1, 1);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        position = position.add(offsetX * CAMERA_POS_STEP, offsetY * CAMERA_POS_STEP, 0);
    }
}
