package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import imgui.Col;
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

        boolean Collided = false;
        TileMap.TileMapPosition colP = new TileMap.TileMapPosition();
        if (!tileMap.IsWorldPointEmpty(NewPlayerP)) {
            colP = NewPlayerP;
            Collided = true;
        }

        if (!tileMap.IsWorldPointEmpty(PlayerLeft)) {
            colP = PlayerLeft;
            Collided = true;
        }

        if (!tileMap.IsWorldPointEmpty(PlayerRight)) {
            colP = PlayerRight;
            Collided = true;
        }

        if (Collided)
        {
            var reflection = new Vector2f(0f, 0f);
            if (colP.AbsTileX < Player.positionc.AbsTileX) {
                reflection = new Vector2f(1f, 0f);
            }

            if (colP.AbsTileX > Player.positionc.AbsTileX) {
                reflection = new Vector2f(-1f, 0f);
            }

            if (colP.AbsTileY < Player.positionc.AbsTileY) {
                reflection = new Vector2f(0f, 1f);
            }

            if (colP.AbsTileY > Player.positionc.AbsTileY) {
                reflection = new Vector2f(0f, -1f);
            }

            Player.dPlayerP = new Vector2f(dPlayerP.x(), dPlayerP.y()).sub(new Vector2f(reflection.x(), reflection.y()).mul(new Vector2f(dPlayerP.x(), dPlayerP.y()).dot(reflection)));
        } else {
            Player.positionc = NewPlayerP;
        }

    }
}
