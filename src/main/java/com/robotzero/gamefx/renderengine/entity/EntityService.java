package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.translations.MoveSpec;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import com.robotzero.gamefx.world.WorldEntityBlock;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EntityService {
    private final GameMemory gameMemory;
    private final World world;
    public static final float PlayerHeight = 1.4f;
    public static final float PlayerWidth = 1.0f;
    public static final float FamiliarHeight = 0.5f;
    public static final float FamiliarWidth = 1.0f;

    public EntityService(GameMemory gameMemory, World world) {
        this.gameMemory = gameMemory;
        this.world = world;
    }

    public World getWorld() {
        return world;
    }

    public AddLowEntityResult AddPlayer() {
        World.WorldPosition P = Camera.position;
        AddLowEntityResult entity = AddLowEntity(EntityType.HERO, P);

        entity.Low.Sim.Height = 1.4f;
        entity.Low.Sim.Width = 1.0f;
        AddFlag(entity.Low.Sim, SimEntityFlag.COLLIDES);

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = entity.LowIndex;
        }

        return entity;
    }

    public AddLowEntityResult AddLowEntity(EntityType Type, World.WorldPosition P) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new LowEntity();
        gameMemory.LowEntities[EntityIndex].Sim = new SimEntity();
        gameMemory.LowEntities[EntityIndex].Sim.Type = Type;
        gameMemory.LowEntities[EntityIndex].P = null;

        ChangeEntityLocation(EntityIndex, gameMemory.LowEntities[EntityIndex], P);

        AddLowEntityResult addLowEntityResult = new AddLowEntityResult();
        addLowEntityResult.Low = gameMemory.LowEntities[EntityIndex];
        addLowEntityResult.LowIndex = EntityIndex;
        return addLowEntityResult;
    }

    public AddLowEntityResult AddWall(int ChunkX, int ChunkY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(ChunkX, ChunkY);
        AddLowEntityResult entity = AddLowEntity(EntityType.WALL, P);

        entity.Low.Sim.Height = World.TileSideInMeters;
        entity.Low.Sim.Width = entity.Low.Sim.Height;
        AddFlag(entity.Low.Sim, SimEntityFlag.COLLIDES);

        return entity;
    }

    public LowEntity GetLowEntity(int Index) {
        LowEntity Result = null;

        if ((Index > 0) && (Index < gameMemory.LowEntityCount)) {
            Result = gameMemory.LowEntities[Index];
        }

        return (Result);
    }

//    public LowEntity.HighEntity MakeEntityHighFrequency(int LowIndex) {
//        LowEntity.HighEntity EntityHigh = null;
//
//        LowEntity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];
//
//        if (EntityLow.HighEntityIndex > 0) {
//            EntityHigh = gameMemory.HighEntities[EntityLow.HighEntityIndex];
//        } else {
//            Vector2f CameraSpaceP = GetCameraSpaceP(EntityLow);
//            EntityHigh = MakeEntityHighFrequency(EntityLow, LowIndex, CameraSpaceP);
//        }
//
//        return (EntityHigh);
//    }

    public Vector2f GetCameraSpaceP(LowEntity EntityLow) {
        // NOTE(casey): Map the entity into camera space
        World.WorldDifference Diff = World.subtract(EntityLow.P, Camera.position);
        Vector2f Result = Diff.dXY;

        return (Result);
    }

