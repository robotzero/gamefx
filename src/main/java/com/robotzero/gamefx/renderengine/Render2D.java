package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.utils.FileUtils;
import com.robotzero.gamefx.renderengine.utils.ShaderProgram;
import org.joml.Matrix4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glViewport;

public class Render2D implements Render {
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram birdShaderProgram;
    private final Camera camera;

    public Render2D(Camera camera) {
        this.camera = camera;
    }

    public void init() throws Exception {
        setupSceneShader();
        setupBirdShader();
    }

    public void clear() {
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final long window, Mesh background2, Mesh bird) {
        clear();
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        renderScene(background2, bird);


//        int error = glGetError();
//        if (error != GL_NO_ERROR)
//            System.out.println(error);
//
//        glfwSwapBuffers(window);
    }

    private void renderScene(Mesh background, Mesh bird) {
        Matrix4f viewMatrix = camera.updateViewMatrix();
        Matrix4f projectionMatrix = camera.getProjectionMatrix();
        Matrix4f modelMatrix = camera.getModelMatrix();
        sceneShaderProgram.bind();
        sceneShaderProgram.setUniform("vw_matrix", viewMatrix);
        sceneShaderProgram.setUniform("pr_matrix", projectionMatrix);
        sceneShaderProgram.setUniform("tex", 1);

        background.render();
        background.endRender();
        sceneShaderProgram.unbind();

        birdShaderProgram.bind();
        birdShaderProgram.setUniform("vw_matrix", viewMatrix);
        birdShaderProgram.setUniform("pr_matrix", projectionMatrix);
        birdShaderProgram.setUniform("ml_matrix", modelMatrix);
        birdShaderProgram.setUniform("tex", 1);
        bird.render();
        bird.endRender();
        birdShaderProgram.unbind();
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

    private void setupBirdShader() throws Exception {
        birdShaderProgram = new ShaderProgram();
        birdShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/bird.vert"));
        birdShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/bird.frag"));
        birdShaderProgram.link();

        // Create uniforms for view and projection matrices
        birdShaderProgram.createUniform("vw_matrix");
        birdShaderProgram.createUniform("pr_matrix");
        birdShaderProgram.createUniform("tex");
        birdShaderProgram.createUniform("ml_matrix");
    }

    public void cleanup() {
        sceneShaderProgram.cleanup();
        birdShaderProgram.cleanup();
    }
}
