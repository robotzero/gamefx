package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.assets.AssetService;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;

import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Robot {
    public static void main(String[] args) {
        GameMemory gameMemory = null;
        ExecutorService executor = Executors.newCachedThreadPool();
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            gameMemory = new GameMemory();
            Timer timer = new Timer();
            DisplayManager displayManager = new DisplayManager();
            World world = new World();
            Camera camera = new Camera();
            Renderer2D renderer2D = new Renderer2D(camera);
            RenderGroupService renderGroupService = new RenderGroupService(renderer2D, executor, gameMemory);
            EntityService entityService = new EntityService(gameMemory, world, renderGroupService, executor);
            AssetService assetService = new AssetService(executor, gameMemory, displayManager);
            GameApp gameApp = new GameApp(displayManager, renderer2D, timer, entityService, gameMemory, renderGroupService, assetService);
            gameApp.run();
        } catch (Throwable t) {
            System.out.println(t.toString());
            System.out.println(t.getMessage());
            t.printStackTrace();
            Optional.ofNullable(gameMemory).ifPresent(GameMemory::free);
            executor.shutdownNow();
            System.exit(1);
        }
        executor.shutdownNow();
        System.exit(0);
    }
}
