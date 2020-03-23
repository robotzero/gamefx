package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.utils.Random;

public class WorldGenerator {
    public static final int tilesPerWidth = 5;
    public static final int tilesPerHeight = 3;
    public static final int screenBaseX = 0;
    public static final int screenBaseY = 0;
    public static int CameraTileX = WorldGenerator.screenBaseX * WorldGenerator.tilesPerWidth + 17/2;
    public static int CameraTileY = WorldGenerator.screenBaseY * WorldGenerator.tilesPerHeight + 9/2;
    public static int randomNumberIndex = 0;

    public static void renderWorld(EntityService entityService) {
        int screenX = screenBaseX;
        int screenY = screenBaseY;
        boolean doorLeft = false;
        boolean doorRight = false;
        boolean doorTop = false;
        boolean doorBottom = false;

        for (int screenIndex = 0;
             screenIndex < 1;
             ++screenIndex) {

            int randomChoice;
            randomChoice = Random.randomNumberTable[randomNumberIndex++] % 2;

            if (randomChoice == 1) {
                doorRight = true;
            } else {
                doorTop = true;
            }

            for (int tileY = 0;
                 tileY < tilesPerHeight;
                 ++tileY) {
                for (int tileX = 0;
                     tileX < tilesPerWidth;
                     ++tileX) {
                    int absTileX = screenX * tilesPerWidth + tileX;
                    int absTileY = screenY * tilesPerHeight + tileY;

                    int tileValue = 1;
                    if ((tileX == 0) && (!doorLeft || (tileY != (tilesPerHeight / 2)))) {
                        tileValue = 2;
                    }

                    if ((tileX == (tilesPerWidth - 1)) && (!doorRight || (tileY != (tilesPerHeight / 2)))) {
                        tileValue = 2;
                    }

                    if ((tileY == 0) && (!doorBottom || (tileX != (tilesPerWidth / 2)))) {
                        tileValue = 2;
                    }

                    if ((tileY == (tilesPerHeight - 1)) && (!doorTop || (tileX != (tilesPerWidth / 2)))) {
                        tileValue = 2;
                    }

                    //world.SetTileValue(BigInteger.valueOf(absTileX).intValueExact(), BigInteger.valueOf(absTileY).intValueExact(), tileValue);
                    if (tileValue == 2) {
                        entityService.AddWall(absTileX, absTileY);
                    }
                }
            }

            doorLeft = doorRight;
            doorBottom = doorTop;
            doorRight = false;
            doorTop = false;

            if (randomChoice == 2) {

            } else if (randomChoice == 1) {
                screenX += 1;
            } else {
                screenY += 1;
            }
        }

//        while(gameMemory.LowEntityCount < (ArrayCount(GameState->LowEntities) - 16))
//        {
//            uint32 Coordinate = 1024 + GameState->LowEntityCount;
//            AddWall(GameState, Coordinate, Coordinate, Coordinate);
//        }
    }
}
