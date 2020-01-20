package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.utils.FileUtils;
import com.robotzero.gamefx.renderengine.utils.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glViewport;

public class Render2D implements Render {
    private ShaderProgram sceneShaderProgram;
    private final Camera camera;

    public Render2D(Camera camera) {
        this.camera = camera;
    }

    public void init() throws Exception {
        setupSceneShader();
    }

    public void clear() {
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final long window, Mesh background2) {
        clear();
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        renderScene();
        background2.render();
        background2.endRender();
        sceneShaderProgram.unbind();

//        int error = glGetError();
//        if (error != GL_NO_ERROR)
//            System.out.println(error);
//
//        glfwSwapBuffers(window);
    }

    private void renderScene() {
        sceneShaderProgram.bind();

        Matrix4f viewMatrix = camera.updateViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        sceneShaderProgram.setUniform("vw_matrix", viewMatrix);
        sceneShaderProgram.setUniform("pr_matrix", projectionMatrix);
        sceneShaderProgram.setUniform("tex", 0);

//        sceneShaderProgram.unbind();
    }

    private void setupSceneShader() throws Exception {
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/background3.vert"));
        sceneShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/background3.frag"));
        sceneShaderProgram.link();

        // Create uniforms for view and projection matrices
        sceneShaderProgram.createUniform("vw_matrix");
        sceneShaderProgram.createUniform("pr_matrix");
        sceneShaderProgram.createUniform("tex");
    }

    public void cleanup() {
        sceneShaderProgram.cleanup();
    }
}
