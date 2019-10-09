package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.DisplayManager;

public class Robot {
    public static void main(String[] args) {
        DisplayManager displayManager = new DisplayManager();
        displayManager.createDisplay();
        displayManager.updateDisplay();
    }
}
