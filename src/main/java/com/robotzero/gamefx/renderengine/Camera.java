package com.robotzero.gamefx.renderengine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position = new Vector3f(0,0,0);

    public void update(int key) {
        if ( key == GLFW_KEY_W ) {
            position.y-=1.02f;
        } else if ( key == GLFW_KEY_A ) {
            position.x+=1.02f;
        } else if ( key == GLFW_KEY_S ) {
            position.y+=1.02f;
        } else if ( key == GLFW_KEY_D ) {
            position.x+=1.02f;
        }
    }

    public Matrix4f updateViewMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(position.x, position.y, position.z);
    }


    public Matrix4f getProjectionMatrix() {
        final Matrix4f p = new Matrix4f();
        return p.ortho2D(0.0f, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f);
    }

    public void movePosition(float offsetX, float offsetY, float offsetZ) {
        position.x += offsetX;
        position.y += offsetY;
    }
}
