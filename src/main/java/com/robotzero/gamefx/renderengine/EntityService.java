package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import org.joml.Vector2f;

public class EntityService {
    private final GameMemory gameMemory;

    public EntityService(GameMemory gameMemory) {
        this.gameMemory = gameMemory;
    }

    public int AddPlayer()
    {
        int EntityIndex = AddLowEntity(EntityType.HERO);
        Entity.LowEntity entity = GetLowEntity(EntityIndex);

        entity.P = new World.WorldPosition(Camera.position);
        entity.P.Offset.x = 0;
        entity.P.Offset.y = 0;
        entity.Height = 1.4f;
        entity.Width = 1.0f;
        entity.Collides = true;

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = EntityIndex;
        }
        return EntityIndex;
    }

    public int AddLowEntity(EntityType Type)
    {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new Entity.LowEntity();
        gameMemory.LowEntities[EntityIndex].Type = Type;

        return(EntityIndex);
    }

    public int AddWall(int ChunkX, int ChunkY)
    {
        int EntityIndex = AddLowEntity(EntityType.WALL);
        Entity.LowEntity entity = GetLowEntity(EntityIndex);

        entity.P.ChunkX = ChunkX;
        entity.P.ChunkY = ChunkY;
        entity.Height = World.TileSideInMeters;
        entity.Width = entity.Height;
        entity.Collides = true;

        return EntityIndex;
    }

    public Entity.LowEntity GetLowEntity(int Index)
    {
        Entity.LowEntity Result = null;

        if((Index > 0) && (Index < gameMemory.LowEntityCount))
        {
            Result = gameMemory.LowEntities[Index];
        }

        return(Result);
    }

    public Entity.HighEntity MakeEntityHighFrequency(int LowIndex)
    {
        Entity.HighEntity EntityHigh = null;

        Entity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];

        if(EntityLow.HighEntityIndex > 0)
        {
            EntityHigh = gameMemory.HighEntities[EntityLow.HighEntityIndex];
        }
        else
        {
            if(gameMemory.HighEntityCount < gameMemory.HighEntities.length)
            {
                int HighIndex = gameMemory.HighEntityCount;
                gameMemory.HighEntityCount = HighIndex + 1;
                EntityHigh = gameMemory.HighEntities[HighIndex];
                if (EntityHigh == null) {
                    EntityHigh = new Entity.HighEntity();
                    gameMemory.HighEntities[HighIndex] = EntityHigh;
                }
                // NOTE(casey): Map the entity into camera space
                World.WorldDifference Diff = World.subtract(EntityLow.P, Camera.position);
                EntityHigh.P = Diff.dXY;
                EntityHigh.dP = new Vector2f(0.0f, 0.0f);
                EntityHigh.LowEntityIndex = LowIndex;

                EntityLow.HighEntityIndex = HighIndex;
            }
            else
            {
                throw new RuntimeException("Invalid code path");
            }
        }

        return(EntityHigh);
    }

    public Entity GetHighEntity(int LowIndex)
    {
        Entity Result = new Entity();

        if((LowIndex > 0) && (LowIndex < gameMemory.LowEntityCount))
        {
            Result.LowIndex = LowIndex;
            Result.Low = gameMemory.LowEntities[LowIndex];
            Result.High = MakeEntityHighFrequency(LowIndex);
        }

        return(Result);
    }

    public void MakeEntityLowFrequency(int LowIndex)
    {
        Entity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];
        int HighIndex = EntityLow.HighEntityIndex;
        if(HighIndex > 0)
        {
            int LastHighIndex = gameMemory.HighEntityCount - 1;
            if(HighIndex != LastHighIndex)
            {
                Entity.HighEntity LastEntity = gameMemory.HighEntities[LastHighIndex];
                Entity.HighEntity DelEntity = gameMemory.HighEntities[HighIndex];

                DelEntity.dP = LastEntity.dP;
                DelEntity.P = LastEntity.P;
                DelEntity.LowEntityIndex = LastEntity.LowEntityIndex;
                gameMemory.LowEntities[LastEntity.LowEntityIndex].HighEntityIndex = HighIndex;
            }

            gameMemory.HighEntityCount = gameMemory.HighEntityCount - 1;
            EntityLow.HighEntityIndex = 0;
        }
    }

    public void OffsetAndCheckFrequencyByArea(Vector2f Offset, Rectangle HighFrequencyBounds)
    {
        for(int EntityIndex = 1; EntityIndex < gameMemory.HighEntityCount;)
        {
            Entity.HighEntity High = gameMemory.HighEntities[EntityIndex];

            High.P = new Vector2f(High.P).add(Offset);
            if(Rectangle.IsInRectangle(HighFrequencyBounds, High.P))
            {
                ++EntityIndex;
            }
            else
            {
                MakeEntityLowFrequency(High.LowEntityIndex);
            }
        }
    }
}
