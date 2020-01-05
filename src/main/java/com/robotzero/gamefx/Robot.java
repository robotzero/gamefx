package main.java.com.robotzero.gamefx;

import main.java.com.robotzero.gamefx.renderengine.Camera;
import main.java.com.robotzero.gamefx.renderengine.DisplayManager;
import main.java.com.robotzero.gamefx.renderengine.Render;
import main.java.com.robotzero.gamefx.renderengine.Render2D;

public class Robot {
    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            Camera camera = new Camera();
            DisplayManager displayManager = new DisplayManager(camera);
            Render render = new Render2D();
            GameApp gameApp = new GameApp(displayManager, render, camera);
            displayManager.createDisplay();
            gameApp.start();
        } catch (Throwable t) {
            System.exit(1);
        }
        System.exit(0);
    }
}
