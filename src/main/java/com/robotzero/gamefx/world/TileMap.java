package com.robotzero.gamefx.world;

import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class TileMap {
    private final int[][] tileMap =
    {
        {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
        {1, 1, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1},
        {1, 1, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 1, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 1},
        {0, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 0},
        {1, 1, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 1, 0, 0, 1},
        {1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0, 1},
        {1, 1, 1, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1},
        {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
    };

    public static final int tileWidth = 60;
    public static final int tileHeight = 60;

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public TileMap() {
        this.generateTilePositions();
    }

    private void generateTilePositions() {
        for(int row = 0;
            row < 9;
            ++row)
        {
            for(int column = 0;
                column < 17;
                ++column) {
                int tileID = tileMap[row][column];
                float color = 0.5f;
                if (tileID == 1) {
                    color = 1.0f;
                }
                tilePositions.put(new Vector3f(column * tileWidth, row * tileHeight, 0), color);
            }
        }
    }

    public Map<Vector3f, Float> getTilePositions() {
        return this.tilePositions;
    }
}
