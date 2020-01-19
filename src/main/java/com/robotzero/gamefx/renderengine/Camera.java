package com.robotzero.gamefx.renderengine;

import com.jogamp.opengl.math.Matrix4;
import org.joml.Matrix4f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.*;

public class Camera {
    private Vector3f position = new Vector3f(0,0,0);

    public void update(int key) {
        if ( key == GLFW_KEY_W ) {
            position.y-=10.02f;
        } else if ( key == GLFW_KEY_A ) {
            position.x+=10.02f;
        } else if ( key == GLFW_KEY_S ) {
            position.y+=10.02f;
        } else if ( key == GLFW_KEY_D ) {
            position.x+=10.02f;
        }
    }

    public Matrix4f getViewMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(position.x, position.y, position.z);
    }


    public Matrix4f getProjectionMatrix() {
        final Matrix4f p = new Matrix4f();
        p.ortho2D(0.0f, 1024f, 768f, 0);

        return p;
    }
}
