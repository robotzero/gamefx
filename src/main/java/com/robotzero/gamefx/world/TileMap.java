package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Player;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMap {
    public static final int TileChunkCountX = 128;
    public static final int TileChunkCountY = 128;
    public static final int ChunkShift = 4;
    public static final int ChunkMask = (1 << ChunkShift) - 1;
    public static final int ChunkDim = 1 << ChunkShift;
    public static final int TileSideInPixels = 60;
    public static final int LowerLeftX = - (TileSideInPixels / 2);
    public static final int LowerLeftY = DisplayManager.HEIGHT;
    public static final float TileSideInMeters = 1.4f;
    public static final float MetersToPixels = TileSideInPixels / TileSideInMeters;
    public static float ScreenCenterX = 0.5f * DisplayManager.WIDTH;
    public static float ScreenCenterY = 0.5f * DisplayManager.HEIGHT;
    private final WorldGenerator worldGenerator;

    private List<TileChunk> tileChunks = new ArrayList<>();

    public static final int tileWidth = 60;
    public static final int tileHeight = 60;

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public TileMap(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
        for (int y = 0; y < TileChunkCountY; ++y) {
            for (int x = 0; x < TileChunkCountX; ++x) {
                tileChunks.add(y * TileChunkCountX + x, new TileChunk());
            }
        }

        this.worldGenerator.renderWorld(this);
    }

    private void generateTilePositions() {
        for(int RelRow = -10;
            RelRow < 10;
            ++RelRow)
        {
            for(int RelColumn = -20;
                RelColumn < 20;
                ++RelColumn) {
                int Column = Camera.position.AbsTileX + RelColumn;
                int Row = Camera.position.AbsTileY + RelRow;
                int tileID = GetTileValue(Column, Row);

                float color = 0.5f;
                if(tileID == 2) {
                    color = 1.0f;
                }

                if(tileID > 2) {
                    color = 0.25f;
                }

                if((Column == Camera.position.AbsTileX) &&
                        (Row == Camera.position.AbsTileY))
                {
                    color = 0.0f;
                }

                float CenX = ScreenCenterX - MetersToPixels * Camera.position.OffsetX + RelColumn * TileSideInPixels;
                float CenY = ScreenCenterY + MetersToPixels * Camera.position.OffsetY - RelRow * TileSideInPixels;
                float MinX = CenX - 0.5f * TileSideInPixels;
                float MinY = CenY - 0.5f * TileSideInPixels;
                float MaxX = CenX + 0.5f * TileSideInPixels;
                float MaxY = CenY + 0.5f * TileSideInPixels;

                tilePositions.put(new Vector3f(MinX, MinY, 0), color);
            }
        }
    }

    public Map<Vector3f, Float> getTilePositions() {
        this.tilePositions.clear();
        this.generateTilePositions();
        return this.tilePositions;
    }

    private TileChunk GetTileChunk(int TileChunkX, int TileChunkY)
    {
        TileChunk tileMap = null;

        if((TileChunkX >= 0) && (TileChunkX < TileChunkCountX) &&
                (TileChunkY >= 0) && (TileChunkY < TileChunkCountY))
        {
            tileMap = tileChunks.get(TileChunkY * TileChunkCountX + TileChunkX);
        }

        return tileMap;
    }

    int GetTileValueUnchecked(TileChunk tileChunk, int TileX, int TileY)
    {
        return tileChunk.getTiles().get(TileY * ChunkDim + TileX);
    }

    int GetTileValue(TileChunk TileChunk, int TestTileX, int TestTileY)
    {
        int TileChunkValue = 0;

        if(TileChunk != null && TileChunk.getTiles() != null && TileChunk.getTiles().limit() > 0) {
            TileChunkValue = GetTileValueUnchecked(TileChunk, TestTileX, TestTileY);
        }

        return(TileChunkValue);
    }

    public boolean IsWorldPointEmpty(TileMapPosition CanPos)
    {
        boolean Empty;

        int TileChunkValue = GetTileValue(CanPos.AbsTileX, CanPos.AbsTileY);
        Empty = (TileChunkValue == 1);

        return(Empty);
    }

    TileMapPosition RecanonicalizeCoord(TileMapPosition Pos)
    {
        int OffsetX = (int) Math.floor(Pos.OffsetX / TileSideInMeters);
        int OffsetY = (int) Math.floor(Pos.OffsetY / TileSideInMeters);
        Pos.AbsTileX = Pos.AbsTileX + OffsetX;
        Pos.AbsTileY = Pos.AbsTileY + OffsetY;
        Pos.OffsetX = Pos.OffsetX -  OffsetX * TileSideInMeters;
        Pos.OffsetY = Pos.OffsetY -  OffsetY * TileSideInMeters;

        return Pos;
    }

    public TileMapPosition RecanonicalizePosition(TileMapPosition Pos)
    {
        return RecanonicalizeCoord(Pos);
    }

    TileChunkPosition GetChunkPositionFor(int AbsTileX, int AbsTileY)
    {
        TileChunkPosition Result = new TileChunkPosition();

        Result.TileChunkX = AbsTileX >> ChunkShift;
        Result.TileChunkY = AbsTileY >> ChunkShift;
        Result.RelTileX = AbsTileX & ChunkMask;
        Result.RelTileY = AbsTileY & ChunkMask;

        return(Result);
    }

    int GetTileValue(int AbsTileX, int AbsTileY)
    {
        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
        TileChunk tileChunk = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);
        int TileChunkValue = GetTileValue(tileChunk, ChunkPos.RelTileX, ChunkPos.RelTileY);

        return(TileChunkValue);
    }

    public void SetTileValueUnchecked(TileChunk tileChunk, int TileX, int TileY, byte TileValue)
    {
        tileChunk.setTile(TileY * ChunkDim + TileX, TileValue);
    }

    public void SetTileValue(int AbsTileX, int AbsTileY, byte TileValue)
    {
        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
        TileChunk tileChunk = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);

        if (tileChunk.getTiles() == null) {
            int tileCount = ChunkDim * ChunkDim;
            ByteBuffer tiles = GameMemory.allocateTiles(tileCount);
            for (int tileIndex = 0; tileIndex < tileCount; ++tileIndex) {
                tiles.put(tileIndex, (byte)1);
            }
            tileChunk.setTiles(tiles);

        }
        SetTileValue(tileChunk, ChunkPos.RelTileX, ChunkPos.RelTileY, TileValue);
    }

    public void SetTileValue(TileChunk tileChunk, int TestTileX, int TestTileY, byte TileValue)
    {
        if(tileChunk != null && tileChunk.getTiles() != null && tileChunk.getTiles().limit() > 0)
        {
            SetTileValueUnchecked(tileChunk, TestTileX, TestTileY, TileValue);
        }
    }

    public TileMapDifference subtract(TileMapPosition A, TileMapPosition B)
    {
        TileMapDifference Result = new TileMapDifference();

        float dTileX = (float) A.AbsTileX - (float) B.AbsTileX;
        float dTileY = (float) A.AbsTileY - (float) B.AbsTileY;

        Result.dX = TileMap.TileSideInMeters * dTileX + (A.OffsetX - B.OffsetX);
        Result.dY = TileMap.TileSideInMeters * dTileY + (A.OffsetY - B.OffsetY);

        return(Result);
    }



    public static class TileMapPosition {
        public float OffsetX = 5.0f;
        public float OffsetY = 5.0f;

        public int AbsTileX = 1;
        public int AbsTileY = 3;
    }

    public static class TileChunkPosition {
        public int TileChunkX;
        public int TileChunkY;
        public int RelTileX;
        public int RelTileY;
    }

    public static class TileMapDifference {
        public float dX;
        public float dY;
    }
}

