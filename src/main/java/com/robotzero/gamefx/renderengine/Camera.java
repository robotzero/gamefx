package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;

public class Camera {
    private static final float CAMERA_POS_STEP = 2f;
    private final TileMap tileMap;
    public static TileMap.TileMapPosition position = new TileMap.TileMapPosition();

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

    public void movePosition(TileMap.TileMapPosition playerPosition, TileMap.TileMapPosition cameraPosition) {
        TileMap.TileMapDifference diff = tileMap.subtract(playerPosition, cameraPosition);
        if(diff.dXY.x > (9.0f * TileMap.TileSideInMeters)) {
            position.AbsTileX += 17;
        }
        if(diff.dXY.x < -(9.0f * TileMap.TileSideInMeters)) {
            position.AbsTileX -= 17;
        }
        if(diff.dXY.y > (5.0f * TileMap.TileSideInMeters)) {
            position.AbsTileY += 9;
        }
        if(diff.dXY.y < -(5.0f * TileMap.TileSideInMeters)) {
            position.AbsTileY -= 9;
        }
//        position = position.add(offsetX * CAMERA_POS_STEP, offsetY * CAMERA_POS_STEP, 0);
    }
}
