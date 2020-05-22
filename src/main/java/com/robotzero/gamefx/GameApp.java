package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.assets.AssetService;
import com.robotzero.gamefx.renderengine.entity.ControlledHero;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.entity.SimEntity;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.rendergroup.GameRenderCommands;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroup;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
import com.robotzero.gamefx.renderengine.utils.Random;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldGenerator;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.logging.Level;
import java.util.logging.Logger;

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
    public static float Time;
    //    public static final int TARGET_FPS = (int) DisplayManager.refreshRate;
    private final DisplayManager displayManager;
    private final Renderer2D renderer2D;
    private final Timer timer;
    public static int fps;
    private final RenderGroupService renderGroupService;
    private double lastFps;
    public static final int TARGET_UPS = 60;
    private final Vector3f cameraInc;
    private final EntityService entityService;
    private final GameMemory gameMemory;
    public static float ZoomRate = 1.0f;
    public static int playerSpeed;
    private int LowIndex;
    private SimEntity monstar;
    public static float globalinterval;
    public static Vector2f ScreenCenter;
    public static RenderGroup renderGroup;
    public static World.WorldPosition SimCenterP;
    public static Vector3f CameraP;
    private final AssetService assetService;

    public GameApp(DisplayManager displayManager, Renderer2D renderer2D, Timer timer, EntityService entityService, GameMemory g, RenderGroupService renderGroupService, AssetService assetService) {
        this.displayManager = displayManager;
        this.renderer2D = renderer2D;
        this.timer = timer;
        this.cameraInc = new Vector3f(0.0f, 0.0f, 0.0f);
        this.gameMemory = g;
        this.entityService = entityService;
        this.renderGroupService = renderGroupService;
        this.assetService = assetService;
        Camera.position.Offset.x = 0;
        Camera.position.Offset.y = 0;
        entityService.AddLowEntity(EntityType.NULL, entityService.NullPosition(), null);
        gameMemory.HighEntityCount = 1;
        gameMemory.StandardRoomCollision = entityService.MakeSimpleGroundedCollision(WorldGenerator.tilesPerWidth * World.TileSideInMeters, WorldGenerator.tilesPerHeight * World.TileSideInMeters, 0.9f * World.TileDepthInMeters);
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
        assetService.LoadAssets("assets");
        GameRenderCommands gameRenderCommands = new GameRenderCommands();
        gameRenderCommands.Width = DisplayManager.WIDTH;
        gameRenderCommands.Height = DisplayManager.HEIGHT;
        GameRenderCommands.allocate(gameRenderCommands);
        renderGroup = entityService.BeginRenderGroup(gameMemory.gameAssets, gameRenderCommands, false);
        renderer2D.init();
        lastFps = timer.getTime();
        fps = 0;
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

    public void gameLoop() throws Exception {
        float delta;
        float accumulator = 0f;
        float interval = 1f / TARGET_UPS;
        float alpha;

        boolean running = true;
        while (running) {
            if (displayManager.isClosing()) {
                running = false;
            }
            /* Get delta time and update the accumulator */
            delta = timer.getDelta();
            accumulator += delta;

            input();

//            while (accumulator >= interval) {
               update(1f / TARGET_UPS);
               timer.updateUPS();
               accumulator -= interval;
//            }

            alpha = accumulator / interval;

            /* Render game and update timer FPS */
            render();
            entityService.EndSim(gameMemory.simRegion);
            timer.updateFPS();

            /* Update timer */
            timer.update();

//            displayManager.updateDisplay();
            sync();
        }
    }

    private void render() throws Exception {
        if ( timer.getLastLoopTime() - lastFps > 1 ) {
            lastFps = timer.getLastLoopTime();
            displayManager.setWindowTitle(DisplayManager.TITLE + " - " + fps + " FPS");
            fps = 0;
        }
        fps++;
        renderGroupService.render(renderGroup,null, 1);
        displayManager.updateDisplay();
    }

    protected void input() {
        playerSpeed = 1;
        cameraInc.set(0f, 0f, 0f);
        gameMemory.ControlledHero.ddP.set(0f, 0f, 0f);
        ZoomRate = 0.0f;
        int heroFacingDirection = 0;
        if (displayManager.isKeyPressed(GLFW_KEY_W, true)) {
            cameraInc.y = -1;
            gameMemory.ControlledHero.ddP.y = 1;
            heroFacingDirection = 1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_S, true)) {
            cameraInc.y = 1;
            gameMemory.ControlledHero.ddP.y = -1;
            heroFacingDirection = 2;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_A, true)) {
            cameraInc.x = -1;
            gameMemory.ControlledHero.ddP.x = -1;
            heroFacingDirection = 3;
        } else if (displayManager.isKeyPressed(GLFW_KEY_D, gameMemory.simRegion != null)) {
            cameraInc.x = 1;
            gameMemory.ControlledHero.ddP.x = 1;
            heroFacingDirection = 4;
        }
        if (displayManager.isKeyPressed(GLFW_KEY_Z, true)) {
            cameraInc.z = -1;
        } else if (displayManager.isKeyPressed(GLFW_KEY_X, true)) {
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
        double lastLoopTime = timer.getLastLoopTime();
        double now = timer.getTime();
        float targetTime = 1f / TARGET_FPS;

        while (now - lastLoopTime < targetTime) {
            Thread.yield();

            /* This is optional if you want your game to stop consuming too much
             * CPU but you will loose some accuracy because Thread.sleep(1)
             * could sleep longer than 1 millisecond */
            try {
                Thread.sleep(1);
            } catch (InterruptedException ex) {
                Logger.getLogger(GameApp.class.getName()).log(Level.SEVERE, null, ex);
            }

            now = timer.getTime();
        }
    }

    protected void cleanup() {
        assetService.cleanUp();
        renderer2D.dispose();
        gameMemory.free();
        glfwDestroyWindow(displayManager.getWindow());
        glfwDestroyWindow(assetService.getSharedWindow());
        glfwTerminate();
    }

    private void update(float interval) {
        GameRenderCommands gameRenderCommands = new GameRenderCommands();
        gameRenderCommands.Width = DisplayManager.WIDTH;
        gameRenderCommands.Height = DisplayManager.HEIGHT;
        GameRenderCommands.allocate(gameRenderCommands);
        entityService.BeginRenderGroup(gameMemory.gameAssets, gameRenderCommands, false);
        float WidthOfMonitor = 0.635f;
        float MetersToPixels = DisplayManager.WIDTH * WidthOfMonitor;
        float FocalLength = 0.6f;
        float DistanceAboveGround = 9.0f;
        renderGroupService.Perspective(renderGroup, DisplayManager.WIDTH, DisplayManager.HEIGHT, MetersToPixels, FocalLength, DistanceAboveGround);
        ScreenCenter = new Vector2f(0.5f * gameRenderCommands.Width, 0.5f * gameRenderCommands.Height);
        globalinterval = interval;
        Rectangle ScreenBounds = renderGroupService.GetCameraRectangleAtTarget(renderGroup);
        Rectangle CameraBoundsInMeters = Rectangle.RectMinMax(new Vector3f(ScreenBounds.getMin()), new Vector3f(ScreenBounds.getMax()));

        CameraBoundsInMeters.getMin().z = -3.0f * World.TypicalFloorHeight;
        CameraBoundsInMeters.getMax().z = 1.0f * World.TypicalFloorHeight;

        Vector3f SimBoundsExpansion = new Vector3f(15.0f, 15.0f, 0.0f);
        Rectangle SimBounds = entityService.AddRadiusTo(CameraBoundsInMeters, SimBoundsExpansion);
        SimCenterP = new World.WorldPosition(Camera.position);
        gameMemory.simRegion = entityService.BeginSim(SimCenterP, SimBounds, globalinterval);

        CameraP = World.subtract(new World.WorldPosition(Camera.position), new World.WorldPosition(SimCenterP));
        renderGroupService.PushRectOutline(renderGroup, EntityService.DefaultFlatTransform(), new Vector3f(0.0f, 0.0f, 0.0f), Rectangle.GetDimV2(ScreenBounds), new Vector4f(1.0f, 1.0f, 0.0f, 1.0f), EntityType.DEBUG);
        renderGroupService.PushRectOutline(renderGroup, EntityService.DefaultFlatTransform(), new Vector3f(0.0f, 0.0f, 0.0f), Rectangle.GetDimV2(SimBounds), new Vector4f(0.0f, 1.0f, 1.0f, 1.0f), EntityType.DEBUG);
        renderGroupService.PushRectOutline(renderGroup, EntityService.DefaultFlatTransform(), new Vector3f(0.0f, 0.0f, 0.0f), Rectangle.GetDimV2(gameMemory.simRegion.Bounds), new Vector4f(1.0f, 0.0f, 1.0f, 1.0f), EntityType.DEBUG);
        entityService.pushToRender(renderGroup);
    }
}
