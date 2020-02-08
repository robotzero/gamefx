package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final TileMap tileMap;
    private static final float PLAYER_POS_STEP = 2f;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 0.75f*PlayerHeight;
    public static TileMap.TileMapPosition positionc = new TileMap.TileMapPosition();

    public Player(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        float PlayerLeft = TileMap.ScreenCenterX - 0.5f * TileMap.MetersToPixels * PlayerWidth;
        float PlayerTop = TileMap.ScreenCenterY - TileMap.MetersToPixels * PlayerHeight;
        return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
    }

    public void movePosition(Vector2f ddPlayer, float interval) {
        ddPlayer = ddPlayer.mul(PLAYER_POS_STEP).mul(interval);

        TileMap.TileMapPosition NewPlayerP = new TileMap.TileMapPosition();
        NewPlayerP.TileRelX = positionc.TileRelX;
        NewPlayerP.TileRelY = positionc.TileRelY;
        NewPlayerP.AbsTileX = positionc.AbsTileX;
        NewPlayerP.AbsTileY = positionc.AbsTileY;
        NewPlayerP.TileRelX = NewPlayerP.TileRelX + ddPlayer.x();
        NewPlayerP.TileRelY = NewPlayerP.TileRelY + ddPlayer.y;
        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);

        TileMap.TileMapPosition PlayerLeft = new TileMap.TileMapPosition();
        PlayerLeft.TileRelX = NewPlayerP.TileRelX;
        PlayerLeft.TileRelY = NewPlayerP.TileRelY;
        PlayerLeft.AbsTileX = NewPlayerP.AbsTileX;
        PlayerLeft.AbsTileY = NewPlayerP.AbsTileY;
        PlayerLeft.TileRelX -= 0.5f*PlayerWidth;
        PlayerLeft = tileMap.RecanonicalizePosition(PlayerLeft);

        TileMap.TileMapPosition PlayerRight = new TileMap.TileMapPosition();
        PlayerRight.TileRelX = NewPlayerP.TileRelX;
        PlayerRight.TileRelY = NewPlayerP.TileRelY;
        PlayerRight.AbsTileX = NewPlayerP.AbsTileX;
        PlayerRight.AbsTileY = NewPlayerP.AbsTileY;
        PlayerRight.TileRelX += 0.5f*PlayerWidth;
        PlayerRight = tileMap.RecanonicalizePosition(PlayerRight);


        if (tileMap.IsWorldPointEmpty(NewPlayerP) &&
            tileMap.IsWorldPointEmpty(PlayerLeft) &&
            tileMap.IsWorldPointEmpty(PlayerRight))
        {
            positionc = new TileMap.TileMapPosition();
            positionc.AbsTileX = NewPlayerP.AbsTileX;
            positionc.AbsTileY = NewPlayerP.AbsTileY;
            positionc.TileRelX = NewPlayerP.TileRelX;
            positionc.TileRelY = NewPlayerP.TileRelY;
        }
    }

    public TileMapPosition recanonicalizePosition(TileMapPosition position) {
        TileMapPosition result = new TileMapPosition(position.getAbsTileX(), position.getAbsTileY(), new Vector2f(position.getOffset().x(), position.getOffset().y()));
        this.recanonicalizeCoord(result);
        return result;
    }

    private void recanonicalizeCoord(TileMapPosition tileMapPosition) {
        int offsetX = Math.round(tileMapPosition.getOffset().x() / 1.4f);
        int offsetY = Math.round(tileMapPosition.getOffset().y() / 1.4f);

        tileMapPosition.setAbsTileX(tileMapPosition.getAbsTileX() + offsetX);
        tileMapPosition.setAbsTileY(tileMapPosition.getAbsTileY() + offsetY);
        tileMapPosition.setOffset(
                tileMapPosition.getOffset().x() - offsetX * 1.4f,
                tileMapPosition.getOffset().y() - offsetY * 1.4f
        );
    }

    public Vector2f subtract(TileMapPosition tileMapPositionA, TileMapPosition tileMapPositionB) {

        Vector2f dTileXY = new Vector2f(
                tileMapPositionA.getAbsTileX() - tileMapPositionB.getAbsTileX(),
                tileMapPositionA.getAbsTileY() - tileMapPositionB.getAbsTileY()
        );

        Vector2f substractV = new Vector2f();
        tileMapPositionA.getOffset().sub(tileMapPositionB.getOffset(), substractV);
        Vector2f multiplication = dTileXY.mul(1.4f);

        return multiplication.add(substractV);
    }
}
