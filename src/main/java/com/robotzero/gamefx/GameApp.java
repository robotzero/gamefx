package com.robotzero.gamefx;

import com.almasb.fxgl.app.GameApplication;
import com.almasb.fxgl.app.GameSettings;

public class GameApp extends GameApplication {
    @Override
    protected void initSettings(GameSettings gameSettings) {
        gameSettings.setTitle("Breakout Underwater");
        gameSettings.setVersion("0.2");
        gameSettings.setWidth(1920);
        gameSettings.setHeight(1080);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
