package main.java.com.robotzero.gamefx.renderengine;

import main.java.com.robotzero.gamefx.renderengine.math.Matrix4f;
import main.java.com.robotzero.gamefx.renderengine.math.Vector3f;
import main.java.com.robotzero.gamefx.renderengine.utils.Texture;
import main.java.com.robotzero.gamefx.renderengine.utils.VertexArray;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;

public class Render2D implements Render {
    private int map = 0;

    public void render(final long window, Texture bgTexture, VertexArray background) {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
//        glClearColor(0.8f, 0.0f, 0.0f, 0.0f);
//        level.render();

        bgTexture.bind();
        Shader.BG.enable();
        Shader.BG.setUniform2f("bird", 0, 20);
        background.bind();
        for (int i = map; i < map + 4; i++) {
            Shader.BG.setUniformMat4f("vw_matrix", Matrix4f.translate(new Vector3f(i * 10 + 0 * 0.03f, 0.0f, 0.0f)));
            background.draw();
        }
        Shader.BG.disable();
        bgTexture.unbind();

        int error = glGetError();
        if (error != GL_NO_ERROR)
            System.out.println(error);

        glfwSwapBuffers(window);
        glfwPollEvents();
    }
}
