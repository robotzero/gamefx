package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.model.Texture;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;
import static org.lwjgl.glfw.GLFW.glfwWindowShouldClose;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glEnable;

public class GameApp {
    private Thread thread;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;
    private Mesh background;
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

    int[] indices = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    public GameApp(DisplayManager displayManager, Render render2D, Camera camera) {
        this.displayManager = displayManager;
        this.render2D = render2D;
        this.camera = camera;
    }

    void start() throws Exception {
        running = true;
        render2D.init();
        run();
//        thread = new Thread(this, "Game");
//        thread.start();
    }

    public void run() {
        GLUtil.setupDebugMessageCallback();

        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        bgTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bg.jpeg")).orElseThrow().getPath());
        background = new Mesh(vertices1, tcs1, indices, bgTexture);

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
            render2D.render(displayManager.getWindow(), background);
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
