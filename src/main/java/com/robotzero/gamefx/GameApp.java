package main.java.com.robotzero.gamefx;

import main.java.com.robotzero.gamefx.renderengine.DisplayManager;
import main.java.com.robotzero.gamefx.renderengine.Render;
import main.java.com.robotzero.gamefx.renderengine.Shader;
import main.java.com.robotzero.gamefx.renderengine.math.Matrix4f;
import main.java.com.robotzero.gamefx.renderengine.utils.Texture;
import main.java.com.robotzero.gamefx.renderengine.utils.VertexArray;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL13.GL_TEXTURE1;
import static org.lwjgl.opengl.GL13.glActiveTexture;

public class GameApp {
    private Thread thread;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;
    private VertexArray background;
    private Texture bgTexture;

    float[] vertices = new float[] {
            -10.0f, -10.0f * 9.0f / 16.0f, 0.0f,
            -10.0f,  10.0f * 9.0f / 16.0f, 0.0f,
            0.0f,  10.0f * 9.0f / 16.0f, 0.0f,
            0.0f, -10.0f * 9.0f / 16.0f, 0.0f
    };

    byte[] indices = new byte[] {
            0, 1, 2,
            2, 3, 0
    };

    float[] tcs = new float[] {
            0, 1,
            0, 0,
            1, 0,
            1, 1
    };

    public GameApp(DisplayManager displayManager, Render render2D) {
        this.displayManager = displayManager;
        this.render2D = render2D;
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
        glActiveTexture(GL_TEXTURE1);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        Shader.loadAll();
        Matrix4f pr_matrix = Matrix4f.orthographic(-10.0f, 10.0f, -10.0f * 9.0f / 16.0f, 10.0f * 9.0f / 16.0f, -1.0f, 1.0f);
        Shader.BG.setUniformMat4f("pr_matrix", pr_matrix);
        Shader.BG.setUniform1i("tex", 1);

        background = new VertexArray(vertices, indices, tcs);
        bgTexture = new Texture("res/bg.jpeg");

        System.out.println("RUN");
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
            render2D.render(displayManager.getWindow(), bgTexture, background);
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
