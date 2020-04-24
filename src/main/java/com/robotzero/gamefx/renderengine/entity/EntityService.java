package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.rendergroup.LoadedBitmap;
import com.robotzero.gamefx.renderengine.rendergroup.RenderBasis;
import com.robotzero.gamefx.renderengine.rendergroup.RenderEntryCoordinateSystem;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroup;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupEntryType;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
import com.robotzero.gamefx.renderengine.translations.MoveSpec;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import com.robotzero.gamefx.world.WorldEntityBlock;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class EntityService {
    private final GameMemory gameMemory;
    private final World world;
    private final RenderGroupService renderGroupService;
    private final ExecutorService executorService;
    public static final float PlayerHeight = 1.0f;
    public static final float PlayerWidth = 1.0f;
    public static final float FamiliarHeight = 0.5f;
    public static final float FamiliarWidth = 1.0f;

    public EntityService(GameMemory gameMemory, World world, RenderGroupService renderGroupService, ExecutorService executorService) {
        this.gameMemory = gameMemory;
        this.world = world;
        this.renderGroupService = renderGroupService;
        this.executorService = executorService;
    }

    public World getWorld() {
        return world;
    }

    public AddLowEntityResult AddPlayer() {
        World.WorldPosition P = Camera.position;
        AddLowEntityResult entity = AddGroundedEntity(EntityType.HERO, P, null, SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = entity.LowIndex;
        }

        return entity;
    }

    public AddLowEntityResult AddLowEntity(EntityType Type, World.WorldPosition P, SimEntityCollisionVolumeGroup Collision, SimEntityFlag ...flags) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new LowEntity();
        gameMemory.LowEntities[EntityIndex].Sim = new SimEntity();
        gameMemory.LowEntities[EntityIndex].Sim.Type = Type;
        gameMemory.LowEntities[EntityIndex].Sim.P = new Vector3f();
        gameMemory.LowEntities[EntityIndex].Sim.Collision = Collision;
        gameMemory.LowEntities[EntityIndex].P = NullPosition();
        if (flags != null && flags.length > 0) {
            for (int i = 0; i < flags.length; ++i) {
                AddFlag(gameMemory.LowEntities[EntityIndex].Sim, flags[i]);
            }
        }

        ChangeEntityLocation(EntityIndex, gameMemory.LowEntities[EntityIndex], P);

        AddLowEntityResult addLowEntityResult = new AddLowEntityResult();
        addLowEntityResult.Low = gameMemory.LowEntities[EntityIndex];
        addLowEntityResult.LowIndex = EntityIndex;
        return addLowEntityResult;
    }

    public AddLowEntityResult AddWall(int ChunkX, int ChunkY) {
        Vector3f Dim = new Vector3f(World.TileSideInMeters, World.TileSideInMeters, World.TileDepthInMeters);
        World.WorldPosition P = ChunkPositionFromTilePosition(ChunkX, ChunkY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult entity = AddGroundedEntity(EntityType.WALL, P, null, SimEntityFlag.COLLIDES);

        return entity;
    }

    public World.WorldPosition ChunkPositionFromTilePosition(int AbsTileX, int AbsTileY, Vector3f AdditionalOffset) {
        World.WorldPosition BasePos = new World.WorldPosition();

        Vector3f Offset =  new Vector3f(AbsTileX, AbsTileY, 0.0f).mul(World.TileSideInMeters).add(AdditionalOffset);
        World.WorldPosition Result = world.MapIntoChunkSpace(BasePos, Offset);
        assert (world.IsCanonical(Result.Offset));
        return (Result);
    }

    public void ChangeEntityLocation(int LowEntityIndex, LowEntity LowEntity, World.WorldPosition NewPInit) {
        World.WorldPosition OldP = null;
        World.WorldPosition NewP = null;

        if (!IsSet(LowEntity.Sim, SimEntityFlag.NONSPATIAL) && isValid(LowEntity.P)) {
            OldP = new World.WorldPosition(LowEntity.P);
        }

        if (isValid(NewPInit)) {
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

    public void
    ChangeEntityLocationRaw(int LowEntityIndex, World.WorldPosition OldP, World.WorldPosition NewP) {
        assert (OldP == null || isValid(OldP));
        assert (NewP == null || isValid(NewP));

        if (OldP != null && NewP != null && world.AreInSameChunk(OldP, NewP)) {
            // NOTE(casey): Leave entity where it is
        } else {
            if (OldP != null) {
                // NOTE(casey): Pull the entity out of its old entity block
                WorldChunk Chunk = world.GetWorldChunk(OldP.ChunkX, OldP.ChunkY, false);

                if (Chunk != null) {
                    boolean NotFound = true;
                    LinkedList<WorldEntityBlock> FirstBlock = Chunk.getFirstBlock(false);
                    WorldEntityBlock First = FirstBlock.getFirst();
                    Iterator<WorldEntityBlock> iterator = FirstBlock.listIterator();
                    while(iterator.hasNext() && NotFound) {
                        WorldEntityBlock Block = iterator.next();
                        for (int Index = 0; Index < Block.EntityCount && NotFound; ++Index) {
                            if (Block.LowEntityIndex[Index] == LowEntityIndex) {
                                First.EntityCount = First.EntityCount - 1;
                                Block.LowEntityIndex[Index] = First.LowEntityIndex[First.EntityCount];
                                if (First.EntityCount == 0) {
                                    if (FirstBlock.listIterator(FirstBlock.indexOf(First)).hasNext()) {
                                        WorldEntityBlock FirstFree = FirstBlock.remove(FirstBlock.indexOf(First));
                                        World.firstFree = FirstFree;
                                    }
                                }
                                NotFound = false;
                            }
                        }
                    }
                }
            }

            if (NewP != null) {
                // NOTE(casey): Insert the entity into its new entity block
                WorldChunk Chunk = world.GetWorldChunk(NewP.ChunkX, NewP.ChunkY, true);
                WorldEntityBlock Block = Chunk.getFirstBlock(true).getFirst();
                LinkedList<WorldEntityBlock> blah = Chunk.getFirstBlock(false);
                if (Block.EntityCount == Block.LowEntityIndex.length) {
                    //@TODO reuse stuff
                    WorldEntityBlock oldBlock;
                    if (World.firstFree != null) {
                        oldBlock = World.firstFree;
                        World.firstFree = null;
                    } else {
                        oldBlock = new WorldEntityBlock();
                    }
                    oldBlock.EntityCount = Block.EntityCount;
                    int[] tmp = new int[16];
                    System.arraycopy(Block.LowEntityIndex, 0, tmp, 0, Block.LowEntityIndex.length);
                    oldBlock.LowEntityIndex = tmp;
                    WorldEntityBlock bn = Block.next != null ? blah.get(Block.next) : null;
                    tmp = new int[16];
                    System.arraycopy(bn != null ? bn.LowEntityIndex : new int[16], 0, tmp, 0, bn != null ? bn.LowEntityIndex.length : 16);
                    Block.EntityCount = 0;
                    Block.LowEntityIndex = new int[16];
                    oldBlock.next = Block.next;
                    blah.addLast(oldBlock);
                }
                Block.LowEntityIndex[Block.EntityCount] = LowEntityIndex;
                Block.EntityCount = Block.EntityCount + 1;
            }
        }
    }

    public AddLowEntityResult AddMonstar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult Entity = AddGroundedEntity(EntityType.MONSTAR, P, null, SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        return(Entity);
    }

    public AddLowEntityResult AddFamiliar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult Entity = AddGroundedEntity(EntityType.FAMILIAR, P, null, SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        return(Entity);
    }

    public AddLowEntityResult AddGroundedEntity(EntityType Type, World.WorldPosition P, SimEntityCollisionVolumeGroup Collision, SimEntityFlag ...flags) {
//        World.WorldPosition OffsetP = world.MapIntoChunkSpace(P, new Vector3f(0, 0, 0));
        AddLowEntityResult Entity = AddLowEntity(Type, P, Collision, flags);

        return(Entity);
    }

    public AddLowEntityResult AddStandardRoom(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult Entity = AddGroundedEntity(
                EntityType.SPACE,
                P,
                gameMemory.StandardRoomCollision,
                SimEntityFlag.TRAVERSABLE
        );

        return(Entity);
    }


    public void UpdateFamiliar(SimRegion simRegion, SimEntity Entity, float dt) {
        SimEntity ClosestHero = null;
        float ClosestHeroDSq = (float) Math.pow(10.0f, 2);
            SimEntity TestEntity = simRegion.simEntities[0];
            // @TODO huh, index is moved but not looping through all entities
            for (int testEntityIndex = 0; testEntityIndex < simRegion.EntityCount; ++testEntityIndex) {
                assert (TestEntity != null);
                if (TestEntity.Type == EntityType.HERO) {
                    float TestDSq = new Vector3f(TestEntity.P).sub(new Vector3f(Entity.P)).lengthSquared();
                    if (TestEntity.Type == EntityType.HERO) {
                        TestDSq *= 0.75f;
                    }

                    if (ClosestHeroDSq > TestDSq) {
                        ClosestHero = TestEntity;
                        ClosestHeroDSq = TestDSq;
                    }
                }
            }

        Vector3f ddP = new Vector3f();
        if (ClosestHero != null && ClosestHeroDSq > Math.pow(3.0f, 2.0f)) {
            float Acceleration = 0.5f;
            float OneOverLength = (float) (Acceleration / Math.sqrt(ClosestHeroDSq));
            ddP = new Vector3f(ClosestHero.P).sub(new Vector3f(Entity.P)).mul(OneOverLength);
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

    public void moveEntity(SimRegion simRegion, SimEntity entity, Vector3f ddP, float interval, MoveSpec moveSpec) {
        assert(!IsSet(entity, SimEntityFlag.NONSPATIAL));

        if (moveSpec.UnitMaxAccelVector) {
            float ddPLength = new Vector3f(ddP).lengthSquared();
            if (ddPLength > 1.0f) {
                ddP = new Vector3f(ddP.x(), ddP.y(), ddP.z()).mul((float) (1.0f / Math.sqrt(ddPLength)));
            }
        }

        ddP = ddP.mul(moveSpec.Speed);
        ddP = ddP.add((new Vector3f(entity.dP.x(), entity.dP.y(), entity.dP.z()).mul(-moveSpec.Drag)));

        Vector3f OldPlayerP = new Vector3f(entity.P);
        Vector3f playerDelta = new Vector3f(ddP.x(), ddP.y(), ddP.z()).mul(0.5f).mul(interval * interval).add(new Vector3f(entity.dP.x(), entity.dP.y(), entity.dP.z()).mul(interval));
        entity.dP = new Vector3f(ddP).mul(interval).add(new Vector3f(entity.dP));

        assert (new Vector3f(entity.dP).lengthSquared() <= simRegion.MaxEntityVelocity * simRegion.MaxEntityVelocity);

        Vector3f NewPlayerP = new Vector3f(OldPlayerP).add(new Vector3f(playerDelta));

        float DistanceRemaining = entity.DistanceLimit;
        if (DistanceRemaining == 0.0f) {
            DistanceRemaining = 10000.0f;
        }
        int count = 0;
        for (int Iteration = 0; Iteration < 4; ++Iteration) {
            count = count + 1;
            float[] tMin = {1.0f, 0.0f};
            float PlayerDeltaLength = playerDelta.length();
            if (PlayerDeltaLength > 0.0f) {
                if (PlayerDeltaLength > DistanceRemaining) {
                    tMin[0] = DistanceRemaining / PlayerDeltaLength;
                }
                Vector3f WallNormal = new Vector3f(0.0f, 0.0f, 0.0f);
                SimEntity HitEntity = null;
                Vector3f DesiredPosition = new Vector3f(entity.P).add(new Vector3f(playerDelta));
                boolean StopOnCollision = IsSet(entity, SimEntityFlag.COLLIDES);
                if (!IsSet(entity, SimEntityFlag.NONSPATIAL)) {
                    for (int TestHighEntityIndex = 1; TestHighEntityIndex < simRegion.EntityCount; ++TestHighEntityIndex) {
                        SimEntity TestEntity = simRegion.simEntities[TestHighEntityIndex];
                        if (TestEntity != entity) {
                            if (TestEntity.flags.contains(SimEntityFlag.COLLIDES) && !entity.flags.contains(SimEntityFlag.NONSPATIAL)) {
                                Vector3f MinkowskiDiameter = new Vector3f(TestEntity.Dim.x() + entity.Dim.x(), TestEntity.Dim.y() + entity.Dim.y(), 2.0f * World.TileDepthInMeters);
                                Vector3f MinCorner = MinkowskiDiameter.mul(-0.5f);
                                Vector3f MaxCorner = MinkowskiDiameter.mul(0.5f);
                                Vector3f Rel = new Vector3f(entity.P).sub(new Vector3f(TestEntity.P));
                                if (world.TestWall(MinCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                        tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                                    WallNormal = new Vector3f(-1, 0, 0);
                                    HitEntity = TestEntity;
                                }

                                if (world.TestWall(MaxCorner.x(), Rel.x(), Rel.y(), playerDelta.x(), playerDelta.y(),
                                        tMin, MinCorner.y(), MaxCorner.y())[1] == 1) {
                                    WallNormal = new Vector3f(1, 0, 0);
                                    HitEntity = TestEntity;
                                }

                                if (world.TestWall(MinCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                        tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                                    WallNormal = new Vector3f(0, -1, 0);
                                    HitEntity = TestEntity;
                                }

                                if (world.TestWall(MaxCorner.y(), Rel.y(), Rel.x(), playerDelta.y(), playerDelta.x(),
                                        tMin, MinCorner.x(), MaxCorner.x())[1] == 1) {
                                    WallNormal = new Vector3f(0, 1, 0);
                                    HitEntity = TestEntity;
                                }
                            }
                        }
                    }
                }
                entity.P = new Vector3f(entity.P).add(new Vector3f(playerDelta).mul(tMin[0]));
                DistanceRemaining = DistanceRemaining - tMin[0] * PlayerDeltaLength;
                if (HitEntity != null) {
                    playerDelta = new Vector3f(DesiredPosition).sub(new Vector3f(entity.P));
                    if (StopOnCollision) {
                        entity.dP = new Vector3f(entity.dP).sub(new Vector3f(WallNormal).mul(new Vector3f(entity.dP).dot(WallNormal)));
                        playerDelta = new Vector3f(playerDelta).sub(new Vector3f(WallNormal).mul(new Vector3f(playerDelta).dot(new Vector3f(WallNormal))));
                    }
                } else {
                    break;
                }
            } else {
                break;
            }
        }
    }

    public void pushToRender(RenderGroup renderGroup) {
        if (gameMemory.simRegion == null || Renderer2D.texture == null) {
            return;
        }

        IntStream.range(0, gameMemory.simRegion.EntityCount).forEach(HighEntityIndex -> {
                SimEntity entity = gameMemory.simRegion.simEntities[HighEntityIndex];
                if (entity.Updatable) {
                    MoveSpec MoveSpec = null;
                    switch (entity.Type.name().toLowerCase()) {
                        case ("hero"): {
                            MoveSpec = DefaultMoveSpec();
                            MoveSpec.UnitMaxAccelVector = true;
                            MoveSpec.Speed = GameApp.playerSpeed == 1 ? 50.0f : GameApp.playerSpeed;
                            MoveSpec.Drag = 8.0f;
                        }
                        break;
                        case ("familiar"): {
//                        UpdateFamiliar(gameMemory.simRegion, entity, GameApp.globalinterval);
//                        entity.tBob = entity.tBob + GameApp.globalinterval;
//                        if (entity.tBob > 2.0f * Math.PI) {
//                            entity.tBob = (float) (entity.tBob - (2.0f * Math.PI));
//                        }
//                        pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                        }
                        break;
                        case ("monstar"): {

                        } break;
                        default: {
                            throw new RuntimeException("Invalid code path");
                        }
                    }

                    if (!IsSet(entity, SimEntityFlag.NONSPATIAL) && MoveSpec != null && IsSet(entity, SimEntityFlag.MOVEABLE)) {
                        moveEntity(gameMemory.simRegion, entity, gameMemory.ControlledHero.ddP, GameApp.globalinterval, MoveSpec);
                    }

                    renderGroup.Transform.OffsetP = GetEntityGroundPoint(entity);

                    switch(entity.Type.name().toLowerCase()) {
                        case ("wall"): {
                            LoadedBitmap loadedBitmap = new LoadedBitmap();
                            loadedBitmap.texture = Renderer2D.texture;
                            loadedBitmap.Width = 60;
                            loadedBitmap.Height = 60;
                            loadedBitmap.WidthOverHeight = 1;
                            renderGroupService.pushBitmap(renderGroup, loadedBitmap, 1.5f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector4f(1, 0.5f, 0f, 1f), entity.Type);
                        } break;
                        case ("hero"): {
                            LoadedBitmap loadedBitmap = new LoadedBitmap();
                            loadedBitmap.texture = Renderer2D.texture;
                            loadedBitmap.Width = 60;
                            loadedBitmap.Height = 60;
                            loadedBitmap.WidthOverHeight = 1;
                            float HeroSizeC = 2.5f;
                            renderGroupService.pushBitmap(renderGroup, loadedBitmap, HeroSizeC * 0.4f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector4f(1f, 1f, 1f, 1f), entity.Type);
                        } break;
                        case ("space"): {
                            for (int VolumeIndex = 0; VolumeIndex < entity.Collision.VolumeCount; ++VolumeIndex) {
                                SimEntityCollisionVolume Volume = entity.Collision.Volumes[VolumeIndex];
//                                    renderGroupService.PushRectOutline(GameApp.renderGroup, new Vector3f(Volume.OffsetP).sub(new Vector3f(0, 0, 0.5f * Volume.Dim.z)) , new Vector2f(Volume.Dim.x, Volume.Dim.y), new Vector4f(0, 0.5f, 1.0f, 1.0f), entity.Type);
                            }
                        } break;
                        default: {
                            throw new RuntimeException("Invalid code path");
                        }
                    }
                }
            });
//        DrawPoints();
    }

    public Map<Integer, List<Vector3f>> getDebug() {
        float ScreenWidthInMeters = DisplayManager.WIDTH;
        float ScreenHeightInMeters = DisplayManager.HEIGHT;
        Rectangle CameraBoundsInMeters = Rectangle.RectCenterDim(new Vector3f(0, 0, 0),
                new Vector3f(ScreenWidthInMeters, ScreenHeightInMeters, 0.0f));

        // Debug drawing
        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMinCorner(CameraBoundsInMeters));
        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMaxCorner(CameraBoundsInMeters));
        Vector2f ScreenCenter = new Vector2f(DisplayManager.WIDTH * 0.5f, DisplayManager.HEIGHT * 0.5f);

        Map<Integer, List<Vector3f>> blah = new HashMap<>();
        for(int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
            for(int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
                World.WorldPosition ChunkCenterP = world.CenteredChunkPoint(ChunkX, ChunkY);
                Vector3f RelP = World.subtract(ChunkCenterP, Camera.position);
//                Vector2f ScreenP = new Vector2f(ScreenCenter.x + World.MetersToPixels * RelP.x, ScreenCenter.y - World.MetersToPixels * RelP.y);
//                Vector2f ScreenDim = new Vector2f(World.MetersToPixels * World.WorldChunkDimInMeters.x, World.MetersToPixels * World.WorldChunkDimInMeters.y);

//                blah.put(ChunkX + ChunkY, List.of(
//                        new Vector3f(new Vector2f(ScreenP).sub(new Vector2f(ScreenDim).mul(0.5f)), 0),
//                        new Vector3f(new Vector2f(ScreenP).add(new Vector2f(ScreenDim).mul(0.5f)), 0)
//                ));
            }
        }

//        blah.put(10000, List.of(
//                new Vector3f(0.0f, 0.0f, 0.0f),
//                new Vector3f(DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f)
//        ));
        return blah;
    }

    public Vector3f GetSimSpaceP(SimRegion simRegion, LowEntity stored) {
        Vector3f Result = GameMemory.InvalidP;
        if (!IsSet(stored.Sim, SimEntityFlag.NONSPATIAL)) {
            return World.subtract(stored.P, simRegion.Origin);
        }

        return Result;
    }

    public SimEntity AddEntity(SimRegion simRegion, int StorageIndex, LowEntity Source, Vector3f SimP) {
        SimEntity Dest = AddEntityRaw(simRegion, StorageIndex, Source);
        if (Dest != null) {
            if (SimP != null) {
                Dest.P = new Vector3f(SimP);
                Dest.Updatable = EntityOverlapsRectangle(Dest.P, Dest.Dim, simRegion.UpdatableBounds);
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
                    Entry.Ptr = Entity;

                    assert (!IsSet(Source.Sim, SimEntityFlag.SIMMING));
                    AddFlag(Source.Sim, SimEntityFlag.SIMMING);
                    simRegion.simEntities[EntityCount] = Entity;
//                LoadEntityReference(simRegion, Entity.Sword);
                }

                Entity.StorageIndex = StorageIndex;
                Entity.Updatable = false;
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
                return Entry;
            }
        }

        return null;
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


    public boolean EntityOverlapsRectangle(Vector3f P, Vector3f Dim, Rectangle Rect) {
        Rectangle Grown = AddRadiusTo(Rect, new Vector3f(Dim).mul(0.5f));
        boolean Result = Rectangle.IsInRectangle(Grown, P);
        return(Result);
    }

    public SimRegion BeginSim(World.WorldPosition Origin, Rectangle Bounds, float interval) {
        SimRegion simRegion = new SimRegion();
        simRegion.MaxEntityRadius = 5.0f;
        simRegion.MaxEntityVelocity = 30.0f;
        float UpdateSafetyMargin = simRegion.MaxEntityRadius + interval * simRegion.MaxEntityVelocity;
        simRegion.Origin = Origin;
        simRegion.UpdatableBounds = AddRadiusTo(Bounds, new Vector3f(simRegion.MaxEntityRadius, simRegion.MaxEntityRadius, 0.0f));
        simRegion.Bounds = AddRadiusTo(simRegion.UpdatableBounds, new Vector3f(UpdateSafetyMargin, UpdateSafetyMargin, 0.0f));


        simRegion.MaxEntityCount = 4096;
        simRegion.EntityCount = 0;
        simRegion.simEntities = new SimEntity[simRegion.MaxEntityCount];

        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMinCorner(simRegion.Bounds));
        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMaxCorner(simRegion.Bounds));

        for (int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
            for (int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
                WorldChunk Chunk = world.GetWorldChunk(ChunkX, ChunkY, false);
                if (Chunk != null) {
                    List<WorldEntityBlock> First = Chunk.getFirstBlock(false);
                    for (WorldEntityBlock Block : First) {
                        for (int EntityIndexIndex = 0; EntityIndexIndex < Block.EntityCount; ++EntityIndexIndex) {
                            int LowEntityIndex = Block.LowEntityIndex[EntityIndexIndex];
                            LowEntity Low = gameMemory.LowEntities[LowEntityIndex];
                            if (!IsSet(Low.Sim, SimEntityFlag.NONSPATIAL)) {
                                Vector3f SimSpaceP = GetSimSpaceP(simRegion, Low);
                                if (EntityOverlapsRectangle(SimSpaceP, Low.Sim.Dim, simRegion.Bounds)) {
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
//                StoreEntityReference(Stored.Sim.Sword);

                World.WorldPosition NewP = IsSet(Entity, SimEntityFlag.NONSPATIAL) ? null : world.MapIntoChunkSpace(Region.Origin, Entity.P);
                ChangeEntityLocation(Entity.StorageIndex, Stored, NewP);

                if (Entity.StorageIndex == gameMemory.CameraFollowingEntityIndex) {
                    World.WorldPosition NewCameraP = new World.WorldPosition(Stored.P);
                    Camera.position = Stored.P;
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

    public void MakeEntitySpatial(SimEntity Entity, Vector3f P, Vector3f dP) {
        ClearFlag(Entity, SimEntityFlag.NONSPATIAL);
        Entity.P = P;
        Entity.dP = dP;
    }

    public Rectangle AddRadiusTo(Rectangle A, Vector3f Radius) {
        return new Rectangle(
                new Vector3f(A.getMin()).sub(Radius),
                new Vector3f(A.getMax()).add(Radius)
        );
    }

    public static Vector2f Hadamard(Vector2f A, Vector2f B) {
        Vector2f Result = new Vector2f(A.x() * B.x(), A.y() * B.y());

        return(Result);
    }

    public static Vector3f Hadamard(Vector3f A, Vector3f B) {
        Vector3f Result = new Vector3f(A.x() * B.x(), A.y() * B.y(), A.z() * B.z());

        return(Result);
    }

    public World.WorldPosition NullPosition() {
        World.WorldPosition Result = new World.WorldPosition();
        Result.ChunkX = Integer.MAX_VALUE;

        return(Result);
    }

    private boolean isValid(World.WorldPosition P) {
        return P != null && P.ChunkX != Integer.MAX_VALUE;
    }

    public Vector3f GetEntityGroundPoint(SimEntity Entity) {
        Vector3f Result = new Vector3f(Entity.P);

        return(Result);
    }

    public SimEntityCollisionVolumeGroup MakeSimpleGroundedCollision(float DimX, float DimY, float DimZ) {
        SimEntityCollisionVolumeGroup Group = new SimEntityCollisionVolumeGroup();
        Group.VolumeCount = 1;
        Group.Volumes = new SimEntityCollisionVolume[1];
        Group.TotalVolume = new SimEntityCollisionVolume();
        Group.TotalVolume.OffsetP = new Vector3f(0, 0, 0.5f * DimZ);
        Group.TotalVolume.Dim = new Vector3f(DimX, DimY, DimZ);
        Group.Volumes[0] = Group.TotalVolume;

        return Group;
    }

    public RenderGroup initRenderGroup() {
        return renderGroupService.AllocateRenderGroup(1000000, DisplayManager.WIDTH, DisplayManager.HEIGHT);
    }

    public RenderEntryCoordinateSystem CoordinateSystem(RenderGroup Group, Vector2f Origin, Vector2f XAxis, Vector2f YAxis, Vector4f Color) {
        RenderEntryCoordinateSystem Entry = (RenderEntryCoordinateSystem) renderGroupService.PushRenderElement(Group, RenderGroupEntryType.COORDINATE);

        Entry.Origin = Origin;
        Entry.XAxis = XAxis;
        Entry.YAxis = YAxis;
        Entry.Color = Color;
        Entry.EntityType = EntityType.DEBUG;

        return(Entry);
    }

    public void DrawPoints() {
        GameApp.Time += GameApp.globalinterval;
        float Angle = GameApp.Time;

        Vector2f Origin = new Vector2f(GameApp.ScreenCenter).add(new Vector2f((float) Math.sin(Angle), 0.0f).mul(10.0f));
        Vector2f XAxis = new Vector2f((float) Math.cos(Angle), (float) Math.sin(Angle)).mul(100.0f + 25.0f * (float) Math.cos(4.2f * Angle));
        Vector2f YAxis = new Vector2f((float) Math.cos(Angle + 1.0f), (float) Math.sin(Angle + 1.0f)).mul(100.0f + 50.0f * (float) Math.sin(3.9f*Angle));
        int PIndex = 0;
        RenderEntryCoordinateSystem C = CoordinateSystem(GameApp.renderGroup, Origin, XAxis, YAxis, new Vector4f(0.5f + 0.5f * (float) Math.sin(Angle), 0.5f +0.5f * (float) Math.sin(2.9f * Angle), 0.5f + 0.5f * (float) Math.cos(9.9f * Angle), 1));
        for (float Y = 0.0f; Y < 1.0f; Y += 0.25f) {
            for (float X = 0.0f; X < 1.0f; X += 0.25f) {
                C.Points[PIndex++] = new Vector2f(X, Y);
            }
        }
    }
}
