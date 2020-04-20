package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;

public class Camera {
    public static World.WorldPosition position = new World.WorldPosition();
    private final Matrix4f identity = new Matrix4f().identity();

    public Matrix4f getIdentity() {
        return identity;
    }

    public Matrix4f getProjectionMatrix() {
        return new Matrix4f().ortho(0f, DisplayManager.WIDTH, 0f, DisplayManager.HEIGHT, -1f, 1f);
//        return new Matrix4f().ortho(0.0f, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f, -1, 1);
    }
}
