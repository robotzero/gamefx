package com.robotzero.gamefx.renderengine;

import org.joml.Vector2f;
import org.joml.Vector2fc;

public class TileMapPosition {
    public TileMapPosition(int absTileX, int absTileY, Vector2fc offset) {
        this.absTileX = absTileX;
        this.absTileY = absTileY;
        this.offset = offset;
    }

    private int absTileX;
    private int absTileY;

    private Vector2fc offset;

    public int getAbsTileX() {
        return absTileX;
    }

    public void setAbsTileX(int absTileX) {
        this.absTileX = absTileX;
    }

    public int getAbsTileY() {
        return absTileY;
    }

    public void setAbsTileY(int absTileY) {
        this.absTileY = absTileY;
    }

    public Vector2fc getOffset() {
        return offset;
    }

    public void setOffset(float x, float y) {
        this.offset = new Vector2f(x, y);
    }
}
