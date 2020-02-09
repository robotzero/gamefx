package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.utils.Random;

public class WorldGenerator {
    private final int tilesPerWidth = 17;
    private final int tilesPerHeight = 9;


    public void renderWorld(TileMap tileMap) {
        int randomNumberIndex = 0;
        int screenBaseX = 0;
        int screenBaseY = 0;
        int screenX = screenBaseX;
        int screenY = screenBaseY;
        boolean doorLeft = false;
        boolean doorRight = false;
        boolean doorTop = false;
        boolean doorBottom = false;

        for (int screenIndex = 0;
             screenIndex < 100;
             ++screenIndex) {

            int randomChoice;
            randomChoice = Random.randomNumberTable[randomNumberIndex++] % 3;

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

                    byte tileValue = 1;
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

                    tileMap.SetTileValue(absTileX, absTileY, tileValue);
                }
            }

            doorLeft = doorRight;
            doorBottom = doorTop;
            doorRight = false;
            doorTop = false;

            if (randomChoice == 1) {
                screenX += 1;
            } else {
                screenY += 1;
            }
        }
    }
}