//    public LowEntity.HighEntity MakeEntityHighFrequency(LowEntity.LowEntity EntityLow, int LowIndex, Vector2f CameraSpaceP) {
//        LowEntity.HighEntity EntityHigh = null;
//
//        if (EntityLow.HighEntityIndex == 0) {
//            if (gameMemory.HighEntityCount < gameMemory.HighEntities.length) {
//                int HighIndex = gameMemory.HighEntityCount;
//                gameMemory.HighEntityCount = HighIndex + 1;
//                EntityHigh = gameMemory.HighEntities[HighIndex];
//                if (EntityHigh == null) {
//                    EntityHigh = new LowEntity.HighEntity();
//                }
//                EntityHigh.P = CameraSpaceP;
//                EntityHigh.dP = new Vector2f(0.0f, 0.0f);
//                EntityHigh.LowEntityIndex = LowIndex;
//                gameMemory.HighEntities[HighIndex] = EntityHigh;
//
//                EntityLow.HighEntityIndex = HighIndex;
//            } else {
//                throw new RuntimeException("Invalid code path");
//            }
//        }
//
//        return (EntityHigh);
//    }

//    public LowEntity ForceEntityIntoHigh(int LowIndex) {
//        LowEntity Result = new LowEntity();
//
//        if ((LowIndex > 0) && (LowIndex < gameMemory.LowEntityCount)) {
//            Result.LowIndex = LowIndex;
//            Result.Low = gameMemory.LowEntities[LowIndex];
//            Result.High = MakeEntityHighFrequency(LowIndex);
//        }
//
//        return (Result);
//    }
//
//    public void MakeEntityLowFrequency(int LowIndex) {
//        LowEntity.LowEntity EntityLow = gameMemory.LowEntities[LowIndex];
//        int HighIndex = EntityLow.HighEntityIndex;
//        if (HighIndex > 0) {
//            int LastHighIndex = gameMemory.HighEntityCount - 1;
//            if (HighIndex != LastHighIndex) {
//                LowEntity.HighEntity LastEntity = gameMemory.HighEntities[LastHighIndex];
//                LowEntity.HighEntity DelEntity = gameMemory.HighEntities[HighIndex];
//
//                DelEntity.dP = LastEntity.dP;
//                DelEntity.P = LastEntity.P;
//                DelEntity.LowEntityIndex = LastEntity.LowEntityIndex;
//                gameMemory.LowEntities[LastEntity.LowEntityIndex].HighEntityIndex = HighIndex;
//                gameMemory.HighEntities[HighIndex] = DelEntity;
//            }
//
//            gameMemory.HighEntityCount = gameMemory.HighEntityCount - 1;
//            EntityLow.HighEntityIndex = 0;
//        }
//    }
//
//    public void OffsetAndCheckFrequencyByArea(Vector2f Offset, Rectangle HighFrequencyBounds) {
//        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ) {
//            LowEntity.HighEntity High = gameMemory.HighEntities[HighEntityIndex];
//            LowEntity.LowEntity Low = gameMemory.LowEntities[High.LowEntityIndex];
//
//            High.P = new Vector2f(High.P).add(Offset);
//            if (Low.P != null && Rectangle.IsInRectangle(HighFrequencyBounds, High.P)) {
//                ++HighEntityIndex;
//            } else {
//                MakeEntityLowFrequency(High.LowEntityIndex);
//            }
//        }
//    }

    public World.WorldPosition ChunkPositionFromTilePosition(int AbsTileX, int AbsTileY) {
        World.WorldPosition Result = new World.WorldPosition();
        Result.ChunkX = AbsTileX / World.TILES_PER_CHUNK;
        Result.ChunkY = AbsTileY / World.TILES_PER_CHUNK;

        if (AbsTileX < 0) {
            Result.ChunkX = Result.ChunkX - 1;
        }

        if (AbsTileY < 0) {
            Result.ChunkY = Result.ChunkY - 1;
        }

        Result.Offset = new Vector2f((AbsTileX - World.TILES_PER_CHUNK / 2f - (Result.ChunkX * World.TILES_PER_CHUNK)) * World.TileSideInMeters,
                (AbsTileY - World.TILES_PER_CHUNK / 2f - (Result.ChunkY * World.TILES_PER_CHUNK)) * World.TileSideInMeters);

        assert (world.IsCanonical(Result.Offset));
        return (Result);
    }

    public void ChangeEntityLocation(int LowEntityIndex, LowEntity LowEntity, World.WorldPosition NewPInit) {
        World.WorldPosition OldP = null;
        World.WorldPosition NewP = null;

        if (!IsSet(LowEntity.Sim, SimEntityFlag.NONSPATIAL) && LowEntity.P != null) {
            OldP = new World.WorldPosition(LowEntity.P);
        }

        if (NewPInit != null) {
            NewP = new World.WorldPosition(NewPInit);
        }

        ChangeEntityLocationRaw(LowEntityIndex, OldP, NewP);

        if (NewP != null) {
            LowEntity.P = new World.WorldPosition(NewP);
            ClearFlag(LowEntity.Sim, SimEntityFlag.NONSPATIAL);
        } else {
            LowEntity.P = null;
            AddFlag(LowEntity.Sim, SimEntityFlag.NONSPATIAL);
        }
    }

    public void ChangeEntityLocationRaw(int LowEntityIndex, World.WorldPosition OldP, World.WorldPosition NewP) {

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
                                        Integer nextN = First.next;
                                        WorldEntityBlock nextBlock = FirstBlock.get(nextN);
                                        WorldEntityBlock firstBlock = new WorldEntityBlock();
                                        int[] tmp = new int[16];
                                        System.arraycopy(nextBlock.LowEntityIndex, 0, tmp, 0, nextBlock.LowEntityIndex.length);
                                        firstBlock.LowEntityIndex = tmp;
                                        firstBlock.next = nextBlock.next;
                                        firstBlock.EntityCount = nextBlock.EntityCount;
                                        nextBlock.next = World.firstFree;
                                        FirstBlock.set(0, firstBlock);
                                        FirstBlock.addLast(nextBlock);
                                        World.firstFree = FirstBlock.indexOf(nextBlock);
                                    }
                                    NotFound = false;
                                }
                            }
                        }
                    }
                }
            }

            if (NewP != null) {
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

//    public SimEntity EntityFromHighIndex(int HighEntityIndex) {
//        SimEntity Result = null;
//
//        if (HighEntityIndex > 0) {
//            assert(HighEntityIndex < gameMemory.HighEntities.length);
//            SimEntity.HighEntity High = gameMemory.HighEntities[HighEntityIndex];
//            if (High != null) {
//                Result = new SimEntity();
//                Result.High = High;
//                Result.LowIndex = Result.High.LowEntityIndex;
//                Result.Low = gameMemory.LowEntities[Result.LowIndex];
//            }
//        }
//
//        return(Result);
//    }

    public AddLowEntityResult AddMonstar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY);
        AddLowEntityResult Entity = AddLowEntity(EntityType.MONSTAR, P);

        Entity.Low.Sim.Height = 0.5f;
        Entity.Low.Sim.Width = 1.0f;
        AddFlag(Entity.Low.Sim, SimEntityFlag.COLLIDES);

        return(Entity);
    }

    public AddLowEntityResult AddFamiliar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY);
        AddLowEntityResult Entity = AddLowEntity(EntityType.FAMILIAR, P);

        Entity.Low.Sim.Height = 0.5f;
        Entity.Low.Sim.Width = 1.0f;
        AddFlag(Entity.Low.Sim, SimEntityFlag.COLLIDES);

        return(Entity);
    }

    public void UpdateFamiliar(SimRegion simRegion, SimEntity Entity, float dt) {
        SimEntity ClosestHero = null;
        float ClosestHeroDSq = (float) Math.pow(10.0f, 2);
            SimEntity TestEntity = simRegion.simEntities[0];
            //@TODO huh, index is moved but not looping through all entities
            for (int testEntityIndex = 0; testEntityIndex < simRegion.EntityCount; ++testEntityIndex) {
                assert (TestEntity != null);
                if (TestEntity.Type == EntityType.HERO) {
                    float TestDSq = new Vector2f(TestEntity.P).sub(new Vector2f(Entity.P)).lengthSquared();
                    if (TestEntity.Type == EntityType.HERO) {
                        TestDSq *= 0.75f;
                    }

                    if (ClosestHeroDSq > TestDSq) {
                        ClosestHero = TestEntity;
                        ClosestHeroDSq = TestDSq;
                    }
                }
            }

        Vector2f ddP = new Vector2f();
        if (ClosestHero != null && ClosestHeroDSq > Math.pow(3.0f, 2.0f)) {
            float Acceleration = 0.5f;
            float OneOverLength = (float) (Acceleration / Math.sqrt(ClosestHeroDSq));
            ddP = new Vector2f(ClosestHero.P).sub(new Vector2f(Entity.P)).mul(OneOverLength);
        }

        MoveSpec moveSpec = new MoveSpec();
        moveSpec.UnitMaxAccelVector = true;
        moveSpec.Speed = GameApp.playerSpeed;
        moveSpec.Drag = 8.0f;

        moveEntity(simRegion, Entity, ddP, dt, moveSpec);
    }

    public void UpdateMonstar(SimEntity entity, float dt) {
    }

    public MoveSpec DefaultMoveSpec() {
        MoveSpec moveSpec = new MoveSpec();
        moveSpec.UnitMaxAccelVector = false;
        moveSpec.Speed = 1.0f;
        moveSpec.Drag = 0.0f;

        return moveSpec;
    }

    public void moveEntity(SimRegion simRegion, SimEntity entity, Vector2f ddP, float interval, MoveSpec moveSpec) {
        assert(!IsSet(entity, SimEntityFlag.NONSPATIAL));

        if (moveSpec.UnitMaxAccelVector) {
            float ddPLength = new Vector2f(ddP).lengthSquared();
            if (ddPLength > 1.0f) {
                ddP = new Vector2f(ddP.x(), ddP.y()).mul((float) (1.0f / Math.sqrt(ddPLength)));
            }
        }

        ddP = ddP.mul(moveSpec.Speed);
        ddP = ddP.add(new Vector2f(entity.dP.x(), entity.dP.y()).mul(-moveSpec.Drag));

        Vector2f OldPlayerP = new Vector2f(entity.P);
        Vector2f playerDelta = new Vector2f(ddP.x(), ddP.y()).mul(0.5f).mul(interval * interval).add(new Vector2f(entity.dP.x(), entity.dP.y()).mul(interval));
        entity.dP = new Vector2f(ddP).mul(interval).add(new Vector2f(entity.dP));
        Vector2f NewPlayerP = OldPlayerP.add(playerDelta);

        for (int Iteration = 0; (Iteration < 4); ++Iteration) {
            float[] tMin = {1.0f, 0.0f};
            Vector2f WallNormal = new Vector2f(0.0f, 0.0f);
            int HitHighEntityIndex = 0;
            Vector2f DesiredPosition = new Vector2f(entity.P).add(playerDelta);

            if (entity.flags.contains(SimEntityFlag.COLLIDES) && !entity.flags.contains(SimEntityFlag.NONSPATIAL)) {
                for (int TestHighEntityIndex = 1; TestHighEntityIndex < simRegion.EntityCount; ++TestHighEntityIndex) {
                    SimEntity TestEntity = simRegion.simEntities[TestHighEntityIndex];
                    if (TestEntity != entity) {
                        if (TestEntity.flags.contains(SimEntityFlag.COLLIDES) && !entity.flags.contains(SimEntityFlag.NONSPATIAL)) {
                            float DiameterW = TestEntity.Width + entity.Width;
                            float DiameterH = TestEntity.Height + entity.Height;
                            Vector2f MinCorner = new Vector2f(DiameterW, DiameterH).mul(-0.5f);
                            Vector2f MaxCorner = new Vector2f(DiameterW, DiameterH).mul(0.5f);
                            Vector2f Rel = new Vector2f(entity.P).sub(new Vector2f(TestEntity.P));
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
            }
            entity.P = new Vector2f(entity.P).add(new Vector2f(playerDelta).mul(tMin[0]));
            if (HitHighEntityIndex > 0) {
                entity.dP = new Vector2f(entity.dP).sub(new Vector2f(WallNormal).mul(new Vector2f(entity.dP).dot(WallNormal)));
                playerDelta = new Vector2f(DesiredPosition).sub(new Vector2f(entity.P));
                playerDelta = new Vector2f(playerDelta).sub(new Vector2f(WallNormal).mul(new Vector2f(playerDelta).dot(new Vector2f(WallNormal))));

            } else {
                break;
            }
        }
    }

    void pushPiece(EntityVisiblePieceGroup group, Vector2f offset, Vector2f align, Vector2f Dim, Vector4f Color, float EntityZC) {
        assert(group.PieceCount < group.Pieces.length);

        EntityVisiblePiece piece = group.Pieces[group.PieceCount];
        if (piece == null) {
            piece = new EntityVisiblePiece();
        }
        piece.Offset = new Vector2f(offset.x(), -offset.y()).mul(World.MetersToPixels).sub(align);
        piece.EntityZC = EntityZC;
        piece.Color = Color;
        piece.Dim = Dim;
        group.Pieces[group.PieceCount] = piece;
        group.PieceCount = group.PieceCount + 1;
    }

    public Map<EntityType, List<Map.Entry<EntityType, Matrix4f>>> getModelMatrix() {
        EntityVisiblePieceGroup pieceGroup = new EntityVisiblePieceGroup();

        if (gameMemory.simRegion == null) {
            return Map.of();
        }
        return IntStream.range(1, gameMemory.simRegion.EntityCount).mapToObj(HighEntityIndex -> {
            pieceGroup.PieceCount = 0;
            SimEntity entity = gameMemory.simRegion.simEntities[HighEntityIndex];

            switch (entity.Type.name().toLowerCase()) {
                case ("wall") : {
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(40f, 80f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("hero") : {
                    MoveSpec MoveSpec = DefaultMoveSpec();
                    MoveSpec.UnitMaxAccelVector = true;
                    MoveSpec.Speed = 50.0f;
                    MoveSpec.Drag = 8.0f;
                    moveEntity(gameMemory.simRegion, entity, gameMemory.ControlledHero.ddP ,GameApp.globalinterval, MoveSpec);
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("familiar"): {
                    UpdateFamiliar(gameMemory.simRegion, entity, GameApp.globalinterval);
                    entity.tBob = entity.tBob + GameApp.globalinterval;
                    if (entity.tBob > 2.0f * Math.PI) {
                        entity.tBob = (float) (entity.tBob - (2.0f * Math.PI));
                    }
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("monstar"): {

                } break;
                default: {
                    throw new RuntimeException("INVALID PATH");
                }
            }

            final Matrix4f v = new Matrix4f();
            float EntityGroundPointX = World.ScreenCenterX + World.MetersToPixels * entity.P.x();
            float EntityGroundPointY = World.ScreenCenterY - World.MetersToPixels * entity.P.y();
//            float PlayerLeft = EntityGroundPointX - 0.5f * World.MetersToPixels * lowEntity.Width;
//            float PlayerTop = EntityGroundPointY - 0.5f * World.MetersToPixels * lowEntity.Height;

            EntityVisiblePiece Piece = pieceGroup.Pieces[0];
            Vector2f Center = new Vector2f(EntityGroundPointX + Piece.Offset.x(), EntityGroundPointY + Piece.Offset.y());
            Vector2f HalfDim = Piece.Dim.mul(0.5f * World.MetersToPixels, new Vector2f(0, 0f));

            return Map.of(entity.Type, v.identity().translate(new Vector3f(Center.sub(HalfDim).x(), Center.add(HalfDim).y(), 0f)));

//            return Map.of(lowEntity.Type, v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f)));
        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
            return a.getKey();
        }));
    }

    public Vector2f GetSimSpaceP(SimRegion simRegion, LowEntity stored) {
        Vector2f Result = GameMemory.InvalidP;
        if (IsSet(stored.Sim, SimEntityFlag.NONSPATIAL)) {
            World.WorldDifference Diff = World.subtract(stored.P, simRegion.Origin);
            return Diff.dXY;
        }

        return Result;
    }

    public SimEntity AddEntity(SimRegion simRegion, int StorageIndex, LowEntity Source, Vector2f SimP) {
        SimEntity Dest = AddEntityRaw(simRegion, StorageIndex, Source);
        if (Dest != null) {
            if (SimP != null) {
                Dest.P = new Vector2f(SimP);
            } else {
                Dest.P = GetSimSpaceP(simRegion, Source);
            }
        }

        return Dest;
    }

    SimEntity AddEntityRaw(SimRegion simRegion, int StorageIndex, LowEntity Source) {
        SimEntity Entity = null;

        SimEntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        if (Entry.Ptr == null) {
            if (simRegion.EntityCount < simRegion.MaxEntityCount) {
                int EntityCount = simRegion.EntityCount;
                Entity = simRegion.simEntities[EntityCount];
                simRegion.EntityCount = simRegion.EntityCount + 1;
                if (Entity == null) {
                    Entity = new SimEntity();
                    simRegion.simEntities[EntityCount] = Entity;
                }

                Entry.Index = StorageIndex;
                Entry.Ptr = Entity;

                if (Source != null) {
                    // TODO(casey): This should really be a decompression step, not
                    // a copy!
                    Entity = new SimEntity(Source.Sim);
                    simRegion.simEntities[EntityCount] = Entity;

                    assert (!IsSet(Source.Sim, SimEntityFlag.SIMMING));
                    AddFlag(Source.Sim, SimEntityFlag.SIMMING);
//                LoadEntityReference(simRegion, Entity.Sword);
                }

                Entity.StorageIndex = StorageIndex;
            } else {
                throw new RuntimeException("Invalid code path");
            }
        }

        return(Entity);
    }

    public void MapStorageIndexToEntity(SimRegion simRegion, int StorageIndex, SimEntity Entity) {
        SimEntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        assert((Entry.Index == 0) || (Entry.Index == StorageIndex));
        Entry.Index = StorageIndex;
        Entry.Ptr = Entity;
    }

    public SimEntity GetEntityByStorageIndex(SimRegion simRegion, int StorageIndex) {
        SimEntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        SimEntity Result = Entry.Ptr;
        return(Result);
    }

    public SimEntityHash GetHashFromStorageIndex(SimRegion simRegion, int StorageIndex) {
        assert (StorageIndex != 0);

        SimEntityHash Result = null;

        int HashValue = StorageIndex;
        for (int Offset = 0; Offset < simRegion.Hash.length; ++Offset) {
            int HashMask = simRegion.Hash.length - 1;
            int HashIndex = ((HashValue + Offset) & HashMask);
            SimEntityHash Entry = simRegion.Hash[HashIndex];
            if (Entry == null) {
                simRegion.Hash[HashIndex] = new SimEntityHash();
                Entry = simRegion.Hash[HashIndex];
            }
            if ((Entry.Index == 0) || (Entry.Index == StorageIndex)) {
                Result = Entry;
                break;
            }
        }

        return(Result);
    }

//    public void LoadEntityReference(SimRegion simRegion, EntityReference Ref) {
//        if (Ref.Index != 0) {
//            SimEntityHash Entry = GetHashFromStorageIndex(simRegion, Ref.Index);
//            if (Entry.Ptr == null) {
//                Entry.Index = Ref.Index;
//                Entry.Ptr = AddEntity(simRegion, Ref.Index, GetLowEntity(Ref.Index));
//            }
//
//            Ref.Ptr = Entry.Ptr;
//        }
//    }

    public void StoreEntityReference(EntityReference Ref) {
        if (Ref.Ptr != null) {
            Ref.Index = Ref.Ptr.StorageIndex;
        }
    }

    public SimRegion BeginSim(World.WorldPosition Origin, Rectangle Bounds) {
        SimRegion simRegion = new SimRegion();
        simRegion.Origin = Origin;
        simRegion.Bounds = Bounds;

        simRegion.MaxEntityCount = 4096;
        simRegion.EntityCount = 0;
        simRegion.simEntities = new SimEntity[simRegion.MaxEntityCount];

        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMinCorner(simRegion.Bounds));
        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMaxCorner(simRegion.Bounds));

        for(int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
            for(int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
                WorldChunk Chunk = world.GetWorldChunk(ChunkX, ChunkY, false);
                if (Chunk != null) {
                    LinkedList<WorldEntityBlock> FirstBlock = Chunk.getFirstBlock();
                    WorldEntityBlock First = Chunk.getFirstBlock().get(0);
                    for (WorldEntityBlock Block = First; Block != null; Block = Block.next == null ? null : Chunk.getFirstBlock().get(Block.next)) {
                        for(int EntityIndexIndex = 0; EntityIndexIndex < Block.EntityCount; ++EntityIndexIndex) {
                            int LowEntityIndex = Block.LowEntityIndex[EntityIndexIndex];
                            LowEntity Low = gameMemory.LowEntities[LowEntityIndex];
                            if (!IsSet(Low.Sim, SimEntityFlag.NONSPATIAL)) {
                                Vector2f SimSpaceP = GetSimSpaceP(simRegion, Low);
                                if (Rectangle.IsInRectangle(simRegion.Bounds, SimSpaceP))
                                {
                                    AddEntity(simRegion, LowEntityIndex, Low, SimSpaceP);
                                }
                            }
                        }
                    }
                }
            }
        }
        return simRegion;
    }

    public void EndSim(SimRegion Region) {
        if (Region != null) {
            SimEntity Entity = Region.simEntities[0];

            for(int EntityIndex = 0; EntityIndex < Region.EntityCount; ++EntityIndex, Entity = Region.simEntities[EntityIndex]) {
                LowEntity Stored = gameMemory.LowEntities[Entity.StorageIndex];
                assert (IsSet(Stored.Sim, SimEntityFlag.SIMMING));
                Stored.Sim = new SimEntity(Entity);
                assert (!IsSet(Stored.Sim, SimEntityFlag.SIMMING));
                //StoreEntityReference(Stored.sim.sword);

                World.WorldPosition NewP = IsSet(Entity, SimEntityFlag.NONSPATIAL) ? null : world.MapIntoChunkSpace(Region.Origin, Entity.P);
                ChangeEntityLocation(Entity.StorageIndex, Stored, NewP);

                if (Entity.StorageIndex == gameMemory.CameraFollowingEntityIndex) {
                    World.WorldPosition NewCameraP = new World.WorldPosition(Camera.position);
                    NewCameraP = new World.WorldPosition(Stored.P);
                    Camera.position = NewCameraP;
                }
            }
        }
    }

    public boolean IsSet(SimEntity Entity, SimEntityFlag Flag) {
        return Entity.flags.contains(Flag);
    }

    public void AddFlag(SimEntity Entity, SimEntityFlag Flag) {
        Entity.flags.add(Flag);
    }

    public void ClearFlag(SimEntity Entity, SimEntityFlag Flag) {
        Entity.flags.remove(Flag);
    }

    public void MakeEntityNonSpatial(SimEntity Entity) {
        AddFlag(Entity, SimEntityFlag.NONSPATIAL);
        Entity.P = GameMemory.InvalidP;
    }

    public void MakeEntitySpatial(SimEntity Entity, Vector2f P, Vector2f dP) {
        ClearFlag(Entity, SimEntityFlag.NONSPATIAL);
        Entity.P = P;
        Entity.dP = dP;
    }
}
