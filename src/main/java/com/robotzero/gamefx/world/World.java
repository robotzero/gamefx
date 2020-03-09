package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.entity.EntityService;
import org.joml.Vector2f;

import java.util.LinkedHashMap;
import java.util.Map;

public class World {
    public static final int TILES_PER_CHUNK = 16;
    public static final int TileSideInPixels = 60;
    public static final float TileSideInMeters = 1.4f;
    private static final float ChunkSideInMeters = TILES_PER_CHUNK * TileSideInMeters;
    public static final float MetersToPixels = TileSideInPixels / TileSideInMeters;
    public static float ScreenCenterX = 0.5f * DisplayManager.WIDTH;
    public static float ScreenCenterY = 0.5f * DisplayManager.HEIGHT;
    public static Integer firstFree = null;
    private static final int tileChunkHashSize = 4096;

    private Map<Long, WorldChunk> tileChunkHash = new LinkedHashMap<>(tileChunkHashSize);

    public static void renderWorld(EntityService entityService) {
        WorldGenerator.renderWorld(entityService);
    }

    public WorldChunk GetWorldChunk(int TileChunkX, int TileChunkY, boolean initialize) {
        long HashValue = 19 * TileChunkX + 7 * TileChunkY;
        long HashSlot = HashValue & (tileChunkHashSize - 1);

        if (initialize) {
            WorldChunk worldChunk = tileChunkHash.computeIfAbsent(HashSlot, (v) -> {
                WorldChunk t = new WorldChunk();
                t.setTileChunkX(TileChunkX);
                t.setTileChunkY(TileChunkY);
                return t;
            });
            return worldChunk;
        }

        return tileChunkHash.get(HashSlot);
    }

    public boolean IsCanonical(float TileRel)
    {
        // TODO(casey): Fix floating point math so this can be exact?
        boolean Result = ((TileRel >= -0.5f * ChunkSideInMeters) &&
                (TileRel <= 0.5f * ChunkSideInMeters));

        return(Result);
    }

    public boolean IsCanonical(Vector2f Offset)
    {
        boolean Result = (IsCanonical(Offset.x()) && IsCanonical(Offset.y()));

        return(Result);
    }

    public boolean AreInSameChunk(WorldPosition A, WorldPosition B)
    {
        boolean Result = ((A.ChunkX == B.ChunkX) &&
                (A.ChunkY == B.ChunkY));

        return(Result);
    }

    public WorldPosition CenteredChunkPoint(int ChunkX, int ChunkY)
    {
        WorldPosition Result = new WorldPosition();

        Result.ChunkX = ChunkX;
        Result.ChunkY = ChunkY;

        return(Result);
    }

    WorldPosition RecanonicalizeCoord(WorldPosition Pos)
    {
        int OffsetX = (int) Math.floor(Pos.Offset.x() / ChunkSideInMeters);
        int OffsetY = (int) Math.floor(Pos.Offset.y() / ChunkSideInMeters);
        Pos.ChunkX = Pos.ChunkX + OffsetX;
        Pos.ChunkY = Pos.ChunkY + OffsetY;
        Pos.Offset.x = Pos.Offset.x() -  OffsetX * ChunkSideInMeters;
        Pos.Offset.y = Pos.Offset.y() -  OffsetY * ChunkSideInMeters;

        return Pos;
    }

    public static WorldDifference subtract(WorldPosition A, WorldPosition B)
    {
        WorldDifference Result = new WorldDifference();
        Vector2f dTileXY = new Vector2f((float) A.ChunkX - (float) B.ChunkX, (float) A.ChunkY - (float) B.ChunkY);
        Result.dXY = dTileXY.mul(World.ChunkSideInMeters).add(new Vector2f(A.Offset).sub(B.Offset));

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

    public WorldPosition MapIntoChunkSpace(WorldPosition BasePos, Vector2f Offset)
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

    public static class WorldPosition {
        public WorldPosition() {}
        public WorldPosition(WorldPosition worldPosition) {
            this.Offset = worldPosition.Offset;
            this.ChunkX = worldPosition.ChunkX;
            this.ChunkY = worldPosition.ChunkY;
        }
        public Vector2f Offset = new Vector2f(0.0f, 0.0f);

        public int ChunkX = 1;
        public int ChunkY = 3;
    }

    public static class WorldDifference {
        public Vector2f dXY;
    }
}

