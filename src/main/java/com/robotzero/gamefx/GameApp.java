package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Player;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.utils.AssetFactory;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import org.joml.Vector2f;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_W;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_X;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_Z;
import static org.lwjgl.glfw.GLFW.glfwDestroyWindow;
import static org.lwjgl.glfw.GLFW.glfwTerminate;

public class GameApp implements Runnable {
    public static final int TARGET_FPS = 60;
//    public static final int TARGET_FPS = (int) DisplayManager.refreshRate;
    private boolean running = false;
    private final DisplayManager displayManager;
    private final Render render2D;
    private float SIZE = 1.0f;
    private Camera camera;
    private final Timer timer;
    public static int fps;
    private double lastFps;
    public static final int TARGET_UPS = 30;
    private boolean sceneChanged;
    private final Vector3f cameraInc;
    private final AssetFactory assetFactory;
    private Mesh background;
    private Mesh bird;
    private Mesh quad;
    private final Player player;
    private Vector2f ddPlayer;
    private final GameMemory gameMemory;
    private float playerSpeed;

    public GameApp(DisplayManager displayManager, Render render2D, Camera camera, Timer timer, AssetFactory assetFactory, Player player, GameMemory g) {
        this.displayManager = displayManager;
        this.render2D = render2D;
        this.camera = camera;
        this.timer = timer;
        this.assetFactory = assetFactory;
        this.player = player;
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.ddPlayer = new Vector2f(0.0f, 0.0f);
        this.gameMemory = g;
        Camera.position.Offset.x = 0;
        Camera.position.Offset.y = 0;
        Camera.position.AbsTileX = 17/2;
        Camera.position.AbsTileY = 9/2;
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
        assetFactory.init();
        background = assetFactory.getBackgroundMesh();
        bird = assetFactory.getBirdMesh();
        quad = assetFactory.getQuadMesh();
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
            displayManager.setWindowTitle(DisplayManager.TITLE + " - " + fps + " FPS");
            fps = 0;
        }
        fps++;
        render2D.render(displayManager.getWindow(), background, bird, quad);
        displayManager.updateDisplay();
    }

    protected void input() {
        TileMap.TileMapPosition OldPlayerP = new TileMap.TileMapPosition(Player.positionc);
        playerSpeed = 10f;
        cameraInc.set(0f, 0f, 0f);
        ddPlayer.set(0f, 0f);
        int heroFacingDirection = 0;
        if (displayManager.isKeyPressed(GLFW_KEY_W)) {
            sceneChanged = true;
            cameraInc.y = -1;
            ddPlayer.y = 1;
            heroFacingDirection = 1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_S)) {
            sceneChanged = true;
            cameraInc.y = 1;
            ddPlayer.y = -1;
            heroFacingDirection = 2;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_A)) {
            sceneChanged = true;
            cameraInc.x = -1;
            ddPlayer.x = -1;
            heroFacingDirection = 3;
        } else if (displayManager.isKeyPressed(GLFW_KEY_D)) {
            sceneChanged = true;
            cameraInc.x = 1;
            ddPlayer.x = 1;
            heroFacingDirection = 4;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_Z)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_X)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }

        if((ddPlayer.x != 0.0f) && (ddPlayer.y != 0.0f))
        {
            ddPlayer = ddPlayer.mul(0.707106781187f);
        }

        if (displayManager.isKeyPressed(GLFW_KEY_SPACE)) {
            playerSpeed = 50f;
        }

        ddPlayer = ddPlayer.mul(playerSpeed);
        ddPlayer = ddPlayer.add(new Vector2f(Player.dPlayerP.x(), Player.dPlayerP.y()).mul(-1.5f));
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
        gameMemory.free();
        glfwDestroyWindow(this.displayManager.getWindow());
        glfwTerminate();
    }

    private void update(float interval) {
        player.movePosition(ddPlayer, interval);
        camera.movePosition(Player.positionc, Camera.position);
    }
}
