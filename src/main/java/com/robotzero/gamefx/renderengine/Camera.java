package main.java.com.robotzero.gamefx.renderengine;

import com.jogamp.opengl.math.Matrix4;
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

    public float[] getViewMatrix() {
        final Matrix4 id = new com.jogamp.opengl.math.Matrix4();
        id.loadIdentity();
        id.translate(position.x, position.y, position.z);
        return id.getMatrix();
    }
}
