package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.utils.Random;

public class WorldGenerator {
    public static final int tilesPerWidth = 17;
    public static final int tilesPerHeight = 9;
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
        RandomSeries Series = RandomSeed(1234);

        for (int screenIndex = 0;
             screenIndex < 1;
             ++screenIndex) {

            int DoorDirection = RandomChoice(Series, doorLeft || doorRight ? 2 : 4);

            if (DoorDirection == 1) {
                doorRight = true;
            } else {
                doorTop = true;
            }

//            entityService.AddStandardRoom(
//                    screenX * tilesPerWidth + tilesPerWidth / 2,
//                    screenY * tilesPerHeight + tilesPerHeight / 2);

            for (int tileY = 0;
                 tileY < tilesPerHeight;
                 ++tileY) {
                for (int tileX = 0;
                     tileX < tilesPerWidth;
                     ++tileX) {
                    int absTileX = screenX * tilesPerWidth + tileX;
                    int absTileY = screenY * tilesPerHeight + tileY;

                    boolean shouldBeDoor = false;
                    if ((tileX == 0) && (!doorLeft || (tileY != (tilesPerHeight / 2)))) {
                        shouldBeDoor = true;
                    }

                    if ((tileX == (tilesPerWidth - 1)) && (!doorRight || (tileY != (tilesPerHeight / 2)))) {
                        shouldBeDoor = true;
                    }

                    if ((tileY == 0) && (!doorBottom || (tileX != (tilesPerWidth / 2)))) {
                        shouldBeDoor = true;
                    }

                    if ((tileY == (tilesPerHeight - 1)) && (!doorTop || (tileX != (tilesPerWidth / 2)))) {
                        shouldBeDoor = true;
                    }

                    if (shouldBeDoor) {
//                        if (screenIndex == 0) {
                            entityService.AddWall(absTileX, absTileY);
//                        }
                    }
                }
            }

            doorLeft = doorRight;
            doorBottom = doorTop;
            doorRight = false;
            doorTop = false;

            if (DoorDirection == 2) {

            } else if (DoorDirection == 1) {
                screenX += 1;
            } else {
                screenY += 1;
            }
        }
    }

    private static int RandomNextUInt32(RandomSeries series) {
        int Result = Random.randomNumberTable[series.Index++];
        if (series.Index >= Random.randomNumberTable.length) {
            series.Index = 0;
        }
        return Result;
    }

    private static int RandomChoice(RandomSeries Series, int ChoiceCount) {
        int Result = RandomNextUInt32(Series) & ChoiceCount;
        return Result;
    }

    private static RandomSeries RandomSeed(int Value)
    {
        RandomSeries Series = new RandomSeries();

        Series.Index = (Value % Random.randomNumberTable.length);

        return(Series);
    }

    private static class RandomSeries {
        public int Index;
    }
}
