package com.robotzero.gamefx.world;

import java.util.LinkedList;
import java.util.Objects;

public class WorldChunk {
    private int ChunkX = 0;
    private int ChunkY = 0;
    private final LinkedList<WorldEntityBlock> FirstBlock = new LinkedList<>();

    public void setTileChunkX(int tileChunkX) {
        ChunkX = tileChunkX;
    }

    public void setTileChunkY(int tileChunkY) {
        ChunkY = tileChunkY;
    }

    public int getChunkX() {
        return ChunkX;
    }

    public int getChunkY() {
        return ChunkY;
    }

    public LinkedList<WorldEntityBlock> getFirstBlock() {
        return FirstBlock;
    }

    public WorldEntityBlock setFirstBlock(WorldEntityBlock worldEntityBlock) {
        WorldEntityBlock free = null;
        if (!FirstBlock.isEmpty()) {
            free = FirstBlock.getFirst();
            FirstBlock.clear();
        }
        FirstBlock.add(worldEntityBlock);
        return free;
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