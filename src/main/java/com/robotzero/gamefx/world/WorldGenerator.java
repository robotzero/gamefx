package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.EntityService;
import com.robotzero.gamefx.renderengine.utils.Random;

import java.math.BigInteger;

public class WorldGenerator {
    public static final int tilesPerWidth = 17;
    public static final int tilesPerHeight = 9;
    public static final long screenBaseX = 0;
    public static final long screenBaseY = 0;
    private final EntityService entityService;

    public WorldGenerator(EntityService entityService) {
        this.entityService = entityService;
    }

    public void renderWorld(TileMap tileMap) {
        // TODO(casey): Waiting for full sparseness
        int randomNumberIndex = 0;
        long screenX = screenBaseX;
        long screenY = screenBaseY;
        boolean doorLeft = false;
        boolean doorRight = false;
        boolean doorTop = false;
        boolean doorBottom = false;

        for (int screenIndex = 0;
             screenIndex < 2;
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
                    long absTileX = screenX * tilesPerWidth + tileX;
                    long absTileY = screenY * tilesPerHeight + tileY;

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

                    tileMap.SetTileValue(BigInteger.valueOf(absTileX).intValueExact(), BigInteger.valueOf(absTileY).intValueExact(), tileValue);
                    if (tileValue == 2) {
                        entityService.AddWall(BigInteger.valueOf(absTileX).intValueExact(), BigInteger.valueOf(absTileY).intValueExact());
                    }
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
