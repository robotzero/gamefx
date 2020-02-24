package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class World {
    public static final int TileChunkCountX = 128;
    public static final int TileChunkCountY = 128;
    public static final int ChunkShift = BigInteger.valueOf(4).intValueExact();
    public static final int ChunkMask = BigInteger.valueOf((1 << ChunkShift) - 1).intValueExact();
    public static final int ChunkDim = BigInteger.valueOf(1 << ChunkShift).intValueExact();
    public static final int TileSideInPixels = 60;
    public static final int LowerLeftX = - (TileSideInPixels / 2);
    public static final int LowerLeftY = DisplayManager.HEIGHT;
    public static final float TileSideInMeters = 1.4f;
    public static final float MetersToPixels = TileSideInPixels / TileSideInMeters;
    public static float ScreenCenterX = 0.5f * DisplayManager.WIDTH;
    public static float ScreenCenterY = 0.5f * DisplayManager.HEIGHT;
    private final WorldGenerator worldGenerator;

    private Map<Long, TileChunk> tileChunkHash = new LinkedHashMap<>(4096);

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public World(WorldGenerator worldGenerator) {
        this.worldGenerator = worldGenerator;
    }

    public void renderWorld() {
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
                long Column = Camera.position.AbsTileX + RelColumn;
                long Row = Camera.position.AbsTileY + RelRow;
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

    private TileChunk GetTileChunk(long TileChunkX, long TileChunkY) {
        long HashValue = 19*TileChunkX + 7*TileChunkY;
        long HashSlot = HashValue & (tileChunkHash.size()) - 1;

        TileChunk tileChunk = tileChunkHash.computeIfAbsent(HashSlot, (v) -> {
            TileChunk t = new TileChunk();
            t.setTileChunkX(TileChunkX);
            t.setTileChunkY(TileChunkY);
            final var tiles = GameMemory.allocateTiles(ChunkDim * ChunkDim, HashSlot);
            t.setTiles(tiles);
            return t;
        });
        return tileChunk;
    }

    int GetTileValueUnchecked(TileChunk tileChunk, long TileX, long TileY)
    {
        return tileChunk.getTiles().get((int) (TileY * ChunkDim + TileX));
    }

    int GetTileValue(TileChunk TileChunk, long TestTileX, long TestTileY)
    {
        int TileChunkValue = 0;

        if(TileChunk != null && TileChunk.getTiles() != null && TileChunk.getTiles().limit() > 0) {
            TileChunkValue = GetTileValueUnchecked(TileChunk, TestTileX, TestTileY);
        }

        return(TileChunkValue);
    }

//    public boolean IsTileValueEmpty(int TileValue)
//    {
//        boolean Empty;
//
//        Empty = ((TileValue == 1) ||
//                (TileValue == 3) ||
//                (TileValue == 4));
//
//        return(Empty);
//    }

//    public boolean IsTileMapPointEmpty(WorldPosition CanPos)
//    {
//        boolean Empty;
//
//        int TileChunkValue = GetTileValue(CanPos.AbsTileX, CanPos.AbsTileY);
//        return IsTileValueEmpty(TileChunkValue);
//    }
//
//    public WorldPosition CenteredTilePoint(int AbsTileX, int AbsTileY)
//    {
//        WorldPosition Result = new WorldPosition();
//
//        Result.AbsTileX = AbsTileX;
//        Result.AbsTileY = AbsTileY;
//
//        return(Result);
//    }

    WorldPosition RecanonicalizeCoord(WorldPosition Pos)
    {
        int OffsetX = (int) Math.floor(Pos.Offset.x() / TileSideInMeters);
        int OffsetY = (int) Math.floor(Pos.Offset.y() / TileSideInMeters);
        Pos.AbsTileX = Pos.AbsTileX + OffsetX;
        Pos.AbsTileY = Pos.AbsTileY + OffsetY;
        Pos.Offset.x = Pos.Offset.x() -  OffsetX * TileSideInMeters;
        Pos.Offset.y = Pos.Offset.y() -  OffsetY * TileSideInMeters;

        return Pos;
    }

//    public WorldPosition RecanonicalizePosition(WorldPosition Pos)
//    {
//        return RecanonicalizeCoord(Pos);
//    }

    TileChunkPosition GetChunkPositionFor(long AbsTileX, long AbsTileY)
    {
        TileChunkPosition Result = new TileChunkPosition();

        Result.TileChunkX = AbsTileX >> ChunkShift;
        Result.TileChunkY = AbsTileY >> ChunkShift;
        Result.RelTileX = AbsTileX & ChunkMask;
        Result.RelTileY = AbsTileY & ChunkMask;

        return(Result);
    }

    public int GetTileValue(long AbsTileX, long AbsTileY)
    {
        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
        TileChunk tileChunk = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);
        int TileChunkValue = GetTileValue(tileChunk, ChunkPos.RelTileX, ChunkPos.RelTileY);

        return(TileChunkValue);
    }

//    public int GetTileValue(WorldPosition Pos)
//    {
//        int TileChunkValue = GetTileValue(Pos.AbsTileX, Pos.AbsTileY);
//
//        return(TileChunkValue);
//    }
//
//    public void SetTileValueUnchecked(TileChunk tileChunk, long TileX, long TileY, byte TileValue)
//    {
//        tileChunk.setTile((int) (TileY * ChunkDim + TileX), TileValue);
//    }

//    public void SetTileValue(int AbsTileX, int AbsTileY, byte TileValue)
//    {
//        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
//        TileChunk tileChunk = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);
//
//        SetTileValue(tileChunk, ChunkPos.RelTileX, ChunkPos.RelTileY, TileValue);
//    }

//    public void SetTileValue(TileChunk tileChunk, long TestTileX, long TestTileY, byte TileValue)
//    {
//        if(tileChunk != null && tileChunk.getTiles() != null && tileChunk.getTiles().limit() > 0)
//        {
//            SetTileValueUnchecked(tileChunk, TestTileX, TestTileY, TileValue);
//        }
//    }

    public static WorldDifference subtract(WorldPosition A, WorldPosition B)
    {
        WorldDifference Result = new WorldDifference();
        Vector2f dTileXY = new Vector2f((float) A.AbsTileX - (float) B.AbsTileX, (float) A.AbsTileY - (float) B.AbsTileY);
        Result.dXY = dTileXY.mul(World.TileSideInMeters).add(new Vector2f(A.Offset).sub(B.Offset));

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

    public WorldPosition MapIntoTileSpace(WorldPosition BasePos, Vector2f Offset)
    {
        WorldPosition Result = new WorldPosition(BasePos);

        Result.Offset = new Vector2f(Result.Offset).add(Offset);
        RecanonicalizeCoord(Result);

        return(Result);
    }

    public int SignOf(int Value)
    {
        int Result = (Value >= 0) ? 1 : -1;
        return(Result);
    }

//    public WorldPosition Offset(WorldPosition P, Vector2f Offset)
//    {
//        P.Offset = P.Offset.add(Offset);
//        P = RecanonicalizePosition(P);
//
//        return(P);
//    }

    public static class WorldPosition {
        public WorldPosition() {}
        public WorldPosition(WorldPosition worldPosition) {
            this.Offset = worldPosition.Offset;
            this.AbsTileX = worldPosition.AbsTileX;
            this.AbsTileY = worldPosition.AbsTileY;
        }
        public Vector2f Offset = new Vector2f(0.0f, 0.0f);

        public long AbsTileX = 1;
        public long AbsTileY = 3;
    }

    public static class TileChunkPosition {
        public long TileChunkX;
        public long TileChunkY;
        public long RelTileX;
        public long RelTileY;
    }

    public static class WorldDifference {
        public Vector2f dXY;
    }
}

