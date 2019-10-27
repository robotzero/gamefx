package main.java.com.robotzero.gamefx;

import main.java.com.robotzero.gamefx.renderengine.DisplayManager;
import main.java.com.robotzero.gamefx.renderengine.Render;
import main.java.com.robotzero.gamefx.renderengine.Render2D;

public class Robot {
    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            DisplayManager displayManager = new DisplayManager();
            Render render = new Render2D();
            GameApp gameApp = new GameApp(displayManager, render);
            displayManager.createDisplay();
            gameApp.start();
        } catch (Throwable t) {
            System.exit(1);
        }
        System.exit(0);
    }
}
