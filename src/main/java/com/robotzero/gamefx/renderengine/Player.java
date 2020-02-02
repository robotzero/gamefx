package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final TileMap tileMap;
    private static final float PLAYER_POS_STEP = 2f;
    public static final float playerHeight = TileMap.TileSideInPixels;
    public static final float playerWidth = 0.75f * TileMap.TileSideInPixels;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 0.75f*PlayerHeight;
//    private Vector3f dPlayerP = new Vector3f(200f, 300f, 1f);
//    private TileMapPosition position = new TileMapPosition(1, 3, new Vector2f(5.0f, 5.0f));
    public static TileMap.CannonicalPosition positionc = new TileMap.CannonicalPosition();
//    private Vector3f dPosition = new Vector3f(130f, 140f, 0f);

    public Player(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        float PlayerLeft = TileMap.UpperLeftX + TileMap.TileSideInPixels * positionc.TileX +
                TileMap.MetersToPixels * positionc.TileRelX - 0.5f * TileMap.MetersToPixels * PlayerWidth;
        float PlayerTop = TileMap.UpperLeftY + TileMap.TileSideInPixels * positionc.TileY +
                TileMap.MetersToPixels * positionc.TileRelY - TileMap.MetersToPixels*PlayerHeight;
        return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
    }

    public void movePosition(Vector2f ddPlayer, float interval) {
        ddPlayer = ddPlayer.mul(PLAYER_POS_STEP).mul(interval);
//        final var newPlayer = new Vector3f(dPosition.x(), dPosition.y(), 0).add(ddPlayer.x(), ddPlayer.y(), 0);
        TileMap.CannonicalPosition NewPlayerP = positionc;
        NewPlayerP.TileRelX += ddPlayer.x();
        NewPlayerP.TileRelY += ddPlayer.y();
        NewPlayerP = tileMap.RecanonicalizePosition(NewPlayerP);

        if (tileMap.IsWorldPointEmpty(NewPlayerP))
//        IsWorldPointEmpty(&World, PlayerLeft) &&
//        IsWorldPointEmpty(&World, PlayerRight))
        {
            positionc = NewPlayerP;
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
