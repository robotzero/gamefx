package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;

public class Camera {
    public static World.WorldPosition position = new World.WorldPosition();
    public static World.WorldPosition oldPosition = new World.WorldPosition();

    public Matrix4f updateViewMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity();
    }

    public Matrix4f getProjectionMatrix() {
        final Matrix4f p = new Matrix4f();
        return p.ortho(0.0f, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f, -1, 1);
    }
}
