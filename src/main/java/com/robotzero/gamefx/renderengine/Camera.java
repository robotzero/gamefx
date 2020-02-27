package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {
    public static World.WorldPosition position = new World.WorldPosition();
    public static Vector2f EntityOffsetForFrame = new Vector2f(0.0f, 0.0f);
    private final GameMemory gameMemory;
    private final EntityService entityService;

    public Camera(GameMemory gameMemory, EntityService entityService) {
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
//        World.WorldPosition NewCameraP = new World.WorldPosition(position);

//        if(entity.High.P.x()  > (9.0f * World.TileSideInMeters)) {
//            NewCameraP.AbsTileX += 17;
//        }
//        if(entity.High.P.x() < -(9.0f * World.TileSideInMeters)) {
//            NewCameraP.AbsTileX -= 17;
//        }
//        if(entity.High.P.y() > (5.0f * World.TileSideInMeters)) {
//            NewCameraP.AbsTileY += 9;
//        }
//        if(entity.High.P.y() < -(5.0f * World.TileSideInMeters)) {
//            NewCameraP.AbsTileY -= 9;
//        }
        World.WorldPosition NewCameraP = new World.WorldPosition(entity.Low.P);

        SetCamera(NewCameraP);
    }

    public void SetCamera(World.WorldPosition NewCameraP)
    {
        World.WorldDifference dCameraP = World.subtract(new World.WorldPosition(NewCameraP), new World.WorldPosition(position));
        position = NewCameraP;

        int TileSpanX = 17 * 3;
        int TileSpanY = 9 * 3;
        Rectangle CameraBounds = Rectangle.RectCenterDim(new Vector2f(0.0f, 0.0f),
                new Vector2f(TileSpanX, TileSpanY).mul(World.TileSideInMeters));

        EntityOffsetForFrame = new Vector2f(-dCameraP.dXY.x(), -dCameraP.dXY.y());

        entityService.OffsetAndCheckFrequencyByArea(EntityOffsetForFrame, CameraBounds);

        long MinTileX = NewCameraP.ChunkX - Long.divideUnsigned(TileSpanX, 2);
        long MaxTileX = NewCameraP.ChunkX + Long.divideUnsigned(TileSpanX, 2);
        long MinTileY = NewCameraP.ChunkY - Long.divideUnsigned(TileSpanY, 2);
        long MaxTileY = NewCameraP.ChunkY + Long.divideUnsigned(TileSpanY, 2);
        for(int EntityIndex = 1; EntityIndex < gameMemory.LowEntityCount; ++EntityIndex)
        {
            Entity.LowEntity Low = gameMemory.LowEntities[EntityIndex];
            if (Low.HighEntityIndex == 0) {
                if((Low.P.ChunkX >= MinTileX) &&
                        (Low.P.ChunkX <= MaxTileX) &&
                        (Low.P.ChunkY >= MinTileY) &&
                        (Low.P.ChunkY <= MaxTileY))

                {
                    entityService.MakeEntityHighFrequency(EntityIndex);
                }
            }
        }
    }
}
