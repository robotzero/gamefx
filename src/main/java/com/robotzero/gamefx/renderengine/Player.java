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
        TileMap.TileMapPosition NewPlayerP = new TileMap.TileMapPosition();
        NewPlayerP.Offset = positionc.Offset;
        NewPlayerP.AbsTileX = positionc.AbsTileX;
        NewPlayerP.AbsTileY = positionc.AbsTileY;
        NewPlayerP.Offset = (new Vector2f(ddPlayer.x(), ddPlayer.y()).mul(0.5f).mul(interval * interval)).add(new Vector2f(dPlayerP.x(), dPlayerP.y()).mul(interval)).add(NewPlayerP.Offset);
        dPlayerP = new Vector2f(ddPlayer.x(), ddPlayer.y()).mul(interval).add(dPlayerP);
        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);

        TileMap.TileMapPosition PlayerLeft = new TileMap.TileMapPosition();
        PlayerLeft.Offset = NewPlayerP.Offset;
        PlayerLeft.AbsTileX = NewPlayerP.AbsTileX;
        PlayerLeft.AbsTileY = NewPlayerP.AbsTileY;
        PlayerLeft.Offset.x -= 0.5f*PlayerWidth;
        PlayerLeft = tileMap.RecanonicalizePosition(PlayerLeft);

        TileMap.TileMapPosition PlayerRight = new TileMap.TileMapPosition();
        PlayerRight.Offset = NewPlayerP.Offset;
        PlayerRight.AbsTileX = NewPlayerP.AbsTileX;
        PlayerRight.AbsTileY = NewPlayerP.AbsTileY;
        PlayerRight.Offset.x += 0.5f*PlayerWidth;
        PlayerRight = tileMap.RecanonicalizePosition(PlayerRight);


        if (tileMap.IsWorldPointEmpty(NewPlayerP) &&
            tileMap.IsWorldPointEmpty(PlayerLeft) &&
            tileMap.IsWorldPointEmpty(PlayerRight))
        {
            positionc = new TileMap.TileMapPosition();
            positionc.AbsTileX = NewPlayerP.AbsTileX;
            positionc.AbsTileY = NewPlayerP.AbsTileY;
            positionc.Offset = NewPlayerP.Offset;
        }
    }
}
