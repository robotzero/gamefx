package com.robotzero.gamefx.world;

import java.util.LinkedList;
import java.util.Objects;

public class WorldChunk {
    private int ChunkX = 0;
    private int ChunkY = 0;
    private LinkedList<WorldEntityBlock> FirstBlock = new LinkedList<>();

    public void setTileChunkX(int tileChunkX) {
        ChunkX = tileChunkX;
    }

    public void setTileChunkY(int tileChunkY) {
        ChunkY = tileChunkY;
    }

    public LinkedList<WorldEntityBlock> getFirstBlock() {
        if (FirstBlock.isEmpty()) {
            FirstBlock.add(new WorldEntityBlock());
        }
        return FirstBlock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorldChunk that = (WorldChunk) o;
        return ChunkX == that.ChunkX &&
                ChunkY == that.ChunkY;
    }

    @Override
    public int hashCode() {
        return Objects.hash(ChunkX, ChunkY);
    }
}