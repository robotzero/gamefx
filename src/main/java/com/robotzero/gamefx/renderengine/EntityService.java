package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import com.robotzero.gamefx.world.WorldEntityBlock;
import org.joml.Vector2f;

import java.util.LinkedList;

public class EntityService {
    private final GameMemory gameMemory;
    private final World world;

    public EntityService(GameMemory gameMemory, World world) {
        this.gameMemory = gameMemory;
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public int AddPlayer() {
        World.WorldPosition P = Camera.position;
        int EntityIndex = AddLowEntity(EntityType.HERO, P);
        Entity.LowEntity entity = GetLowEntity(EntityIndex);

        entity.Height = 1.4f;
        entity.Width = 1.0f;
        entity.Collides = true;

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = EntityIndex;
        }
        return EntityIndex;
    }

    public int AddLowEntity(EntityType Type, World.WorldPosition P) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new Entity.LowEntity();
        gameMemory.LowEntities[EntityIndex].Type = Type;

        //@TODO
        if (P != null) {
            gameMemory.LowEntities[EntityIndex].P = new World.WorldPosition(P);
            ChangeEntityLocation(EntityIndex, null, P);
        }

        return (EntityIndex);
    }

    public int AddWall(int ChunkX, int ChunkY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(ChunkX, ChunkY);
        int EntityIndex = AddLowEntity(EntityType.WALL, P);

        Entity.LowEntity entity = GetLowEntity(EntityIndex);

        entity.Height = World.TileSideInMeters;
        entity.Width = entity.Height;
        entity.Collides = true;

        return EntityIndex;
    }

    public Entity.LowEntity GetLowEntity(int Index) {
        Entity.LowEntity Result = null;

        if ((Index > 0) && (Index < gameMemory.LowEntityCount)) {
            Result = gameMemory.LowEntities[Index];
        }

        return (Result);
    }

    public Entity.HighEntity MakeEntityHighFrequency(int LowIndex) {
        Entity.HighEntity EntityHigh = null;

        Entity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];

        if (EntityLow.HighEntityIndex > 0) {
            EntityHigh = gameMemory.HighEntities[EntityLow.HighEntityIndex];
        } else {
            Vector2f CameraSpaceP = GetCameraSpaceP(EntityLow);
            EntityHigh = MakeEntityHighFrequency(EntityLow, LowIndex, CameraSpaceP);
        }

        return (EntityHigh);
    }

    public Vector2f GetCameraSpaceP(Entity.LowEntity EntityLow) {
        // NOTE(casey): Map the entity into camera space
        World.WorldDifference Diff = World.subtract(EntityLow.P, Camera.position);
        Vector2f Result = Diff.dXY;

        return (Result);
    }

    public Entity.HighEntity MakeEntityHighFrequency(Entity.LowEntity EntityLow, int LowIndex, Vector2f CameraSpaceP) {
        Entity.HighEntity EntityHigh = null;

        if (EntityLow.HighEntityIndex == 0) {
            if (gameMemory.HighEntityCount < gameMemory.HighEntities.length) {
                int HighIndex = gameMemory.HighEntityCount;
                gameMemory.HighEntityCount = HighIndex + 1;
                EntityHigh = gameMemory.HighEntities[HighIndex];
                if (EntityHigh == null) {
                    EntityHigh = new Entity.HighEntity();
                }
                EntityHigh.P = CameraSpaceP;
                EntityHigh.dP = new Vector2f(0.0f, 0.0f);
                EntityHigh.LowEntityIndex = LowIndex;
                gameMemory.HighEntities[HighIndex] = EntityHigh;

                EntityLow.HighEntityIndex = HighIndex;
            } else {
                throw new RuntimeException("Invalid code path");
            }
        }

        return (EntityHigh);
    }

    public Entity GetHighEntity(int LowIndex) {
        Entity Result = new Entity();

        if ((LowIndex > 0) && (LowIndex < gameMemory.LowEntityCount)) {
            Result.LowIndex = LowIndex;
            Result.Low = gameMemory.LowEntities[LowIndex];
            Result.High = MakeEntityHighFrequency(LowIndex);
        }

        return (Result);
    }

    public void MakeEntityLowFrequency(int LowIndex) {
        Entity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];
        int HighIndex = EntityLow.HighEntityIndex;
        if (HighIndex > 0) {
            int LastHighIndex = gameMemory.HighEntityCount - 1;
            if (HighIndex != LastHighIndex) {
                Entity.HighEntity LastEntity = gameMemory.HighEntities[LastHighIndex];
                Entity.HighEntity DelEntity = gameMemory.HighEntities[HighIndex];

                DelEntity.dP = LastEntity.dP;
                DelEntity.P = LastEntity.P;
                DelEntity.LowEntityIndex = LastEntity.LowEntityIndex;
                gameMemory.LowEntities[LastEntity.LowEntityIndex].HighEntityIndex = HighIndex;
                gameMemory.HighEntities[HighIndex] = DelEntity;
            }

            gameMemory.HighEntityCount = gameMemory.HighEntityCount - 1;
            EntityLow.HighEntityIndex = 0;
        }
    }

    public void OffsetAndCheckFrequencyByArea(Vector2f Offset, Rectangle HighFrequencyBounds) {
        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ) {
            Entity.HighEntity High = gameMemory.HighEntities[HighEntityIndex];

            High.P = new Vector2f(High.P).add(Offset);
            if (Rectangle.IsInRectangle(HighFrequencyBounds, High.P)) {
                ++HighEntityIndex;
            } else {
                MakeEntityLowFrequency(High.LowEntityIndex);
            }
        }
    }

    public World.WorldPosition ChunkPositionFromTilePosition(int AbsTileX, int AbsTileY) {
        World.WorldPosition Result = new World.WorldPosition();
        Result.ChunkX = AbsTileX / World.TILES_PER_CHUNK;
        Result.ChunkY = AbsTileY / World.TILES_PER_CHUNK;
        Result.Offset = new Vector2f((AbsTileX - (Result.ChunkX * World.TILES_PER_CHUNK)) * World.TileSideInMeters,
                (AbsTileY - (Result.ChunkY * World.TILES_PER_CHUNK)) * World.TileSideInMeters);
        return (Result);
    }

    public boolean ValidateEntityPairs() {
        boolean Valid = true;

        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ++HighEntityIndex) {
            Entity.HighEntity High = gameMemory.HighEntities[HighEntityIndex];
            Valid = Valid && (gameMemory.LowEntities[High.LowEntityIndex].HighEntityIndex == HighEntityIndex);
        }

        return (Valid);
    }

    public void ChangeEntityLocation(int LowEntityIndex, World.WorldPosition OldP, World.WorldPosition NewP) {
        if (OldP != null && world.AreInSameChunk(OldP, NewP)) {
            // NOTE(casey): Leave entity where it is
        } else {
            if (OldP != null) {
                // NOTE(casey): Pull the entity out of its old entity block
                WorldChunk Chunk = world.GetWorldChunk(OldP.ChunkX, OldP.ChunkY, false);

                if (Chunk != null) {
                    boolean NotFound = true;
                    LinkedList<WorldEntityBlock> FirstBlock = Chunk.getFirstBlock();
                    WorldEntityBlock First = Chunk.getFirstBlock().get(0);
                    for (WorldEntityBlock Block = First; NotFound && Block != null; Block = Block.next == null ? null : Chunk.getFirstBlock().get(Block.next)) {
                        for (int Index = 0; Index < Block.EntityCount && NotFound; ++Index) {
                            if (Block.LowEntityIndex[Index] == LowEntityIndex) {
                                First.EntityCount = First.EntityCount - 1;
                                Block.LowEntityIndex[Index] = First.LowEntityIndex[First.EntityCount];
                                if (First.EntityCount == 0) {
                                    if (First.next != null) {
                                    }
                                    //@TODO potentially remove
                                    NotFound = false;
                                }
                            }
                        }
                    }
                }
            }

            // NOTE(casey): Insert the entity into its new entity block
            WorldChunk Chunk = world.GetWorldChunk(NewP.ChunkX, NewP.ChunkY, true);
            WorldEntityBlock Block = Chunk.getFirstBlock().get(0);
            LinkedList<WorldEntityBlock> blah = Chunk.getFirstBlock();
            if (Block.EntityCount == Block.LowEntityIndex.length) {
                //@TODO reuse stuff
                WorldEntityBlock oldBlock;
                if (World.firstFree != null) {
                    oldBlock = blah.get(World.firstFree);
                    World.firstFree = oldBlock.next;
                } else {
                    oldBlock = new WorldEntityBlock();
                }
                oldBlock.EntityCount = Block.EntityCount;
                int[] tmp = new int[16];
                System.arraycopy(Block.LowEntityIndex, 0, tmp, 0, Block.LowEntityIndex.length);
                oldBlock.LowEntityIndex = tmp;
                WorldEntityBlock next = new WorldEntityBlock();
                WorldEntityBlock bn = Block.next != null ? blah.get(Block.next) : null;
                next.EntityCount = bn != null ? bn.EntityCount : 0;
                next.next = bn != null ? bn.next : null;
                tmp = new int[16];
                System.arraycopy(bn != null ? bn.LowEntityIndex : new int[16], 0, tmp, 0, bn != null ? bn.LowEntityIndex.length : 16);
                next.LowEntityIndex = tmp;
                Block.EntityCount = 0;
                Block.LowEntityIndex = new int[16];
                blah.addLast(oldBlock);
                blah.addLast(next);
                int oldIndex = blah.indexOf(oldBlock);
                int nIndex = blah.indexOf(next);
                oldBlock.next = nIndex;
                Block.next = oldIndex;
            }
            Block.LowEntityIndex[Block.EntityCount] = LowEntityIndex;
            Block.EntityCount = Block.EntityCount + 1;
        }
    }
}
