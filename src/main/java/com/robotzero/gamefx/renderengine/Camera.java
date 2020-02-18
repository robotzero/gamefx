package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {
    private static final float CAMERA_POS_STEP = 2f;
    private final TileMap tileMap;
    public static TileMap.TileMapPosition position = new TileMap.TileMapPosition();
    public static Vector2f EntityOffsetForFrame = new Vector2f(0.0f, 0.0f);

    public Camera(TileMap tileMap) {
        this.tileMap = tileMap;
    }

    public Matrix4f updateViewMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity();
    }

    public Matrix4f getProjectionMatrix() {
        final Matrix4f p = new Matrix4f();
        return p.ortho(0.0f, DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f, -1, 1);
    }

    public void movePosition(Entity entity) {
        TileMap.TileMapPosition OldCameraP = new TileMap.TileMapPosition(position);

        if(entity.High.P.x()  > (9.0f * TileMap.TileSideInMeters)) {
            position.AbsTileX += 17;
        }
        if(entity.High.P.x() < -(9.0f * TileMap.TileSideInMeters)) {
            position.AbsTileX -= 17;
        }
        if(entity.High.P.y() > (5.0f * TileMap.TileSideInMeters)) {
            position.AbsTileY += 9;
        }
        if(entity.High.P.y() < -(5.0f * TileMap.TileSideInMeters)) {
            position.AbsTileY -= 9;
        }
        TileMap.TileMapDifference dCameraP = tileMap.subtract(position, OldCameraP);
        EntityOffsetForFrame = new Vector2f(-dCameraP.dXY.x(), -dCameraP.dXY.y());
    }
}
