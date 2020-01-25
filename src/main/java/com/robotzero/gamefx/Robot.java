package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Player;
import com.robotzero.gamefx.renderengine.Render;
import com.robotzero.gamefx.renderengine.Render2D;
import com.robotzero.gamefx.renderengine.utils.AssetFactory;
import com.robotzero.gamefx.renderengine.utils.Timer;

public class Robot {
    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            Camera camera = new Camera();
            Timer timer = new Timer();
            DisplayManager displayManager = new DisplayManager();
            AssetFactory assetFactory = new AssetFactory();
            Player player = new Player();
            Render render = new Render2D(camera, player);
            GameApp gameApp = new GameApp(displayManager, render, camera, timer, assetFactory, player);
            gameApp.run();
        } catch (Throwable t) {
            System.out.println(t.toString());
            System.out.println(t.getMessage());
            System.exit(1);
        }
        System.exit(0);
    }
}
