package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.Player;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Map;

public class TileMap {
    private final static int TILEMAP_COUNT_X = 17;
    private final static int TILEMAP_COUNT_Y = 9;
    private final int WorldTileMapCountX = 2;
    private final int WorldTileMapCountY = 2;
    public static final int TileSideInPixels = 60;
    public static final int UpperLeftX = - -(TileSideInPixels / 2);
    public static final int UpperLeftY = 0;
    public static final float TileSideInMeters = 1.4f;
    public static final float MetersToPixels = TileSideInPixels / TileSideInMeters;
    private final int[][] tiles00 =
            {
                    {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1},
                    {1, 0, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1},
                    {1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 1, 0, 1},
                    {1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 1},
                    {1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 0},
                    {1, 1, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 1, 0, 0, 1},
                    {1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0, 1},
                    {1, 0, 1, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1},
                    {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
            };

    private final int[][] tiles01 =
    {
            {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 0},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1}
    };

    private final int[][] tiles10 =
    {
            {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1}
    };
    private final int[][] tiles11 =
    {
            {1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1},
            {1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1}
    };

    private IntBuffer btiles00;
    private IntBuffer btiles01;
    private IntBuffer btiles10;
    private IntBuffer btiles11;

    private IntBuffer[] tileMaps = {null, null, null, null};

    public static final int tileWidth = 60;
    public static final int tileHeight = 60;

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public TileMap() {
        final var arr1 = convertTo1d(tiles00);
        final var arr2 = convertTo1d(tiles01);
        final var arr3 = convertTo1d(tiles10);
        final var arr4 = convertTo1d(tiles11);
        btiles00 = MemoryStack.stackMallocInt(arr1.length);
        btiles01 = MemoryStack.stackMallocInt(arr2.length);
        btiles10 = MemoryStack.stackMallocInt(arr3.length);
        btiles11 = MemoryStack.stackMallocInt(arr4.length);
        this.btiles00 = btiles00.put(arr1);
        this.btiles01 = btiles01.put(arr2);
        this.btiles10 = btiles10.put(arr3);
        this.btiles11 = btiles11.put(arr4);
        tileMaps[0] = btiles00;
        tileMaps[1] = btiles10;
        tileMaps[2] = btiles01;
        tileMaps[3] = btiles11;
    }

    private int[] convertTo1d(int[][] tiles) {
        int[] array = new int[17 * 9];
        for (int i = 0; i < 9; i++)
        {
            for (int j = 0; j < 17; j++)
            {
                int k = i * 17 + j;
                array[k] = tiles[i][j];
            }
        }
        return array;
    }

    private void generateTilePositions() {
        IntBuffer tileMap =  this.GetTileMap(Player.positionc.TileMapX, Player.positionc.TileMapY);
        for(int row = 0;
            row < 9;
            ++row)
        {
            for(int column = 0;
                column < 17;
                ++column) {
                int tileID = tileMap.get(row * TILEMAP_COUNT_X + column);
                float color = 0.5f;
                if (tileID == 1) {
                    color = 1.0f;
                }

                if((column == Player.positionc.TileX) &&
                        (row == Player.positionc.TileY))
                {
                    color = 0.0f;
                }

                tilePositions.put(new Vector3f(UpperLeftX + (column * TileSideInPixels), UpperLeftY + (row * TileSideInPixels), 0), color);
            }
        }
    }

    public Map<Vector3f, Float> getTilePositions() {
        this.tilePositions.clear();
        this.generateTilePositions();
        return this.tilePositions;
    }

    private IntBuffer GetTileMap(int TileMapX, int TileMapY)
    {
        IntBuffer tileMap = null;

        if((TileMapX >= 0) && (TileMapX < WorldTileMapCountX) &&
                (TileMapY >= 0) && (TileMapY < WorldTileMapCountY))
        {
            tileMap = tileMaps[TileMapY*WorldTileMapCountX + TileMapX];
        }

        return tileMap;
    }

    int GetTileValueUnchecked(IntBuffer tileMap, int TileX, int TileY)
    {
//        Assert(TileMap);
//        Assert((TileX >= 0) && (TileX < World->CountX) &&
//                (TileY >= 0) && (TileY < World->CountY));

        return tileMap.get(TileY*TILEMAP_COUNT_X + TileX);
    }


    boolean IsTileMapPointEmpty(IntBuffer tileMap, int TestTileX, int TestTileY)
    {
        boolean Empty = false;

        if(tileMap != null && tileMap.limit() > 0)
        {
            if((TestTileX >= 0) && (TestTileX < TILEMAP_COUNT_X) &&
                    (TestTileY >= 0) && (TestTileY < TILEMAP_COUNT_Y))
            {
                int TileMapValue = GetTileValueUnchecked(tileMap, TestTileX, TestTileY);
                Empty = (TileMapValue == 0);
            }
        }

        return(Empty);
    }

    public boolean IsWorldPointEmpty(CannonicalPosition CanPos)
    {
        boolean Empty = false;

        IntBuffer tileMap = GetTileMap(CanPos.TileMapX, CanPos.TileMapY);
        Empty = IsTileMapPointEmpty(tileMap, CanPos.TileX, CanPos.TileY);

        return(Empty);
    }

    CannonicalPosition RecanonicalizeCoord(CannonicalPosition Pos)
    {
        double OffsetX = Math.floor(Pos.TileRelX / TileSideInMeters);
        double OffsetY = Math.floor(Pos.TileRelY / TileSideInMeters);
        Pos.TileX += OffsetX;
        Pos.TileY += OffsetY;
        Pos.TileRelX -= OffsetX * TileSideInMeters;
        Pos.TileRelY -= OffsetY * TileSideInMeters;

        if(Pos.TileX < 0)
        {
            Pos.TileX = TILEMAP_COUNT_X + Pos.TileX;
            Pos.TileMapX = Pos.TileMapX - 1;
        }

        if(Pos.TileX >= TILEMAP_COUNT_X)
        {
            Pos.TileX = Pos.TileX - TILEMAP_COUNT_X;
            Pos.TileMapX = Pos.TileMapX + 1;
        }

        if(Pos.TileY < 0)
        {
            Pos.TileY = TILEMAP_COUNT_Y + Pos.TileY;
            Pos.TileMapY = Pos.TileMapY - 1;
        }

        if(Pos.TileY >= TILEMAP_COUNT_Y)
        {
            Pos.TileY = Pos.TileY - TILEMAP_COUNT_Y;
            Pos.TileMapY = Pos.TileMapY + 1;
        }

        return Pos;
    }

    public CannonicalPosition RecanonicalizePosition(CannonicalPosition Pos)
    {
        return RecanonicalizeCoord(Pos);
    }


    public static class CannonicalPosition {
        public int TileMapX = 0;
        public int TileMapY = 0;

        public int TileX = 3;
        public int TileY = 3;

        public float TileRelX = 5.0f;
        public float TileRelY = 5.0f;
    }
}

