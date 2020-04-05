package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.utils.FileUtils;
import com.robotzero.gamefx.renderengine.utils.ShaderProgram;
import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glViewport;

public class Render2D implements Render {
    private ShaderProgram sceneShaderProgram;
    private World world;
    private ShaderProgram birdShaderProgram;
    private ShaderProgram quadShaderProgram;
    private ShaderProgram familiarShaderProgram;
    private final Camera camera;
    private final EntityService entityService;

    public Render2D(Camera camera, EntityService entityService, World world) {
        this.camera = camera;
        this.entityService = entityService;
        this.world = world;
    }

    public void init() throws Exception {
//        setupSceneShader();
        setupBirdShader();
        setUpQuadShader();
        setUpFamiliarShader();
    }

    public void clear() {
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final long window, Mesh background2, Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1) {
        clear();
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
        renderScene(background2, bird, quad, familiar, rectangle1);

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            System.out.println(error);
        }
    }

    private void renderScene(Mesh background, Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1) {
        final var translations = entityService.getModelMatrix();
        if (!translations.isEmpty()) {
            Matrix4f viewMatrix = camera.updateViewMatrix();
            Matrix4f projectionMatrix = camera.getProjectionMatrix();
            //@@TODO one player so far so we just get it.
            Matrix4f playerModelMatrix = translations.get(EntityType.HERO).get(0);
            Matrix4f quadViewMatrix = new Matrix4f().identity();
//        sceneShaderProgram.bind();
//        sceneShaderProgram.setUniform("vw_matrix", viewMatrix);
//        sceneShaderProgram.setUniform("pr_matrix", projectionMatrix);
//        sceneShaderProgram.setUniform("tex", 1);sssssssssssss
//
//        background.render();
//        background.endRender();
//        sceneShaderProgram.unbind();
//
            birdShaderProgram.bind();
            birdShaderProgram.setUniform("vw_matrix", viewMatrix);
            birdShaderProgram.setUniform("pr_matrix", projectionMatrix);
            birdShaderProgram.setUniform("ml_matrix", playerModelMatrix);
            birdShaderProgram.setUniform("tex", 1);
            bird.render();
            bird.endRender();
            birdShaderProgram.unbind();

            quadShaderProgram.bind();
            quadShaderProgram.setUniform("pr_matrix", projectionMatrix);
            quadShaderProgram.setUniform("t_color", quad.getMaterial().getColor());
            quadShaderProgram.setUniform("vw_matrix", viewMatrix);
            translations.get(EntityType.WALL).forEach((key) -> {
                quadShaderProgram.setUniform("ml_matrix", key);
                quadShaderProgram.setUniform("t_color", new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
                quad.render();
                quad.endRender();
            });
            translations.get(EntityType.SPACE).forEach((key) -> {
                  quadShaderProgram.setUniform("ml_matrix", key);
                  quadShaderProgram.setUniform("t_color", new Vector4f(1.0f, 0.5f, 0.0f, 1.0f));
                  quad.render();
                  quad.endRender();
            });

//            translations.get(EntityType.DEBUG).forEach((key) -> {
//                quadShaderProgram.setUniform("ml_matrix", key);
//                quadShaderProgram.setUniform("t_color", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
//                rectangle1.render();
//                rectangle1.endRender();
//            });
            quadShaderProgram.unbind();

//            Matrix4f familiarMatrix = entityService.getModelMatrix().get(EntityType.FAMILIAR).get(0).getValue();
//            familiarShaderProgram.bind();
//            familiarShaderProgram.setUniform("pr_matrix", projectionMatrix);
//            familiarShaderProgram.setUniform("ml_matrix", familiarMatrix);
//            familiarShaderProgram.setUniform("t_color", new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
//            familiar.render();
//            familiar.endRender();
//            familiarShaderProgram.unbind();
        }
    }

    private void setupSceneShader() throws Exception {
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/background.vert"));
        sceneShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/background.frag"));
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

    private void setUpQuadShader() throws Exception {
        quadShaderProgram = new ShaderProgram();
        quadShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/quad2D.vert"));
        quadShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/quad2D.frag"));
        quadShaderProgram.link();

        // Create uniforms for view and projection matrices
        quadShaderProgram.createUniform("vw_matrix");
        quadShaderProgram.createUniform("pr_matrix");
        quadShaderProgram.createUniform("t_color");
        quadShaderProgram.createUniform("ml_matrix");
    }

    private void setUpFamiliarShader() throws Exception {
        familiarShaderProgram = new ShaderProgram();
        familiarShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/familiar.vert"));
        familiarShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/familiar.frag"));
        familiarShaderProgram.link();

        // Create uniforms for view and projection matrices
        familiarShaderProgram.createUniform("vw_matrix");
        familiarShaderProgram.createUniform("pr_matrix");
        familiarShaderProgram.createUniform("t_color");
        familiarShaderProgram.createUniform("ml_matrix");
    }

    public void cleanup() {
//        sceneShaderProgram.cleanup();
        familiarShaderProgram.cleanup();
        birdShaderProgram.cleanup();
        quadShaderProgram.cleanup();
    }
}
