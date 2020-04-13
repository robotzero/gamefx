package com.robotzero.gamefx.renderengine.utils;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.model.Material;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.world.World;

import java.util.Optional;

public class AssetFactory {
    private static float backgroundWidth = 284f, backgroundHeight = 512f;

    private final float[] tcs = new float[] {
            0, 0,
            0, 1,
            1, 1,
            1, 0
    };

    private final int[] indices = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    private Mesh background;
    private Texture bgTexture;
    private Texture birdTexture;
    private Mesh bird;
    private Mesh quad2D;
    private Mesh familiar;
    private Mesh rectangle1;

    public void init() {
        bgTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bg.jpeg")).orElseThrow().getPath());
        background = new Mesh(getVertices(backgroundWidth, backgroundHeight), tcs, indices, bgTexture, null);
        birdTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bird.png")).orElseThrow().getPath());
        bird = new Mesh(getVertices(World.MetersToPixels * EntityService.PlayerWidth, World.MetersToPixels * EntityService.PlayerHeight), tcs, indices, birdTexture, null);
        quad2D = new Mesh(getVertices(World.TileSideInPixels * GameApp.ZoomRate, World.TileSideInPixels * GameApp.ZoomRate), tcs, indices, null, new Material());
        familiar = new Mesh(getVertices(World.MetersToPixels * EntityService.FamiliarWidth, World.MetersToPixels * EntityService.FamiliarHeight), tcs, indices, null, new Material());
        rectangle1 = new Mesh(getVertices(DisplayManager.WIDTH, DisplayManager.HEIGHT), tcs, indices, null, new Material());
    }

    public Mesh getBirdMesh() {
        return bird;
    }

    public Mesh getBackgroundMesh() {
        return background;
    }

    public Mesh getQuadMesh() {
        return quad2D;
    }

    public Mesh getFamiliarMesh() {
        return familiar;
    }

    public Mesh getRectangle1() {
        return rectangle1;
    }

    private float[] getVertices(float width, float height) {
        return new float[] {
                0.0f, 0.0f, 0.0f,
                0.0f, height, 0.0f,
                width, height, 0.0f,
                width, 0.0f, 0.0f
        };
    }
}
