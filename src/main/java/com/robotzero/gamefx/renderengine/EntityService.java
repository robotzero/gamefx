package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.TileMap;
import org.joml.Vector2f;

public class EntityService {
    private final GameMemory gameMemory;
    private final TileMap tileMap;
    private final Camera camera;

    public EntityService(GameMemory gameMemory, TileMap tileMap, Camera camera) {
        this.gameMemory = gameMemory;
        this.tileMap = tileMap;
        this.camera = camera;
    }

    public Entity GetEntity(Entity.EntityResidence Residence, int Index)
    {
        Entity entity = new Entity();

        if((Index >= 0) && (Index < gameMemory.entityCount)) {
            if (gameMemory.EntityResidence[Index].ordinal() < Residence.ordinal()) {
                ChangeEntityResidence(Index, Residence);
            }

            entity.Residence = Residence;
            entity.Dormant = gameMemory.DormantEntities[Index];
            entity.Low = gameMemory.LowEntities[Index];
            entity.High = gameMemory.HighEntities[Index];
        }

        return(entity);
    }

    void ChangeEntityResidence(int EntityIndex, Entity.EntityResidence Residence) {

        if (Residence == Entity.EntityResidence.High) {
            if(gameMemory.EntityResidence[EntityIndex] != Entity.EntityResidence.High)
            {
                Entity.HighEntity EntityHigh = gameMemory.HighEntities[EntityIndex];
                Entity.DormantEntity EntityDormant = gameMemory.DormantEntities[EntityIndex];

                TileMap.TileMapDifference Diff = tileMap.subtract(EntityDormant.P, Camera.position);
                EntityHigh.P = Diff.dXY;
                EntityHigh.dP = new Vector2f(0f, 0f);
            }
        }
        gameMemory.EntityResidence[EntityIndex] = Residence;
    }

    public void InitializePlayer(int EntityIndex)
    {
        Entity entity = GetEntity(Entity.EntityResidence.Dormant, EntityIndex);

        entity.Dormant.P.AbsTileX = 1;
        entity.Dormant.P.AbsTileY = 3;
        entity.Dormant.P.Offset.x = 0;
        entity.Dormant.P.Offset.y = 0;
        entity.Dormant.Height = 0.5f; // 1.4f;
        entity.Dormant.Width = 1.0f;
        entity.Dormant.Collides = true;

        ChangeEntityResidence(EntityIndex, Entity.EntityResidence.High);

        if(GetEntity(Entity.EntityResidence.Dormant, gameMemory.CameraFollowingEntityIndex).Residence ==
                Entity.EntityResidence.Nonexistent)
        {
            gameMemory.CameraFollowingEntityIndex = EntityIndex;
        }
    }

    public int AddEntity()
    {
        int EntityIndex = gameMemory.entityCount++;

        gameMemory.EntityResidence[EntityIndex] = Entity.EntityResidence.Dormant;
        gameMemory.LowEntities[EntityIndex] = new Entity.LowEntity();
        gameMemory.DormantEntities[EntityIndex] = new Entity.DormantEntity();
        gameMemory.HighEntities[EntityIndex] = new Entity.HighEntity();

        return(EntityIndex);
    }
}
