package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlayerService {
    private final TileMap tileMap;
    private final GameMemory gameMemory;
    public static final float PlayerHeight = 0.5f;
    public static final float PlayerWidth = 1.0f;
//    public static TileMap.TileMapPosition positionc = new TileMap.TileMapPosition();
//    public static Vector2f dPlayerP = new Vector2f(0.0f, 0.0f);

    public PlayerService(TileMap tileMap, GameMemory gameMemory) {
        this.tileMap = tileMap;
        this.gameMemory = gameMemory;
    }

    public Matrix4f getModelMatrix() {
        for (int EntityIndex = 0; EntityIndex < gameMemory.entityCount; ++EntityIndex) {
            if (gameMemory.EntityResidence[EntityIndex] == Entity.EntityResidence.High) {
                Entity.HighEntity highEntity = gameMemory.HighEntities[EntityIndex];
                Entity.LowEntity lowEntity = gameMemory.LowEntities[EntityIndex];
                Entity.DormantEntity dormantEntity = gameMemory.DormantEntities[EntityIndex];
                final Matrix4f v = new Matrix4f();
//                TileMap.TileMapDifference diff = tileMap.subtract(positionc, Camera.position);
                float PlayerGroundPointX = TileMap.ScreenCenterX + TileMap.MetersToPixels * highEntity.P.x();
                float PlayerGroundPointY = TileMap.ScreenCenterY - TileMap.MetersToPixels * highEntity.P.y();
                float PlayerLeft = PlayerGroundPointX - 0.5f * TileMap.MetersToPixels * dormantEntity.Width;
                float PlayerTop = PlayerGroundPointY - 0.5f * TileMap.MetersToPixels * dormantEntity.Height;
                return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
            }
        }
        return new Matrix4f().identity();
    }

    public void movePlayer(Entity entity, Vector2f ddP, float interval, int playerSpeed) {
        float ddPLength = ddP.lengthSquared();
        if (ddPLength > 1.0f) {
            ddP = new Vector2f(ddP.x(), ddP.y()).mul((float) (1.0f/ Math.sqrt(ddPLength)));
        }

        ddP = ddP.mul(playerSpeed);
        ddP = ddP.add(new Vector2f(entity.High.P.x(), entity.High.P.y()).mul(-8.0f));


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
//        float tRemaining = 1.0f;
//        for(int Iteration = 0; (Iteration < 4) && (tRemaining > 0.0f); ++Iteration) {
//            float[] tMin = {1, 0};
//            Vector2f WallNormal = new Vector2f(0.0f, 0.0f);
//
//            for (int AbsTileY = MinTileY; AbsTileY <= MaxTileY; ++AbsTileY) {
//                for (int AbsTileX = MinTileX; AbsTileX <= MaxTileX; ++AbsTileX) {
//                    TileMap.TileMapPosition TestTileP = tileMap.CenteredTilePoint(AbsTileX, AbsTileY);
//                    int TileValue = tileMap.GetTileValue(TestTileP);
//                    if (!tileMap.IsTileValueEmpty(TileValue)) {
//                        float DiameterW = TileMap.TileSideInMeters + PlayerWidth;
//                        float DiameterH = TileMap.TileSideInMeters + PlayerHeight;
//                        Vector2f MinCorner = new Vector2f(DiameterW, DiameterH).mul(-0.5f);
//                        Vector2f MaxCorner = new Vector2f(DiameterW, DiameterH).mul(0.5f);
//
//                        TileMap.TileMapDifference RelOldPlayerP = tileMap.subtract(positionc, TestTileP);
//                        Vector2f Rel = RelOldPlayerP.dXY;
//
//                        if (tileMap.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
//                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
//                            WallNormal = new Vector2f(-1, 0);
//                        }
//
//                        if (tileMap.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
//                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
//                            WallNormal = new Vector2f(1, 0);
//                        }
//
//                        if (tileMap.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
//                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
//                            WallNormal = new Vector2f(0, -1);
//                        }
//
//                        if (tileMap.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
//                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
//                            WallNormal = new Vector2f(0, 1);
//                        }
//                    }
//                }
//            }
//            positionc = tileMap.Offset(OldPlayerP, playerDelta.mul(tMin[0]));
//            dPlayerP = new Vector2f(dPlayerP.x(), dPlayerP.y()).sub(new Vector2f(WallNormal).mul(new Vector2f(dPlayerP.x(), dPlayerP.y()).dot(WallNormal)));
//            playerDelta = new Vector2f(playerDelta).sub(new Vector2f(WallNormal).mul(new Vector2f(playerDelta).dot(WallNormal)));
//            tRemaining -= tMin[0] * tRemaining;
//        }
    }
}
