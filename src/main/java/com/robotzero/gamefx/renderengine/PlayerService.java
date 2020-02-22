package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlayerService {
    private final TileMap tileMap;
    private final EntityService entityService;
    private final GameMemory gameMemory;
    public static final float PlayerHeight = 0.5f;
    public static final float PlayerWidth = 1.0f;

    public PlayerService(TileMap tileMap, EntityService entityService, GameMemory gameMemory) {
        this.tileMap = tileMap;
        this.entityService = entityService;
        this.gameMemory = gameMemory;
    }

    public Matrix4f getModelMatrix() {
        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ++HighEntityIndex) {
//            if (gameMemory.EntityResidence[EntityIndex] == Entity.EntityResidence.High) {
                Entity.HighEntity highEntity = gameMemory.HighEntities[HighEntityIndex];
                Entity.LowEntity lowEntity = gameMemory.LowEntities[highEntity.LowEntityIndex];

                highEntity.P = new Vector2f(highEntity.P).add(Camera.EntityOffsetForFrame);
                final Matrix4f v = new Matrix4f();
                float PlayerGroundPointX = TileMap.ScreenCenterX + TileMap.MetersToPixels * highEntity.P.x();
                float PlayerGroundPointY = TileMap.ScreenCenterY - TileMap.MetersToPixels * highEntity.P.y();
                float PlayerLeft = PlayerGroundPointX - 0.5f * TileMap.MetersToPixels * lowEntity.Width;
                float PlayerTop = PlayerGroundPointY - 0.5f * TileMap.MetersToPixels * lowEntity.Height;

                if (lowEntity.Type == EntityType.HERO) {
                    return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
                }
//            }
        }
        return new Matrix4f().identity();
    }

    public void movePlayer(Entity entity, Vector2f ddP, float interval, int playerSpeed) {
        float ddPLength = ddP.lengthSquared();
        if (ddPLength > 1.0f) {
            ddP = new Vector2f(ddP.x(), ddP.y()).mul((float) (1.0f / Math.sqrt(ddPLength)));
        }

        ddP = ddP.mul(playerSpeed);
        ddP = ddP.add(new Vector2f(entity.High.dP.x(), entity.High.dP.y()).mul(-8.0f));


        Vector2f OldPlayerP = new Vector2f(entity.High.P);
        Vector2f playerDelta = new Vector2f(ddP.x(), ddP.y()).mul(0.5f).mul(interval * interval).add(new Vector2f(entity.High.dP.x(), entity.High.dP.y()).mul(interval));
        entity.High.dP = new Vector2f(ddP).mul(interval).add(new Vector2f(entity.High.dP));
        Vector2f NewPlayerP = OldPlayerP.add(playerDelta);

//        TileMap.TileMapPosition PlayerLeft = new TileMap.TileMapPosition(NewPlayerP);
//        PlayerLeft.Offset.x -= 0.5f*PlayerWidth;
//        PlayerLeft = tileMap.RecanonicalizePosition(PlayerLeft);
//
//        TileMap.TileMapPosition PlayerRight = new TileMap.TileMapPosition(NewPlayerP);
//        PlayerRight.Offset.x += 0.5f*PlayerWidth;
//        PlayerRight = tileMap.RecanonicalizePosition(PlayerRight);
//
//        boolean Collided = false;
//        TileMap.TileMapPosition colP = new TileMap.TileMapPosition();
//        if (!tileMap.IsTileMapPointEmpty(NewPlayerP)) {
//            colP = NewPlayerP;
//            Collided = true;
//        }
//
//        if (!tileMap.IsTileMapPointEmpty(PlayerLeft)) {
//            colP = PlayerLeft;
//            Collided = true;
//        }
//
//        if (!tileMap.IsTileMapPointEmpty(PlayerRight)) {
//            colP = PlayerRight;
//            Collided = true;
//        }
//
//        if (Collided)
//        {
//            var reflection = new Vector2f(0f, 0f);
//            if (colP.AbsTileX < Player.positionc.AbsTileX) {
//                reflection = new Vector2f(1f, 0f);
//            }
//
//            if (colP.AbsTileX > Player.positionc.AbsTileX) {
//                reflection = new Vector2f(-1f, 0f);
//            }
//
//            if (colP.AbsTileY < Player.positionc.AbsTileY) {
//                reflection = new Vector2f(0f, 1f);
//            }
//
//            if (colP.AbsTileY > Player.positionc.AbsTileY) {
//                reflection = new Vector2f(0f, -1f);
//            }
//
//            Player.dPlayerP = new Vector2f(dPlayerP.x(), dPlayerP.y()).sub(new Vector2f(reflection.x(), reflection.y()).mul(new Vector2f(dPlayerP.x(), dPlayerP.y()).dot(reflection)));
//        } else {
//            Player.positionc = NewPlayerP;
//        }
//        int MinTileX = Math.min(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX);
//        int MinTileY = Math.min(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY);
//        int OnePastMaxTileX = Math.max(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX) + 1;
//        int OnePastMaxTileY = Math.max(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY) + 1;

//        int MinTileX = Math.max(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX);
//        int MinTileY = Math.max(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY);
//        int MaxTileX = Math.max(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX);
//        int MaxTileY = Math.max(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY);
//
//        int EntityTileWidth = (int) Math.ceil(PlayerWidth / TileMap.TileSideInMeters);
//        int EntityTileHeight = (int) Math.ceil(PlayerHeight / TileMap.TileSideInMeters);
//
//        MinTileX -= EntityTileWidth;
//        MinTileY -= EntityTileHeight;
//        MaxTileX += EntityTileWidth;
//        MaxTileY += EntityTileHeight;
//
        for (int Iteration = 0; (Iteration < 4); ++Iteration) {
            float[] tMin = {1, 0};
            Vector2f WallNormal = new Vector2f(0.0f, 0.0f);
            int HitHighEntityIndex = 0;
            Vector2f DesiredPosition = new Vector2f(entity.High.P).add(playerDelta);
            for (int TestHighEntityIndex = 1; TestHighEntityIndex < gameMemory.HighEntityCount; ++TestHighEntityIndex) {
                    if (TestHighEntityIndex != entity.Low.HighEntityIndex) {
                        Entity TestEntity = new Entity();
                        TestEntity.High = gameMemory.HighEntities[TestHighEntityIndex];
                        TestEntity.LowIndex = TestEntity.High.LowEntityIndex;
                        TestEntity.Low = gameMemory.LowEntities[TestEntity.LowIndex];
                        if (TestEntity.Low.Collides) {
                            float DiameterW = TestEntity.Low.Width + entity.Low.Width;
                            float DiameterH = TestEntity.Low.Height + entity.Low.Height;
                            Vector2f MinCorner = new Vector2f(DiameterW, DiameterH).mul(-0.5f);
                            Vector2f MaxCorner = new Vector2f(DiameterW, DiameterH).mul(0.5f);
                            Vector2f Rel = new Vector2f(entity.High.P).sub(new Vector2f(TestEntity.High.P));
                            if (tileMap.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                    tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                                WallNormal = new Vector2f(-1, 0);
                                HitHighEntityIndex = TestHighEntityIndex;
                            }

                            if (tileMap.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                    tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                                WallNormal = new Vector2f(1, 0);
                                HitHighEntityIndex = TestHighEntityIndex;
                            }

                            if (tileMap.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                    tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                                WallNormal = new Vector2f(0, -1);
                                HitHighEntityIndex = TestHighEntityIndex;
                            }

                            if (tileMap.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                    tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                                WallNormal = new Vector2f(0, 1);
                                HitHighEntityIndex = TestHighEntityIndex;
                            }
                        }
                    }
                }
            entity.High.P = new Vector2f(entity.High.P).add(new Vector2f(playerDelta).mul(tMin[0]));
            if(HitHighEntityIndex > 0)
            {
                entity.High.dP = new Vector2f(entity.High.dP).sub(new Vector2f(WallNormal).mul(new Vector2f(entity.High.dP).dot(WallNormal)));
                playerDelta = new Vector2f(DesiredPosition).sub(new Vector2f(entity.High.P));
                playerDelta = new Vector2f(playerDelta).sub(new Vector2f(WallNormal).mul(new Vector2f(playerDelta).dot(new Vector2f(WallNormal))));

                Entity.HighEntity HitHigh = gameMemory.HighEntities[HitHighEntityIndex];
                Entity.LowEntity HitLow = gameMemory.LowEntities[HitHigh.LowEntityIndex];
            }
            else
            {
                break;
            }
        }

        entity.Low.P = tileMap.MapIntoTileSpace(Camera.position, entity.High.P);
    }
}
