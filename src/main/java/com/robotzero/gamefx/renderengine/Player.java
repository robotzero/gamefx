package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final TileMap tileMap;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 0.75f * PlayerHeight;
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
        float PlayerTop = PlayerGroundPointY - TileMap.MetersToPixels * PlayerHeight;
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

        int StartTileX = OldPlayerP.AbsTileX;
        int StartTileY = OldPlayerP.AbsTileY;
        int EndTileX = NewPlayerP.AbsTileX;
        int EndTileY = NewPlayerP.AbsTileY;

        if(EndTileY > StartTileY)
        {
            int x = 4;
        }

        int DeltaX = tileMap.SignOf(EndTileX - StartTileX);
        int DeltaY = tileMap.SignOf(EndTileY - StartTileY);

        float tMin = 1.0f;
        int AbsTileY = StartTileY;
        for(;;) {
            int AbsTileX = StartTileX;
            for(;;) {
                TileMap.TileMapPosition TestTileP = tileMap.CenteredTilePoint(AbsTileX, AbsTileY);
                int TileValue = tileMap.GetTileValue(TestTileP);
                if(!tileMap.IsTileValueEmpty(TileValue)) {
                    Vector2f MinCorner = new Vector2f(TileMap.TileSideInMeters, TileMap.TileSideInMeters).mul(-0.5f);
                    Vector2f MaxCorner = new Vector2f(TileMap.TileSideInMeters, TileMap.TileSideInMeters).mul(0.5f);

                    TileMap.TileMapDifference RelOldPlayerP = tileMap.subtract(OldPlayerP, TestTileP);
                    Vector2f Rel = RelOldPlayerP.dXY;

                    tMin = tileMap.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(), tMin, MinCorner.y(), MaxCorner.y());
                    tMin = tileMap.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(), tMin, MinCorner.y(), MaxCorner.y());
                    tMin = tileMap.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(), tMin, MinCorner.x(), MaxCorner.x());
                    tMin = tileMap.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(), tMin, MinCorner.x(), MaxCorner.x());
                }

                if(AbsTileX == EndTileX) {
                    break;
                } else {
                    AbsTileX += DeltaX;
                }
            }

            if(AbsTileY == EndTileY) {
                break;
            } else {
                AbsTileY += DeltaY;
            }
        }

//        NewPlayerP = OldPlayerP;
//        NewPlayerP.Offset = new Vector2f(NewPlayerP.Offset.x(), NewPlayerP.Offset.y()).add(playerDelta.mul(tMin));
        positionc = tileMap.Offset(OldPlayerP, playerDelta.mul(tMin));
//        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);
    }
}
