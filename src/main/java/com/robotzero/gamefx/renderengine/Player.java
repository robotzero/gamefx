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

    public Player(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        TileMap.TileMapDifference diff = tileMap.subtract(positionc, Camera.position);
        float PlayerGroundPointX = TileMap.ScreenCenterX + TileMap.MetersToPixels * diff.dX;
        float PlayerGroundPointY = TileMap.ScreenCenterY - TileMap.MetersToPixels * diff.dY;
        float PlayerLeft = PlayerGroundPointX - 0.5f * TileMap.MetersToPixels * PlayerWidth;
        float PlayerTop = PlayerGroundPointY - TileMap.MetersToPixels * PlayerHeight;
        return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
    }

    public void movePosition(Vector2f ddPlayer, float interval, float speed) {
        ddPlayer = ddPlayer.mul(speed).mul(interval);

        TileMap.TileMapPosition NewPlayerP = new TileMap.TileMapPosition();
        NewPlayerP.OffsetX = positionc.OffsetX;
        NewPlayerP.OffsetY = positionc.OffsetY;
        NewPlayerP.AbsTileX = positionc.AbsTileX;
        NewPlayerP.AbsTileY = positionc.AbsTileY;
        NewPlayerP.OffsetX = NewPlayerP.OffsetX + ddPlayer.x();
        NewPlayerP.OffsetY = NewPlayerP.OffsetY + ddPlayer.y;
        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);

        TileMap.TileMapPosition PlayerLeft = new TileMap.TileMapPosition();
        PlayerLeft.OffsetX = NewPlayerP.OffsetX;
        PlayerLeft.OffsetY = NewPlayerP.OffsetY;
        PlayerLeft.AbsTileX = NewPlayerP.AbsTileX;
        PlayerLeft.AbsTileY = NewPlayerP.AbsTileY;
        PlayerLeft.OffsetX -= 0.5f*PlayerWidth;
        PlayerLeft = tileMap.RecanonicalizePosition(PlayerLeft);

        TileMap.TileMapPosition PlayerRight = new TileMap.TileMapPosition();
        PlayerRight.OffsetX = NewPlayerP.OffsetX;
        PlayerRight.OffsetY = NewPlayerP.OffsetY;
        PlayerRight.AbsTileX = NewPlayerP.AbsTileX;
        PlayerRight.AbsTileY = NewPlayerP.AbsTileY;
        PlayerRight.OffsetX += 0.5f*PlayerWidth;
        PlayerRight = tileMap.RecanonicalizePosition(PlayerRight);


        if (tileMap.IsWorldPointEmpty(NewPlayerP) &&
            tileMap.IsWorldPointEmpty(PlayerLeft) &&
            tileMap.IsWorldPointEmpty(PlayerRight))
        {
            positionc = new TileMap.TileMapPosition();
            positionc.AbsTileX = NewPlayerP.AbsTileX;
            positionc.AbsTileY = NewPlayerP.AbsTileY;
            positionc.OffsetX = NewPlayerP.OffsetX;
            positionc.OffsetY = NewPlayerP.OffsetY;
        }
    }
}
