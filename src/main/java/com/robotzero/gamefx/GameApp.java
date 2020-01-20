package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.renderengine.utils.Timer;
import org.joml.Vector3f;

import java.util.Optional;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class GameApp implements Runnable {
    private static final float CAMERA_POS_STEP = 10.40f;
    public static final int TARGET_FPS = 75;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;
    private Mesh background;
    private Texture bgTexture;
    private float SIZE = 1.0f;
    private Camera camera;
    private final Timer timer;
    private static float width = 284f, height = 512f;
    private int fps;
    private double lastFps;
    public static final int TARGET_UPS = 30;
    private boolean sceneChanged;
    private final Vector3f cameraInc;

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

    public GameApp(DisplayManager displayManager, Render render2D, Camera camera, Timer timer) {
        this.displayManager = displayManager;
        this.render2D = render2D;
        this.camera = camera;
        this.timer = timer;
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
    }

    public void run() {
        try {
            init();
            gameLoop();
        } catch (Exception excp) {
            excp.printStackTrace();
        } finally {
            cleanup();
        }
    }


    private void init() throws Exception {
        displayManager.createDisplay();
        timer.init();
        render2D.init();
        lastFps = timer.getTime();
        fps = 0;
        bgTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bg.jpeg")).orElseThrow().getPath());
        background = new Mesh(vertices1, tcs1, indices, bgTexture);
    }

    public void gameLoop() {
        float elapsedTime;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;

        boolean running = true;
        while (running && !displayManager.windowShouldClose()) {
            elapsedTime = timer.getElapsedTime();
            accumulator += elapsedTime;

            input();

            while (accumulator >= interval) {
                update(interval);
                accumulator -= interval;
            }

            render();

            sync();
        }
    }

    private void render() {
        if ( timer.getLastLoopTime() - lastFps > 1 ) {
            lastFps = timer.getLastLoopTime();
            DisplayManager.TITLE = DisplayManager.TITLE + " - " + fps + " FPS";
            fps = 0;
        }
        fps++;
        render2D.render(displayManager.getWindow(), background);
        displayManager.updateDisplay();
    }

    protected void input() {
        if (displayManager.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;
            cameraInc.x = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;
            cameraInc.x = 1;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;
            cameraInc.y = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;
            cameraInc.y = 1;
        }
    }

    private void sync() {
        float loopSlot = 1f / TARGET_FPS;
        double endTime = timer.getLastLoopTime() + loopSlot;
        while (timer.getTime() < endTime) {
            try {
                Thread.sleep(1);
            } catch (InterruptedException ie) {
                System.out.println("BLAH");
            }
        }
    }

    protected void cleanup() {
        render2D.cleanup();
        background.cleanUp();
        glfwDestroyWindow(this.displayManager.getWindow());
        glfwTerminate();
    }

    private void update(float interval) {
        camera.movePosition(cameraInc.x * CAMERA_POS_STEP, cameraInc.y * CAMERA_POS_STEP, cameraInc.z * CAMERA_POS_STEP);
        camera.updateViewMatrix();
    }
}
