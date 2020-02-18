package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import org.joml.Vector2f;
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

                Vector2f TileSide = new Vector2f(0.5f * TileSideInPixels, 0.5f * TileSideInPixels);
                Vector2f Cen =  new Vector2f(ScreenCenterX - MetersToPixels * Camera.position.Offset.x() + (RelColumn * TileSideInPixels),
                    ScreenCenterY + MetersToPixels * Camera.position.Offset.y() - (RelRow * TileSideInPixels));
                Vector2f Min = Cen.sub(TileSide.mul(0.9f));
                Vector2f Max = Cen.add(0.5f * TileSideInPixels, 0.5f * TileSideInPixels);

                tilePositions.put(new Vector3f(Min.x, Min.y, 0), color);
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

    public boolean IsTileValueEmpty(int TileValue)
    {
        boolean Empty;

        Empty = ((TileValue == 1) ||
                (TileValue == 3) ||
                (TileValue == 4));

        return(Empty);
    }

    public boolean IsTileMapPointEmpty(TileMapPosition CanPos)
    {
        boolean Empty;

        int TileChunkValue = GetTileValue(CanPos.AbsTileX, CanPos.AbsTileY);
        return IsTileValueEmpty(TileChunkValue);
    }

    public TileMapPosition CenteredTilePoint(int AbsTileX, int AbsTileY)
    {
        TileMapPosition Result = new TileMapPosition();

        Result.AbsTileX = AbsTileX;
        Result.AbsTileY = AbsTileY;

        return(Result);
    }

    TileMapPosition RecanonicalizeCoord(TileMapPosition Pos)
    {
        int OffsetX = (int) Math.floor(Pos.Offset.x() / TileSideInMeters);
        int OffsetY = (int) Math.floor(Pos.Offset.y() / TileSideInMeters);
        Pos.AbsTileX = Pos.AbsTileX + OffsetX;
        Pos.AbsTileY = Pos.AbsTileY + OffsetY;
        Pos.Offset.x = Pos.Offset.x() -  OffsetX * TileSideInMeters;
        Pos.Offset.y = Pos.Offset.y() -  OffsetY * TileSideInMeters;

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

    public int GetTileValue(int AbsTileX, int AbsTileY)
    {
        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
        TileChunk tileChunk = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);
        int TileChunkValue = GetTileValue(tileChunk, ChunkPos.RelTileX, ChunkPos.RelTileY);

        return(TileChunkValue);
    }

    public int GetTileValue(TileMapPosition Pos)
    {
        int TileChunkValue = GetTileValue(Pos.AbsTileX, Pos.AbsTileY);

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

    public static TileMapDifference subtract(TileMapPosition A, TileMapPosition B)
    {
        TileMapDifference Result = new TileMapDifference();
        Vector2f dTileXY = new Vector2f((float) A.AbsTileX - (float) B.AbsTileX, (float) A.AbsTileY - (float) B.AbsTileY);
        Result.dXY = dTileXY.mul(TileMap.TileSideInMeters).add(new Vector2f(A.Offset).sub(B.Offset));

        return(Result);
    }

    public float[] TestWall(float WallX, float RelX, float RelY, float PlayerDeltaX, float PlayerDeltaY,
                            float[] tMin, float MinY, float MaxY)
    {
        float Hit = 0;
        float tMinTemp = tMin[0];
        float tEpsilon = 0.001f;
        if(PlayerDeltaX != 0.0f)
        {
            float tResult = (WallX - RelX) / PlayerDeltaX;
            float Y = RelY + tResult * PlayerDeltaY;
            if((tResult >= 0.0f) && (tMinTemp > tResult)) {
                if((Y >= MinY) && (Y <= MaxY)) {
                    tMinTemp = Math.max(0.0f, tResult - tEpsilon);
                    Hit = 1;
                }
            }
        }

        return new float[]{tMinTemp, Hit};
    }

    public TileMapPosition MapIntoTileSpace(TileMapPosition BasePos, Vector2f Offset)
    {
        TileMapPosition Result = new TileMapPosition(BasePos);

        Result.Offset = new Vector2f(Result.Offset).add(Offset);
        RecanonicalizeCoord(Result);

        return(Result);
    }

    public int SignOf(int Value)
    {
        int Result = (Value >= 0) ? 1 : -1;
        return(Result);
    }

    public TileMapPosition Offset(TileMapPosition P, Vector2f Offset)
    {
        P.Offset = P.Offset.add(Offset);
        P = RecanonicalizePosition(P);

        return(P);
    }

    public static class TileMapPosition {
        public TileMapPosition() {}
        public TileMapPosition(TileMapPosition tileMapPosition) {
            this.Offset = tileMapPosition.Offset;
            this.AbsTileX = tileMapPosition.AbsTileX;
            this.AbsTileY = tileMapPosition.AbsTileY;
        }
        public Vector2f Offset = new Vector2f(0.0f, 0.0f);

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
        public Vector2f dXY;
    }
}

