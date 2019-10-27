package main.java.com.robotzero.gamefx;

import main.java.com.robotzero.gamefx.renderengine.DisplayManager;
import main.java.com.robotzero.gamefx.renderengine.Render;
import org.lwjgl.opengl.GL;

import static org.lwjgl.glfw.GLFW.*;

public class GameApp {
    private Thread thread;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;

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
            render2D.render(displayManager.getWindow());
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
