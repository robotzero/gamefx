package com.robotzero.gamefx.world;

import java.nio.ByteBuffer;

public class TileChunk {
    private ByteBuffer tiles;
    private long TileChunkX = 0;
    private long TileChunkY = 0;

    public ByteBuffer getTiles() {
        return tiles;
    }

    public void setTile(int coords, byte value) {
        this.tiles.put(coords, value);
    }

    public void setTiles(ByteBuffer tiles) {
        this.tiles = tiles;
    }

    public long getTileChunkX() {
        return TileChunkX;
    }

    public void setTileChunkX(long tileChunkX) {
        TileChunkX = tileChunkX;
    }

    public long getTileChunkY() {
        return TileChunkY;
    }

    public void setTileChunkY(long tileChunkY) {
        TileChunkY = tileChunkY;
    }
}