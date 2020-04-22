package com.robotzero.gamefx;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
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
            World world = new World();
            RenderGroupService renderGroupService = new RenderGroupService();
            EntityService entityService = new EntityService(gameMemory, world, renderGroupService);
            Camera camera = new Camera();
            Renderer2D renderer2D = new Renderer2D(entityService, camera);
            GameApp gameApp = new GameApp(displayManager, renderer2D, timer, entityService, gameMemory, renderGroupService);
            gameApp.run();
        } catch (Throwable t) {
            System.out.println(t.toString());
            System.out.println(t.getMessage());
            t.printStackTrace();
            Optional.ofNullable(gameMemory).ifPresent(GameMemory::free);
            System.exit(1);
        }
        System.exit(0);
    }
}
