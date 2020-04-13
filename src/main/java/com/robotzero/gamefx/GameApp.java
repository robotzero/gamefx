package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.entity.ControlledHero;
import com.robotzero.gamefx.renderengine.entity.SimEntity;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.utils.AssetFactory;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldGenerator;
import com.robotzero.gamefx.renderengine.utils.Random;
import org.joml.Vector3f;

import static org.lwjgl.glfw.GLFW.GLFW_KEY_A;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_B;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_D;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_S;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_SPACE;
import static org.lwjgl.glfw.GLFW.GLFW_KEY_V;
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
    private final Timer timer;
    public static int fps;
    private double lastFps;
    public static final int TARGET_UPS = 60;
    private boolean sceneChanged;
    private final Vector3f cameraInc;
    private final AssetFactory assetFactory;
    private Mesh background;
    private Mesh bird;
    private Mesh quad;
    private Mesh familiarA;
    private final EntityService entityService;
    private final GameMemory gameMemory;
    private final World world;
    public static float ZoomRate = 0.2f;
    public static int playerSpeed;
    private int LowIndex;
    private SimEntity monstar;
    public static float globalinterval;
    int TileSpanX = 17 * 3;
    int TileSpanY = 9 * 3;

    public GameApp(DisplayManager displayManager, Render render2D, Timer timer, AssetFactory assetFactory, EntityService entityService, GameMemory g, World world) {
        this.displayManager = displayManager;
        this.render2D = render2D;
        this.timer = timer;
        this.assetFactory = assetFactory;
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.gameMemory = g;
        this.entityService = entityService;
        this.world = world;
        Camera.position.Offset.x = 0;
        Camera.position.Offset.y = 0;
        entityService.AddLowEntity(EntityType.NULL, entityService.NullPosition(), null, null);
        gameMemory.HighEntityCount = 1;
        World.renderWorld(entityService);
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
        familiarA = assetFactory.getFamiliarMesh();

        World.WorldPosition NewCameraP = entityService.ChunkPositionFromTilePosition(
                WorldGenerator.CameraTileX,
                WorldGenerator.CameraTileY,
                new Vector3f(0.0f, 0.0f, 0.5f * World.TileDepthInMeters)
        );
//        entityService.AddMonstar(WorldGenerator.CameraTileX + 2, WorldGenerator.CameraTileY + 2);
        Camera.position = NewCameraP;
        for (int FamiliarIndex = 0; FamiliarIndex < 1; ++FamiliarIndex) {
            int FamiliarOffsetX = (Random.randomNumberTable[WorldGenerator.randomNumberIndex++] % 10) - 7;
            int FamiliarOffsetY = (Random.randomNumberTable[WorldGenerator.randomNumberIndex++] % 10) - 3;
            if ((FamiliarOffsetX != 0) || (FamiliarOffsetY != 0)) {
//                entityService.AddFamiliar(WorldGenerator.CameraTileX + FamiliarOffsetX, WorldGenerator.CameraTileY + FamiliarOffsetY);
            }
        }
        LowIndex =  entityService.AddPlayer().LowIndex;
        gameMemory.ControlledHero = new ControlledHero();
        gameMemory.ControlledHero.ddP = new Vector3f(0.0f, 0.0f, 0.0f);
        gameMemory.ControlledHero.EntityIndex = LowIndex;
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
            entityService.EndSim(gameMemory.simRegion);
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
        render2D.render(displayManager.getWindow(), background, bird, quad, familiarA, assetFactory.getRectangle1());
        displayManager.updateDisplay();
    }

    protected void input() {
        playerSpeed = 1;
        cameraInc.set(0f, 0f, 0f);
        gameMemory.ControlledHero.ddP.set(0f, 0f, 0f);
        ZoomRate = 0.0f;
        int heroFacingDirection = 0;
        if (displayManager.isKeyPressed(GLFW_KEY_W, true)) {
            sceneChanged = true;
            cameraInc.y = -1;
            gameMemory.ControlledHero.ddP.y = 1;
            heroFacingDirection = 1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_S, true)) {
            sceneChanged = true;
            cameraInc.y = 1;
            gameMemory.ControlledHero.ddP.y = -1;
            heroFacingDirection = 2;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_A, true)) {
            sceneChanged = true;
            cameraInc.x = -1;
            gameMemory.ControlledHero.ddP.x = -1;
            heroFacingDirection = 3;
        } else if (displayManager.isKeyPressed(GLFW_KEY_D, gameMemory.simRegion != null)) {
            sceneChanged = true;
            cameraInc.x = 1;
            gameMemory.ControlledHero.ddP.x = 1;
            heroFacingDirection = 4;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_Z, true)) {
            sceneChanged = true;
            cameraInc.z = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_X, true)) {
            sceneChanged = true;
            cameraInc.z = 1;
        }

        if (displayManager.isKeyPressed(GLFW_KEY_SPACE, true)) {
            playerSpeed = 180;
        }

        if (displayManager.isKeyPressed(GLFW_KEY_V, true)) {
            ZoomRate = 1.0f;
        }

        if (displayManager.isKeyPressed(GLFW_KEY_B, true)) {
            ZoomRate = -1.0f;
        }

        GameMemory.ZOffset = GameMemory.ZOffset + ZoomRate * globalinterval;
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
        globalinterval = interval;
        Rectangle CameraBounds = Rectangle.RectCenterDim(new Vector3f(0f, 0f, 0f), new Vector3f(TileSpanX, TileSpanY, 0f).mul(World.TileSideInMeters));
        gameMemory.simRegion = entityService.BeginSim(Camera.position, CameraBounds, interval);
    }
}
