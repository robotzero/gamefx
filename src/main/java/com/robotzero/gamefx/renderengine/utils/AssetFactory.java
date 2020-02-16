package com.robotzero.gamefx.renderengine.utils;

import com.robotzero.gamefx.renderengine.PlayerService;
import com.robotzero.gamefx.renderengine.model.Material;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.world.TileMap;

import java.util.Optional;

public class AssetFactory {
    private static float backgroundWidth = 284f, backgroundHeight = 512f;
    private static float birdWidth = 125f, birdHeight = 126f;

    private final float[] vertices1 = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, backgroundHeight, 0.0f,
            backgroundWidth, backgroundHeight, 0.0f,
            backgroundWidth, 0, 0.0f
    };

    private final float[] vertices2 = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, TileMap.MetersToPixels * PlayerService.PlayerHeight, 0.0f,
            TileMap.MetersToPixels * PlayerService.PlayerWidth, TileMap.MetersToPixels * PlayerService.PlayerHeight, 0.0f,
            TileMap.MetersToPixels * PlayerService.PlayerWidth, 0.0f, 0.0f
    };

    private final float[] vertices3 = new float[] {
            0.0f, 0.0f, 0.0f,
            0.0f, TileMap.TileSideInPixels, 0.0f,
            TileMap.TileSideInPixels, TileMap.TileSideInPixels, 0.0f,
            TileMap.TileSideInPixels, 0.0f, 0.0f
    };

    private final float[] tcs1 = new float[] {
            0, 0,
            0, 1,
            1, 1,
            1, 0
    };

    private final float[] tcs2 = new float[] {
            0, 0,
            0, 1,
            1, 1,
            1, 0
    };

    private final int[] indices = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    private final int[] indices2 = new int[] {
            0, 1, 2,
            2, 3, 0
    };

    private Mesh background;
    private Texture bgTexture;
    private Texture birdTexture;
    private Mesh bird;
    private Mesh quad2D;

    public void init() {
        bgTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bg.jpeg")).orElseThrow().getPath());
        background = new Mesh(vertices1, tcs1, indices, bgTexture, null);
        birdTexture = new Texture(Optional.ofNullable(this.getClass().getClassLoader().getResource("bird.png")).orElseThrow().getPath());
        bird = new Mesh(vertices2, tcs2, indices2, birdTexture, null);
        quad2D = new Mesh(vertices3, tcs1, indices, null, new Material());
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
}
