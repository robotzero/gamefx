package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;

public class Camera {
    public static World.WorldPosition position = new World.WorldPosition();
    public static World.WorldPosition oldPosition = new World.WorldPosition();
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

//    public void movePosition(LowEntity entity) {
//        World.WorldPosition NewCameraP = new World.WorldPosition(entity.Low.P);
//
//        SetCamera(NewCameraP);
//    }

//    public void SetCamera(World.WorldPosition NewCameraP) {
//        World.WorldDifference dCameraP = World.subtract(new World.WorldPosition(NewCameraP), new World.WorldPosition(position));
//        oldPosition = new World.WorldPosition(position);
//        position = NewCameraP;
//        assert(entityService.ValidateEntityPairs());
//        int TileSpanX = 17 * 3;
//        int TileSpanY = 9 * 3;
//        Rectangle CameraBounds = Rectangle.RectCenterDim(new Vector2f(0.0f, 0.0f),
//                new Vector2f(TileSpanX, TileSpanY).mul(World.TileSideInMeters));
//
//        EntityOffsetForFrame = new Vector2f(-dCameraP.dXY.x(), -dCameraP.dXY.y());
//
//        entityService.OffsetAndCheckFrequencyByArea(EntityOffsetForFrame, CameraBounds);
//        assert(entityService.ValidateEntityPairs());
//
//        // TODO(casey): Do this in terms of tile chunks!
//        World.WorldPosition MinChunkP = entityService.getWorld().MapIntoChunkSpace(NewCameraP, Rectangle.GetMinCorner(CameraBounds));
//        World.WorldPosition MaxChunkP = entityService.getWorld().MapIntoChunkSpace(NewCameraP, Rectangle.GetMaxCorner(CameraBounds));
//        for (int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
//            for (int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
//                WorldChunk Chunk = entityService.getWorld().GetWorldChunk(ChunkX, ChunkY, false);
//                if (Chunk != null) {
//                    for (WorldEntityBlock Block = Chunk.getFirstBlock().get(0); Block != null; Block = Block.next == null ? null : Chunk.getFirstBlock().get(Block.next)) {
//                        for (int EntityIndexIndex = 0; EntityIndexIndex < Block.EntityCount; ++EntityIndexIndex) {
//                            int LowEntityIndex = Block.LowEntityIndex[EntityIndexIndex];
//                            LowEntity.LowEntity Low = gameMemory.LowEntities[LowEntityIndex];
//                            if (Low.HighEntityIndex == 0) {
//                                Vector2f CameraSpaceP = entityService.GetCameraSpaceP(Low);
//                                if (Rectangle.IsInRectangle(CameraBounds, CameraSpaceP)) {
//                                    entityService.MakeEntityHighFrequency(Low, LowEntityIndex, CameraSpaceP);
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
//    }
}
