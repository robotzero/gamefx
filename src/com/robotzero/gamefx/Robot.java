package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.DisplayManager;

public class Robot {
    public static void main(String[] args) {
        try {
            Thread.setDefaultUncaughtExceptionHandler((t, e) -> {
                throw new RuntimeException("There was an unhandled exception in thread " + t.getName(), e);
            });
            DisplayManager displayManager = new DisplayManager();
            displayManager.createDisplay();
            displayManager.updateDisplay();
        } catch (Throwable t) {
            System.exit(1);
        }
        System.exit(0);
    }
}
