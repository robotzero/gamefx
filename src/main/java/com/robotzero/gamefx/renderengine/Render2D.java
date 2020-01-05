package main.java.com.robotzero.gamefx.renderengine;

import main.java.com.robotzero.gamefx.renderengine.utils.Texture;
import main.java.com.robotzero.gamefx.renderengine.utils.VertexArray;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;
import static org.lwjgl.glfw.GLFW.glfwSwapBuffers;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;

public class Render2D implements Render {
    private int map = 0;

    public void render(final long window, Texture bgTexture, VertexArray background, float[] viewMatrix) {
        glfwPollEvents();
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);

        bgTexture.bind();
        Shader.BG.enable();
        background.bind();
        for (int i = map; i <=1; i++) {
            Shader.BG.setUniformMat4f("vw_matrix", viewMatrix);
            background.draw();
        }
        Shader.BG.disable();
        bgTexture.unbind();

        int error = glGetError();
        if (error != GL_NO_ERROR)
            System.out.println(error);

        glfwSwapBuffers(window);
    }
}
