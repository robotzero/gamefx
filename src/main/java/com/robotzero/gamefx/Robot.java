package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Player;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.Render2D;
import com.robotzero.gamefx.renderengine.utils.AssetFactory;
import com.robotzero.gamefx.renderengine.utils.Timer;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import com.robotzero.gamefx.world.WorldGenerator;

import java.util.Optional;

public class Robot {
    public static void main(String[] args) {
        GameMemory gameMemory = null;
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            gameMemory = new GameMemory();
            WorldGenerator worldGenerator = new WorldGenerator();
            TileMap tileMap = new TileMap(worldGenerator);
            Camera camera = new Camera(tileMap);
            Timer timer = new Timer();
            DisplayManager displayManager = new DisplayManager();
            AssetFactory assetFactory = new AssetFactory();
            Player player = new Player(tileMap);
            Render render = new Render2D(camera, player, tileMap);
            GameApp gameApp = new GameApp(displayManager, render, camera, timer, assetFactory, player, gameMemory);
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
