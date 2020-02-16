package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final TileMap tileMap;
    public static final float PlayerHeight = 0.5f;
    public static final float PlayerWidth = 1.0f;
    public static TileMap.TileMapPosition positionc = new TileMap.TileMapPosition();
    public static Vector2f dPlayerP = new Vector2f(0.0f, 0.0f);

    public Player(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        TileMap.TileMapDifference diff = tileMap.subtract(positionc, Camera.position);
        float PlayerGroundPointX = TileMap.ScreenCenterX + TileMap.MetersToPixels * diff.dXY.x();
        float PlayerGroundPointY = TileMap.ScreenCenterY - TileMap.MetersToPixels * diff.dXY.y();
        float PlayerLeft = PlayerGroundPointX - 0.5f * TileMap.MetersToPixels * PlayerWidth;
        float PlayerTop = PlayerGroundPointY - 0.5f * TileMap.MetersToPixels * PlayerHeight;
        return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
    }

    public void movePosition(Vector2f ddPlayer, float interval) {
        TileMap.TileMapPosition OldPlayerP = new TileMap.TileMapPosition(Player.positionc);
        TileMap.TileMapPosition NewPlayerP = OldPlayerP;
        Vector2f playerDelta = new Vector2f(ddPlayer.x(), ddPlayer.y()).mul(0.5f).mul(interval * interval).add(new Vector2f(dPlayerP.x(), dPlayerP.y()).mul(interval));
        NewPlayerP.Offset = new Vector2f(NewPlayerP.Offset.x(), NewPlayerP.Offset.y()).add(playerDelta);
        dPlayerP = new Vector2f(ddPlayer.x(), ddPlayer.y()).mul(interval).add(dPlayerP);
        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);

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

        int MinTileX = Math.max(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX);
        int MinTileY = Math.max(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY);
        int MaxTileX = Math.max(OldPlayerP.AbsTileX, NewPlayerP.AbsTileX);
        int MaxTileY = Math.max(OldPlayerP.AbsTileY, NewPlayerP.AbsTileY);

        int EntityTileWidth = (int) Math.ceil(PlayerWidth / TileMap.TileSideInMeters);
        int EntityTileHeight = (int) Math.ceil(PlayerHeight / TileMap.TileSideInMeters);

        MinTileX -= EntityTileWidth;
        MinTileY -= EntityTileHeight;
        MaxTileX += EntityTileWidth;
        MaxTileY += EntityTileHeight;

        float tRemaining = 1.0f;
        for(int Iteration = 0; (Iteration < 4) && (tRemaining > 0.0f); ++Iteration) {
            float[] tMin = {1, 0};
            Vector2f WallNormal = new Vector2f(0.0f, 0.0f);

            for (int AbsTileY = MinTileY; AbsTileY <= MaxTileY; ++AbsTileY) {
                for (int AbsTileX = MinTileX; AbsTileX <= MaxTileX; ++AbsTileX) {
                    TileMap.TileMapPosition TestTileP = tileMap.CenteredTilePoint(AbsTileX, AbsTileY);
                    int TileValue = tileMap.GetTileValue(TestTileP);
                    if (!tileMap.IsTileValueEmpty(TileValue)) {
                        float DiameterW = TileMap.TileSideInMeters + PlayerWidth;
                        float DiameterH = TileMap.TileSideInMeters + PlayerHeight;
                        Vector2f MinCorner = new Vector2f(DiameterW, DiameterH).mul(-0.5f);
                        Vector2f MaxCorner = new Vector2f(DiameterW, DiameterH).mul(0.5f);

                        TileMap.TileMapDifference RelOldPlayerP = tileMap.subtract(positionc, TestTileP);
                        Vector2f Rel = RelOldPlayerP.dXY;

                        if (tileMap.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                            WallNormal = new Vector2f(-1, 0);
                        }

                        if (tileMap.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                            WallNormal = new Vector2f(1, 0);
                        }

                        if (tileMap.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                            WallNormal = new Vector2f(0, -1);
                        }

                        if (tileMap.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                            WallNormal = new Vector2f(0, 1);
                        }
                    }
                }
            }
            positionc = tileMap.Offset(OldPlayerP, playerDelta.mul(tMin[0]));
            dPlayerP = new Vector2f(dPlayerP.x(), dPlayerP.y()).sub(new Vector2f(WallNormal).mul(new Vector2f(dPlayerP.x(), dPlayerP.y()).dot(WallNormal)));
            playerDelta = new Vector2f(playerDelta).sub(new Vector2f(WallNormal).mul(new Vector2f(playerDelta).dot(WallNormal)));
            tRemaining -= tMin[0] * tRemaining;
        }
    }
}
