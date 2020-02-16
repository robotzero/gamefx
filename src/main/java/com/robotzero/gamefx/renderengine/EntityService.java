package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.GameMemory;

public class EntityService {
    private final GameMemory gameMemory;

    public EntityService(GameMemory gameMemory) {
        this.gameMemory = gameMemory;
    }

    public Entity GetEntity(Entity.EntityResidence Residence, int Index)
    {
        Entity entity = new Entity();

        if((Index >= 0) && (Index < gameMemory.entities.length))
            if((Index >= 0) && (Index < gameMemory.entityCount))
            {
                entity.Residence = Residence;
                entity.Dormant = gameMemory.DormantEntities[Index];
                entity.Low = gameMemory.LowEntities[Index];
                entity.High = gameMemory.HighEntities[Index];
            }

        return(entity);
    }

    void ChangeEntityResidence(Entity entity, Entity.EntityResidence Residence)

    {

        // TODO(casey): Implement this!

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

        ChangeEntityResidence(entity, Entity.EntityResidence.High);

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
