package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

public class PlayerService {
    private final World world;
    private final EntityService entityService;
    private final GameMemory gameMemory;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 1.0f;

    public PlayerService(World world, EntityService entityService, GameMemory gameMemory) {
        this.world = world;
        this.entityService = entityService;
        this.gameMemory = gameMemory;
    }

    public Matrix4f getModelMatrix() {
        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ++HighEntityIndex) {
//            if (gameMemory.EntityResidence[EntityIndex] == Entity.EntityResidence.High) {
            Entity.HighEntity highEntity = gameMemory.HighEntities[HighEntityIndex];
            Entity.LowEntity lowEntity = gameMemory.LowEntities[highEntity.LowEntityIndex];
            //@@TODO
//            highEntity.P = new Vector2f(highEntity.P).add(Camera.EntityOffsetForFrame);
            highEntity.P = new Vector2f(highEntity.P);
            final Matrix4f v = new Matrix4f();
            float PlayerGroundPointX = World.ScreenCenterX + World.MetersToPixels * highEntity.P.x();
            float PlayerGroundPointY = World.ScreenCenterY - World.MetersToPixels * highEntity.P.y();
            float PlayerLeft = PlayerGroundPointX - 0.5f * World.MetersToPixels * lowEntity.Width;
            float PlayerTop = PlayerGroundPointY - 0.5f * World.MetersToPixels * lowEntity.Height;

            if (lowEntity.Type == EntityType.HERO) {
                return v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f));
            }
        }
        return new Matrix4f().identity();
    }

    public void movePlayer(Entity entity, Vector2f ddP, float interval, int playerSpeed) {
        float ddPLength = new Vector2f(ddP).lengthSquared();
        if (ddPLength > 1.0f) {
            ddP = new Vector2f(ddP.x(), ddP.y()).mul((float) (1.0f / Math.sqrt(ddPLength)));
        }

        ddP = ddP.mul(playerSpeed);
        ddP = ddP.add(new Vector2f(entity.High.dP.x(), entity.High.dP.y()).mul(-8.0f));


        Vector2f OldPlayerP = new Vector2f(entity.High.P);
        Vector2f playerDelta = new Vector2f(ddP.x(), ddP.y()).mul(0.5f).mul(interval * interval).add(new Vector2f(entity.High.dP.x(), entity.High.dP.y()).mul(interval));
        entity.High.dP = new Vector2f(ddP).mul(interval).add(new Vector2f(entity.High.dP));
        Vector2f NewPlayerP = OldPlayerP.add(playerDelta);

        for (int Iteration = 0; (Iteration < 4); ++Iteration) {
            float[] tMin = {1.0f, 0.0f};
            Vector2f WallNormal = new Vector2f(0.0f, 0.0f);
            int HitHighEntityIndex = 0;
            Vector2f DesiredPosition = new Vector2f(entity.High.P).add(playerDelta);
            for (int TestHighEntityIndex = 1; TestHighEntityIndex < gameMemory.HighEntityCount; ++TestHighEntityIndex) {
                if (TestHighEntityIndex != entity.Low.HighEntityIndex) {
                    Entity TestEntity = new Entity();
                    TestEntity.High = gameMemory.HighEntities[TestHighEntityIndex];
                    TestEntity.LowIndex = TestEntity.High.LowEntityIndex;
                    TestEntity.Low = gameMemory.LowEntities[TestEntity.LowIndex];
                    if (TestEntity.Low.Collides) {
                        float DiameterW = TestEntity.Low.Width + entity.Low.Width;
                        float DiameterH = TestEntity.Low.Height + entity.Low.Height;
                        Vector2f MinCorner = new Vector2f(DiameterW, DiameterH).mul(-0.5f);
                        Vector2f MaxCorner = new Vector2f(DiameterW, DiameterH).mul(0.5f);
                        Vector2f Rel = new Vector2f(entity.High.P).sub(new Vector2f(TestEntity.High.P));
                        if (world.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                            WallNormal = new Vector2f(-1, 0);
                            HitHighEntityIndex = TestHighEntityIndex;
                        }

                        if (world.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                            WallNormal = new Vector2f(1, 0);
                            HitHighEntityIndex = TestHighEntityIndex;
                        }

                        if (world.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                            WallNormal = new Vector2f(0, -1);
                            HitHighEntityIndex = TestHighEntityIndex;
                        }

                        if (world.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                            WallNormal = new Vector2f(0, 1);
                            HitHighEntityIndex = TestHighEntityIndex;
                        }
                    }
                }
            }
            entity.High.P = new Vector2f(entity.High.P).add(new Vector2f(playerDelta).mul(tMin[0]));
            if (HitHighEntityIndex > 0) {
                entity.High.dP = new Vector2f(entity.High.dP).sub(new Vector2f(WallNormal).mul(new Vector2f(entity.High.dP).dot(WallNormal)));
                playerDelta = new Vector2f(DesiredPosition).sub(new Vector2f(entity.High.P));
                playerDelta = new Vector2f(playerDelta).sub(new Vector2f(WallNormal).mul(new Vector2f(playerDelta).dot(new Vector2f(WallNormal))));

                Entity.HighEntity HitHigh = gameMemory.HighEntities[HitHighEntityIndex];
                Entity.LowEntity HitLow = gameMemory.LowEntities[HitHigh.LowEntityIndex];
            } else {
                break;
            }
        }

        entity.Low.P = world.MapIntoTileSpace(Camera.position, entity.High.P);
    }
}
