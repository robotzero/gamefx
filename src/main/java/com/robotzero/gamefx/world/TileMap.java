package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Player;
import org.joml.Vector3f;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TileMap {
    public final static int TILE_MAP_COUNT_X = 256;
    public final static int TILE_MAP_COUNT_Y = 256;
    public static final int TileChunkCountX = 1;
    public static final int TileChunkCountY = 1;
    private final int ChunkShift = 8;
    private final int ChunkMask = (1 << ChunkShift) - 1;
    private final int ChunkDim = 256;
    public static final int TileSideInPixels = 60;
    public static final int LowerLeftX = - (TileSideInPixels / 2);
    public static final int LowerLeftY = 0;
    public static final float TileSideInMeters = 1.4f;
    public static final float MetersToPixels = TileSideInPixels / TileSideInMeters;
    public static float CenterX = 0.5f * DisplayManager.WIDTH;
    public static float CenterY = 0.5f * DisplayManager.HEIGHT;
    private final byte TempTiles[][] =
    {
        {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1},
        {1, 1, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 1, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 1, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 1, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 1, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 1, 1, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
        {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
        {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1},
    };

    private List<TileChunk> tileChunks = new ArrayList<>();

    public static final int tileWidth = 60;
    public static final int tileHeight = 60;
    private TileChunk tileChunk;

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public TileMap() {
//        final var arr1 = convertTo1d(tiles00);
//        final var arr2 = convertTo1d(tiles01);
//        final var arr3 = convertTo1d(tiles10);
//        final var arr4 = convertTo1d(tiles11);
//        btiles00 = MemoryStack.stackMallocInt(arr1.length);
//        btiles01 = MemoryStack.stackMallocInt(arr2.length);
//        btiles10 = MemoryStack.stackMallocInt(arr3.length);
//        btiles11 = MemoryStack.stackMallocInt(arr4.length);
//        this.btiles00 = btiles00.put(arr1);
//        this.btiles01 = btiles01.put(arr2);
//        this.btiles10 = btiles10.put(arr3);
//        this.btiles11 = btiles11.put(arr4);
        final var t = convertTo1d(TempTiles);
        tileChunk = new TileChunk(t);
        tileChunks.add(tileChunk);

//        tileChunks = MemoryStack.stackMallocInt(t.length);
//        tileChunks.put(t);
//        tileMaps[0] = btiles00;
//        tileMaps[1] = btiles10;
//        tileMaps[2] = btiles01;
//        tileMaps[3] = btiles11;
    }

    private byte[] convertTo1d(byte[][] tiles) {
        byte[] array = new byte[256 * 256];
        for (int i = 0; i < 256; i++)
        {
            for (int j = 0; j < 256; j++)
            {
                int k = i * 256 + j;
                try {
                    array[k] = tiles[i][j];
                } catch (ArrayIndexOutOfBoundsException e) {
                    array[k] = 0;
                }
            }
        }
        return array;
    }

    private void generateTilePositions() {
//        IntBuffer tileMap =  this.GetTileChunk(Player.positionc.TileMapX, Player.positionc.TileMapY);
        for(int RelRow = -10;
            RelRow < 10;
            ++RelRow)
        {
            for(int RelColumn = -20;
                RelColumn < 20;
                ++RelColumn) {
                int Column = Player.positionc.AbsTileX + RelColumn;
                int Row = Player.positionc.AbsTileY + RelRow;
                int tileID = GetTileValue(Column, Row);

                float color = 0.5f;
                if (tileID == 1) {
                    color = 1.0f;
                }

                if((Column == Player.positionc.AbsTileX) &&
                        (Row == Player.positionc.AbsTileY))
                {
                    color = 0.0f;
                }

                //tilePositions.put(new Vector3f(UpperLeftX + (column * TileSideInPixels), UpperLeftY + (row * TileSideInPixels), 0), color);
                tilePositions.put(new Vector3f(CenterX + (RelColumn * TileSideInPixels), CenterY - (RelRow * TileSideInPixels), 0), color);
            }
        }
    }

    public Map<Vector3f, Float> getTilePositions() {
        this.tilePositions.clear();
        this.generateTilePositions();
        return this.tilePositions;
    }

    private ByteBuffer GetTileChunk(int TileChunkX, int TileChunkY)
    {
        ByteBuffer tileMap = null;

        if((TileChunkX >= 0) && (TileChunkX < TileChunkCountX) &&
                (TileChunkY >= 0) && (TileChunkY < TileChunkCountY))
        {
            tileMap = tileChunks.get(TileChunkY * TileChunkCountX + TileChunkX).getTiles();
        }

        return tileMap;
    }

    int GetTileValueUnchecked(ByteBuffer tileChunk, int TileX, int TileY)
    {
//        Assert(TileMap);
//        Assert((TileX >= 0) && (TileX < World->CountX) &&
//                (TileY >= 0) && (TileY < World->CountY));

        return tileChunk.get(TileY * ChunkDim + TileX);
    }

    int GetTileValue(ByteBuffer TileChunk, int TestTileX, int TestTileY)
    {
        int TileChunkValue = 0;

        if(TileChunk != null && TileChunk.limit() > 0)
        {
            TileChunkValue = GetTileValueUnchecked(TileChunk, TestTileX, TestTileY);
        }

        return(TileChunkValue);
    }

    public boolean IsWorldPointEmpty(WorldPosition CanPos)
    {
        boolean Empty = false;

        int TileChunkValue = GetTileValue(CanPos.AbsTileX, CanPos.AbsTileY);
        Empty = (TileChunkValue == 0);

        return(Empty);
    }

    WorldPosition RecanonicalizeCoord(WorldPosition Pos)
    {
        double OffsetX = Math.floor(Pos.TileRelX / TileSideInMeters);
        double OffsetY = Math.floor(Pos.TileRelY / TileSideInMeters);
        Pos.AbsTileX += OffsetX;
        Pos.AbsTileY += OffsetY;
        Pos.TileRelX -= OffsetX * TileSideInMeters;
        Pos.TileRelY -= OffsetY * TileSideInMeters;

//        if(Pos.TileX < 0)
//        {
//            Pos.TileX = TILEMAP_COUNT_X + Pos.TileX;
//            Pos.TileMapX = Pos.TileMapX - 1;
//        }
//
//        if(Pos.TileX >= TILEMAP_COUNT_X)
//        {
//            Pos.TileX = Pos.TileX - TILEMAP_COUNT_X;
//            Pos.TileMapX = Pos.TileMapX + 1;
//        }
//
//        if(Pos.TileY < 0)
//        {
//            Pos.TileY = TILEMAP_COUNT_Y + Pos.TileY;
//            Pos.TileMapY = Pos.TileMapY - 1;
//        }
//
//        if(Pos.TileY >= TILEMAP_COUNT_Y)
//        {
//            Pos.TileY = Pos.TileY - TILEMAP_COUNT_Y;
//            Pos.TileMapY = Pos.TileMapY + 1;
//        }

        return Pos;
    }

    public WorldPosition RecanonicalizePosition(WorldPosition Pos)
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
        boolean Empty = false;

        TileChunkPosition ChunkPos = GetChunkPositionFor(AbsTileX, AbsTileY);
        ByteBuffer TileMap = GetTileChunk(ChunkPos.TileChunkX, ChunkPos.TileChunkY);
        int TileChunkValue = GetTileValue(TileMap, ChunkPos.RelTileX, ChunkPos.RelTileY);

        return(TileChunkValue);
    }


    public static class WorldPosition {
        public float TileRelX = 5.0f;
        public float TileRelY = 5.0f;

        public int AbsTileX = 3;
        public int AbsTileY = 3;
    }

    public static class TileChunkPosition {
        public int TileChunkX;
        public int TileChunkY;
        public int RelTileX;
        public int RelTileY;
    }
}

