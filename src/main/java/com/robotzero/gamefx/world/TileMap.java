package com.robotzero.gamefx.world;

import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

public class TileMap {
    private final static int TILEMAP_COUNT_X = 17;
    private final static int TILEMAP_COUNT_Y = 9;
    private final int WorldTileMapCountX = 2;
    private final int WorldTileMapCountY = 2;
    private final int UpperLeftX = 0;
    private final int UpperLeftY = 0;
    private final int TileWidth = 60;
    private final int TileHeight = 60;
    private final int[] tiles00 =
    {
        1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,
        1, 1, 0, 0,  0, 1, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1,
        1, 1, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 1, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 1,
        0, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 0, 0, 0, 0,
        1, 1, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  0, 1, 0, 0, 1,
        1, 0, 0, 0,  0, 1, 0, 0,  1, 0, 0, 0,  1, 0, 0, 0, 1,
        1, 1, 1, 1,  1, 0, 0, 0,  0, 0, 0, 0,  0, 1, 0, 0, 1,
        1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,
    };

    private final int[] tiles01 =
    {
        1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 0,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1
    };

    private final int[] tiles10 =
    {
        1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1
    };
    private final int[] tiles11 =
    {
        1, 1, 1, 1,  1, 1, 1, 1,  0, 1, 1, 1,  1, 1, 1, 1, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0,  0, 0, 0, 0, 1,
        1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1,  1, 1, 1, 1, 1
    };

    private IntBuffer btiles00 = MemoryStack.stackMallocInt(tiles00.length);
    private IntBuffer btiles01 = MemoryStack.stackMallocInt(tiles01.length);
    private IntBuffer btiles10 = MemoryStack.stackMallocInt(tiles10.length);
    private IntBuffer btiles11 = MemoryStack.stackMallocInt(tiles11.length);

    private final IntBuffer[] tileMaps = { btiles00, btiles01, btiles10, btiles11 };

    public static final int tileWidth = 60;
    public static final int tileHeight = 60;

    private Map<Vector3f, Float> tilePositions = new HashMap<>();

    public TileMap() {
        this.generateTilePositions();
        this.btiles00 = btiles00.put(tiles00);
        this.btiles01 = btiles01.put(tiles01);
        this.btiles10 = btiles10.put(tiles10);
        this.btiles11 = btiles11.put(tiles11);

    }

    private void generateTilePositions() {
        Arrays.stream(tiles00).mapToObj(entry -> {
            float color = 0.5f;
            if (entry == 1) {
                color = 1.0f;
            }
            tilePositions.put(new Vector3f(10 * tileWidth, 10 * tileHeight, 0), color);
            return new Vector3f(10 * tileWidth, 10 * tileHeight, 0);
        }).collect(Collectors.toList());
//        for(int row = 0;
//            row < 9;
//            ++row)
//        {
//            for(int column = 0;
//                column < 17;
//                ++column) {
//                int tileID = tiles00[row][column];
//                float color = 0.5f;
//                if (tileID == 1) {
//                    color = 1.0f;
//                }
//                tilePositions.put(new Vector3f(column * tileWidth, row * tileHeight, 0), color);
//            }
//        }
    }

    public Map<Vector3f, Float> getTilePositions() {
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
        return tileMap.get(TileY*TILEMAP_COUNT_X + TileX);
    }

    boolean isTileMapPointEmpty(IntBuffer tileMap, float TestX, float TestY)
    {
        boolean Empty = false;

        double PlayerTileX = Math.floor((TestX - UpperLeftX) / TileWidth);
        double PlayerTileY = Math.floor((TestY - UpperLeftY) / TileHeight);

        if((PlayerTileX >= 0) && (PlayerTileX < TILEMAP_COUNT_X) &&
                (PlayerTileY >= 0) && (PlayerTileY < TILEMAP_COUNT_Y))
        {
            int TileMapValue = GetTileValueUnchecked(tileMap, (int)PlayerTileX, (int)PlayerTileY);
            Empty = (TileMapValue == 0);
        }

        return(Empty);
    }

    boolean IsWorldPointEmpty(int TileMapX, int TileMapY, double TestX, double TestY)
    {
        boolean Empty = false;

        IntBuffer tileMap = GetTileMap(TileMapX, TileMapY);
        if(tileMap != null && tileMap.array().length > 0)
        {
            double PlayerTileX = Math.floor((TestX - UpperLeftX) / TileWidth);
            double PlayerTileY = Math.floor((TestY - UpperLeftY) / TileHeight);

            if((PlayerTileX >= 0) && (PlayerTileX < TILEMAP_COUNT_X) &&
                    (PlayerTileY >= 0) && (PlayerTileY < TILEMAP_COUNT_Y))
            {
                int TileMapValue = GetTileValueUnchecked(tileMap, (int)PlayerTileX, (int)PlayerTileY);
                Empty = (TileMapValue == 0);
            }
        }

        return(Empty);
    }


}
