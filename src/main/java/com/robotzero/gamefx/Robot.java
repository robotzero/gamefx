package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.EntityService;
import com.robotzero.gamefx.renderengine.PlayerService;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.Render2D;
import com.robotzero.gamefx.renderengine.utils.AssetFactory;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;

import java.util.Optional;

public class Robot {
    public static void main(String[] args) {
        GameMemory gameMemory = null;
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            gameMemory = new GameMemory();
            Timer timer = new Timer();
            DisplayManager displayManager = new DisplayManager();
            AssetFactory assetFactory = new AssetFactory();
            World world = new World();
            EntityService entityService = new EntityService(gameMemory, world);
            Camera camera = new Camera(gameMemory, entityService);
            PlayerService playerService = new PlayerService(world, entityService, gameMemory);
            Render render = new Render2D(camera, playerService, world);
            GameApp gameApp = new GameApp(displayManager, render, camera, timer, assetFactory, playerService, entityService, gameMemory, world);
            gameApp.run();
        } catch (Throwable t) {
            System.out.println(t.toString());
            System.out.println(t.getMessage());
            Optional.ofNullable(gameMemory).ifPresent(GameMemory::free);
            System.exit(1);
        }
        System.exit(0);
    }
}
