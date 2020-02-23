package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {
    private static final float CAMERA_POS_STEP = 2f;
    private final TileMap tileMap;
    public static TileMap.TileMapPosition position = new TileMap.TileMapPosition();
    public static Vector2f EntityOffsetForFrame = new Vector2f(0.0f, 0.0f);
    private final GameMemory gameMemory;
    private final EntityService entityService;

    public Camera(TileMap tileMap, GameMemory gameMemory, EntityService entityService) {
        this.tileMap = tileMap;
        this.gameMemory = gameMemory;
        this.entityService = entityService;
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
        TileMap.TileMapPosition NewCameraP = new TileMap.TileMapPosition(position);

        if(entity.High.P.x()  > (9.0f * TileMap.TileSideInMeters)) {
            NewCameraP.AbsTileX += 17;
        }
        if(entity.High.P.x() < -(9.0f * TileMap.TileSideInMeters)) {
            NewCameraP.AbsTileX -= 17;
        }
        if(entity.High.P.y() > (5.0f * TileMap.TileSideInMeters)) {
            NewCameraP.AbsTileY += 9;
        }
        if(entity.High.P.y() < -(5.0f * TileMap.TileSideInMeters)) {
            NewCameraP.AbsTileY -= 9;
        }

        SetCamera(NewCameraP);
    }

    public void SetCamera(TileMap.TileMapPosition NewCameraP)
    {
        TileMap.TileMapDifference dCameraP = TileMap.subtract(NewCameraP, position);
        position = NewCameraP;

        int TileSpanX = 17 * 3;
        int TileSpanY = 9 * 3;
        Rectangle CameraBounds = Rectangle.RectCenterDim(new Vector2f(0.0f, 0.0f),
                new Vector2f(TileSpanX, TileSpanY).mul(TileMap.TileSideInMeters));

        EntityOffsetForFrame = new Vector2f(-dCameraP.dXY.x(), -dCameraP.dXY.y());

        entityService.OffsetAndCheckFrequencyByArea(EntityOffsetForFrame, CameraBounds);

        long MinTileX = Long.divideUnsigned(NewCameraP.AbsTileX - TileSpanX, 2);
        long MaxTileX = Long.divideUnsigned(NewCameraP.AbsTileX + TileSpanX, 2);
        long MinTileY = Long.divideUnsigned(NewCameraP.AbsTileY - TileSpanY, 2);
        long MaxTileY = Long.divideUnsigned(NewCameraP.AbsTileY + TileSpanY, 2);
        for(int EntityIndex = 1; EntityIndex < gameMemory.LowEntityCount; ++EntityIndex)
        {
            Entity.LowEntity Low = gameMemory.LowEntities[EntityIndex];
            if (Low.HighEntityIndex == 0) {
                if((Low.P.AbsTileX >= MinTileX) &&
                        (Low.P.AbsTileX <= MaxTileX) &&
                        (Low.P.AbsTileY >= MinTileY) &&
                        (Low.P.AbsTileY <= MaxTileY))

                {
                    entityService.MakeEntityHighFrequency(EntityIndex);
                }
            }
        }
    }
}
