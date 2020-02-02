package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class Player {
    private final TileMap tileMap;
    private static final float PLAYER_POS_STEP = 264;
    public static final float playerHeight = TileMap.TileHeight;
    public static final float playerWidth = 0.75f * playerHeight;
    private Vector3f dPlayerP = new Vector3f(200f, 300f, 1f);
    private TileMapPosition position = new TileMapPosition(1, 3, new Vector2f(5.0f, 5.0f));
    private Vector3f dPosition = new Vector3f(130f, 80f, 0f);
    public static int playerTileMapX = 0;
    public static int playerTileMapY = 0;

    public Player(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(dPosition);
    }

    public void movePosition(Vector2f ddPlayer, float interval) {
        ddPlayer = ddPlayer.mul(PLAYER_POS_STEP).mul(interval);
        final var newPlayer = new Vector3f(dPosition.x(), dPosition.y(), 0).add(ddPlayer.x(), ddPlayer.y(), 0);
        TileMap.RawPosition PlayerPos = new TileMap.RawPosition();
        PlayerPos.TileMapX = Player.playerTileMapX;
        PlayerPos.TileMapY = Player.playerTileMapY;
        PlayerPos.X = newPlayer.x();
        PlayerPos.Y = newPlayer.y();

        if (tileMap.IsWorldPointEmpty(PlayerPos))
//        IsWorldPointEmpty(&World, PlayerLeft) &&
//        IsWorldPointEmpty(&World, PlayerRight))
        {
            TileMap.CannonicalPosition CanPos = tileMap.GetCanonicalPosition(PlayerPos);

            playerTileMapX = CanPos.TileMapX;
            playerTileMapY = CanPos.TileMapY;
            float x  = (float) (TileMap.UpperLeftX + TileMap.TileWidth*CanPos.TileX + CanPos.TileRelX);
            float y = (float) (TileMap.UpperLeftY + TileMap.TileHeight*CanPos.TileY + CanPos.TileRelY);
            dPosition = new Vector3f(x, y, dPosition.z());
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
