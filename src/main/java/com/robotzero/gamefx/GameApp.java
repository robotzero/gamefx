package com.robotzero.gamefx;

import com.jogamp.opengl.math.Matrix4;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.Shader;
import com.robotzero.gamefx.renderengine.utils.Texture;
import com.robotzero.gamefx.renderengine.utils.VertexArray;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL13.GL_TEXTURE0;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GameApp {
    private Thread thread;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;
    private VertexArray background;
    private Texture bgTexture;
    private float SIZE = 1.0f;
    private Camera camera;
    private static float width = 284f, height = 512f;

    float[] vertices1 = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, height, 0.0f,
            width, height, 0.0f,
            width, 0, 0.0f
    };

    float[] tcs1 = new float[] {
            0, 0,
            0, 1,
            1, 1,
            1, 0
    };

    byte[] indices = new byte[] {
            0, 1, 2,
            2, 3, 0
    };

    public GameApp(DisplayManager displayManager, Render render2D, Camera camera) {
        this.displayManager = displayManager;
        this.render2D = render2D;
        this.camera = camera;
    }

    void start() {
        running = true;
        run();
//        thread = new Thread(this, "Game");
//        thread.start();
    }

//    @Override
    public void run() {
        GLUtil.setupDebugMessageCallback();

        glEnable(GL_DEPTH_TEST);
        glActiveTexture(GL_TEXTURE0);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Shader.loadAll();
        final Matrix4 perspective = new com.jogamp.opengl.math.Matrix4();
        perspective.makeOrtho(0.0f, 1024f, 768f, 0, -1.0f, 1.0f);
        Shader.BG.setUniformMat4f("pr_matrix", perspective.getMatrix());
        Shader.BG.setUniform1i("tex", 0);

        background = new VertexArray(vertices1, indices, tcs1);
        bgTexture = new Texture(this.getClass().getClassLoader().getResource("bg.jpeg").getPath());

        long lastTime = System.nanoTime();
        double delta = 0.0;
        double ns = 1000000000.0 / 60.0;
        long timer = System.currentTimeMillis();
        int updates = 0;
        int frames = 0;
        GL.createCapabilities();

        while (running) {
            long now = System.nanoTime();
            delta += (now - lastTime) / ns;
            lastTime = now;
            if (delta >= 1.0) {
                update();
                updates++;
                delta--;
            }
            render2D.render(displayManager.getWindow(), bgTexture, background, camera.getViewMatrix());
            frames++;
            if (System.currentTimeMillis() - timer > 1000) {
                timer += 1000;
                System.out.println(updates + " ups, " + frames + " fps");
                updates = 0;
                frames = 0;
            }
            if (glfwWindowShouldClose(this.displayManager.getWindow())) {
                running = false;
            }
        }

        glfwDestroyWindow(this.displayManager.getWindow());
        glfwTerminate();
    }

    private void update() {
//        level.update();
//        if (level.isGameOver()) {
//            level = new Level();
//        }
    }
}
