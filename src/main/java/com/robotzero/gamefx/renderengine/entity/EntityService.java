package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.assets.Asset;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.rendergroup.GameRenderCommands;
import com.robotzero.gamefx.renderengine.rendergroup.LoadedBitmap;
import com.robotzero.gamefx.renderengine.rendergroup.RenderEntryCoordinateSystem;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroup;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupEntryType;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
import com.robotzero.gamefx.renderengine.translations.MoveSpec;
import com.robotzero.gamefx.world.*;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.stream.IntStream;

public class EntityService {
    private final GameMemory gameMemory;
    private final World world;
    private final RenderGroupService renderGroupService;
    private final GameModeWorld gameModeWorld;
    public static final float PlayerHeight = 1.0f;
    public static final float PlayerWidth = 1.0f;
    public static final float FamiliarHeight = 0.5f;
    public static final float FamiliarWidth = 1.0f;

    public EntityService(GameMemory gameMemory, World world, RenderGroupService renderGroupService, GameModeWorld gameModeWorld) {
        this.gameMemory = gameMemory;
        this.world = world;
        this.renderGroupService = renderGroupService;
        this.gameModeWorld = gameModeWorld;
    }

    public World getWorld() {
        return world;
    }

    public Entity AddPlayer() {
        World.WorldPosition P = gameModeWorld.CameraP;
        Entity entity = BeginGroundedEntity(EntityType.HERO, MakeSimpleGroundedCollision(1.0f, 0.5f, 0.6f));
        AddFlag(entity, EntityFlag.COLLIDES);
        AddFlag(entity, EntityFlag.MOVEABLE);

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = entity.StorageIndex;
        }

        EndEntity(entity, P);

        return entity;
    }

    public Entity BeginLowEntity(EntityType Type) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        World.CreationBuffer[World.CreationBufferIndex] = new Entity();
        World.CreationBuffer[World.CreationBufferIndex].Type = Type;
        World.CreationBuffer[World.CreationBufferIndex].P = new Vector3f();
        World.CreationBuffer[World.CreationBufferIndex].Collision = MakeNullCollision();
//        World.CreationBuffer[World.CreationBufferIndex].P = NullPosition();
//        if (flags != null && flags.length > 0) {
//            for (int i = 0; i < flags.length; ++i) {
//                AddFlag(gameMemory.LowEntities[EntityIndex].Sim, flags[i]);
//            }
//        }
        Entity EntityLow = World.CreationBuffer[World.CreationBufferIndex];
        EntityLow.StorageIndex = World.LastUsedEntityStorageIndex + 1;
        World.LastUsedEntityStorageIndex = World.LastUsedEntityStorageIndex + 1;
        World.CreationBufferIndex = World.CreationBufferIndex + 1;

        return EntityLow;
    }

    public void EndEntity(Entity Entity, World.WorldPosition P)
    {
        --World.CreationBufferIndex;
        world.PackEntityIntoWorld(Entity, P);
    }

    public void AddWall(int ChunkX, int ChunkY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(ChunkX, ChunkY, new Vector3f(0.0f, 0.0f, 0.0f));
        Entity entity = BeginGroundedEntity(EntityType.WALL, MakeSimpleGroundedCollision(World.TileSideInMeters, World.TileSideInMeters, World.TileDepthInMeters));
        AddFlag(entity, EntityFlag.COLLIDES);
        EndEntity(entity, P);
    }

    public World.WorldPosition ChunkPositionFromTilePosition(int AbsTileX, int AbsTileY, Vector3f AdditionalOffset) {
        World.WorldPosition BasePos = new World.WorldPosition();

        float TileSideInMeters = 1.4f;
        float TileDepthInMeters = 3.0f;

        Vector3f TileDim = new Vector3f(TileSideInMeters, TileSideInMeters, TileDepthInMeters);
        Vector3f Offset = EntityService.Hadamard(new Vector3f(TileDim), new Vector3f(AbsTileX, AbsTileY, 0));

        World.WorldPosition Result = world.MapIntoChunkSpace(BasePos, Offset);
        assert (world.IsCanonical(Result.Offset));
        return (Result);
    }

    public void ChangeEntityLocation(int LowEntityIndex, LowEntity LowEntity, World.WorldPosition NewPInit) {
        World.WorldPosition OldP = null;
        World.WorldPosition NewP = null;

        if (!IsSet(LowEntity.Sim, EntityFlag.NONSPATIAL) && isValid(LowEntity.P)) {
            OldP = new World.WorldPosition(LowEntity.P);
        }

        if (isValid(NewPInit)) {
            NewP = new World.WorldPosition(NewPInit);
        }

        ChangeEntityLocationRaw(LowEntityIndex, OldP, NewP);

        if (NewP != null) {
            LowEntity.P = new World.WorldPosition(NewP);
            ClearFlag(LowEntity.Sim, EntityFlag.NONSPATIAL);
        } else {
            LowEntity.P = null;
            AddFlag(LowEntity.Sim, EntityFlag.NONSPATIAL);
        }
    }

    public void ChangeEntityLocationRaw(int LowEntityIndex, World.WorldPosition OldP, World.WorldPosition NewP) {
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
                    LinkedList<WorldEntityBlock> FirstBlock = Chunk.getFirstBlock();
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
                WorldEntityBlock Block = Chunk.getFirstBlock().getFirst();
                LinkedList<WorldEntityBlock> blah = Chunk.getFirstBlock();
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

    public Entity BeginGroundedEntity(EntityType Type, EntityCollisionVolumeGroup Collision) {
        Entity Entity = BeginLowEntity(Type);
        Entity.Collision = Collision;

        return(Entity);
    }

    public MoveSpec DefaultMoveSpec() {
        MoveSpec moveSpec = new MoveSpec();
        moveSpec.UnitMaxAccelVector = false;
        moveSpec.Speed = 1.0f;
        moveSpec.Drag = 0.0f;

        return moveSpec;
    }

    public void moveEntity(SimRegion simRegion, Entity entity, Vector3f ddP, float interval, MoveSpec moveSpec) {
        assert(!IsSet(entity, EntityFlag.NONSPATIAL));

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
                Entity HitEntity = null;
                Vector3f DesiredPosition = new Vector3f(entity.P).add(new Vector3f(playerDelta));
                boolean StopOnCollision = IsSet(entity, EntityFlag.COLLIDES);
                if (!IsSet(entity, EntityFlag.NONSPATIAL)) {
                    for (int TestHighEntityIndex = 1; TestHighEntityIndex < simRegion.EntityCount; ++TestHighEntityIndex) {
                        Entity TestEntity = simRegion.simEntities[TestHighEntityIndex];
                        if (TestEntity != entity) {
                            if (TestEntity.flags.contains(EntityFlag.COLLIDES) && !entity.flags.contains(EntityFlag.NONSPATIAL)) {
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
        if (gameMemory.simRegion == null) {
            return;
        }

        IntStream.range(0, gameMemory.simRegion.EntityCount).forEach(HighEntityIndex -> {
                Entity entity = gameMemory.simRegion.simEntities[HighEntityIndex];
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
                    }

                    if (!IsSet(entity, EntityFlag.NONSPATIAL) && MoveSpec != null && IsSet(entity, EntityFlag.MOVEABLE)) {
                        moveEntity(gameMemory.simRegion, entity, gameMemory.ControlledHero.ddP, GameApp.globalinterval, MoveSpec);
                    }

                    ObjectTransform EntityTransform = DefaultFlatTransform();
                    EntityTransform.OffsetP = GetEntityGroundPoint(entity);

                    switch(entity.Type.name().toLowerCase()) {
                        case ("wall"): {
                            Optional.ofNullable(renderGroup.Assets.get("tree00.bmp")).ifPresentOrElse(tree -> {
                                LoadedBitmap loadedBitmap = new LoadedBitmap();
                                loadedBitmap.texture = gameMemory.gameAssets.get("tree00.bmp").getTexture();
                                loadedBitmap.Width = gameMemory.gameAssets.get("tree00.bmp").getWidth();
                                loadedBitmap.Height = gameMemory.gameAssets.get("tree00.bmp").getHeight();
                                loadedBitmap.WidthOverHeight = (float) loadedBitmap.Width / (float) loadedBitmap.Height;
                                loadedBitmap.AlignPercentage = new Vector2f(1, 1);
                                renderGroupService.pushBitmap(renderGroup, EntityTransform, loadedBitmap, 1.5f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector4f(1f, 1f, 1f, 1f), 1.0f, entity.Type);
                            }, () -> {
                                System.out.println("Asset not loaded");
                            });
                        } break;
                        case ("hero"): {
                            Optional.ofNullable(renderGroup.Assets.get("fred_01.png")).ifPresentOrElse(hero -> {
                                LoadedBitmap loadedBitmap = new LoadedBitmap();
                                loadedBitmap.texture = gameMemory.gameAssets.get("fred_01.png").getTexture();
                                loadedBitmap.Width = gameMemory.gameAssets.get("fred_01.png").getWidth();
                                loadedBitmap.Height = gameMemory.gameAssets.get("fred_01.png").getHeight();
                                loadedBitmap.WidthOverHeight = (float) loadedBitmap.Width / (float) loadedBitmap.Height;
                                loadedBitmap.WidthOverHeight = 1.0f;
                                loadedBitmap.AlignPercentage = new Vector2f(1, 1);
                                float HeroSizeC = 1.0f;
                                renderGroupService.pushBitmap(renderGroup, EntityTransform, loadedBitmap, HeroSizeC * 1.5f, new Vector3f(0.0f, 0.0f, 0.0f), new Vector4f(0.5f, 0.5f, 0.5f, 1f), 1.0f, entity.Type);
                            }, () -> {
                                System.out.println("Asset not loaded");
                            });
                        } break;
                        case ("space"): {
                            for (int VolumeIndex = 0; VolumeIndex < entity.Collision.VolumeCount; ++VolumeIndex) {
                                EntityCollisionVolume Volume = entity.Collision.Volumes[VolumeIndex];
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

//    public Map<Integer, List<Vector3f>> getDebug() {
//        float ScreenWidthInMeters = DisplayManager.WIDTH;
//        float ScreenHeightInMeters = DisplayManager.HEIGHT;
//        Rectangle CameraBoundsInMeters = Rectangle.RectCenterDim(new Vector3f(0, 0, 0),
//                new Vector3f(ScreenWidthInMeters, ScreenHeightInMeters, 0.0f));
//
//        // Debug drawing
//        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMinCorner(CameraBoundsInMeters));
//        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMaxCorner(CameraBoundsInMeters));
//        Vector2f ScreenCenter = new Vector2f(DisplayManager.WIDTH * 0.5f, DisplayManager.HEIGHT * 0.5f);
//
//        Map<Integer, List<Vector3f>> blah = new HashMap<>();
//        for(int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
//            for(int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
//                World.WorldPosition ChunkCenterP = world.CenteredChunkPoint(ChunkX, ChunkY);
//                Vector3f RelP = World.subtract(ChunkCenterP, Camera.position);
////                Vector2f ScreenP = new Vector2f(ScreenCenter.x + World.MetersToPixels * RelP.x, ScreenCenter.y - World.MetersToPixels * RelP.y);
////                Vector2f ScreenDim = new Vector2f(World.MetersToPixels * World.WorldChunkDimInMeters.x, World.MetersToPixels * World.WorldChunkDimInMeters.y);
//
////                blah.put(ChunkX + ChunkY, List.of(
////                        new Vector3f(new Vector2f(ScreenP).sub(new Vector2f(ScreenDim).mul(0.5f)), 0),
////                        new Vector3f(new Vector2f(ScreenP).add(new Vector2f(ScreenDim).mul(0.5f)), 0)
////                ));
//            }
//        }
//
////        blah.put(10000, List.of(
////                new Vector3f(0.0f, 0.0f, 0.0f),
////                new Vector3f(DisplayManager.WIDTH, DisplayManager.HEIGHT, 0.0f)
////        ));
//        return blah;
//    }

    public Vector3f GetSimSpaceP(SimRegion simRegion, Entity stored) {
        Vector3f Result = GameMemory.InvalidP;
        if (!IsSet(stored, EntityFlag.NONSPATIAL)) {
            return World.subtract(stored.ChunkP, simRegion.Origin);
        }

        return Result;
    }

    public Entity AddEntity(SimRegion simRegion, Entity Source, int StorageIndex, Vector3f SimP) {
        Entity Dest = AddEntityRaw(simRegion, StorageIndex, Source);
        if (Dest != null) {
            if (SimP != null) {
                Dest.P = new Vector3f(SimP);
                Dest.Updatable = EntityOverlapsRectangle(Dest.P, Dest.Collision.TotalVolume, simRegion.UpdatableBounds);
            } else {
                Dest.P = GetSimSpaceP(simRegion, Source);
            }
        }

        return Dest;
    }

    Entity AddEntityRaw(SimRegion simRegion, int StorageIndex, Entity Source) {
        Entity Entity = null;
        EntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        if (Entry.Ptr == null) {
            if (simRegion.EntityCount < simRegion.MaxEntityCount) {
                int EntityCount = simRegion.EntityCount;
                Entity = simRegion.simEntities[EntityCount];
                simRegion.EntityCount = simRegion.EntityCount + 1;
                if (Entity == null) {
                    Entity = new Entity();
                    simRegion.simEntities[EntityCount] = Entity;
                }

                Entry.Index = StorageIndex;
                Entry.Ptr = Entity;

                if (Source != null) {
                    // TODO(casey): This should really be a decompression step, not
                    // a copy!
                    Entity = new Entity(Source);
                    simRegion.simEntities[EntityCount] = Entity;
                    Entry.Ptr = Entity;

                    assert (!IsSet(Source, EntityFlag.SIMMING));
                    AddFlag(Source, EntityFlag.SIMMING);
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

    public void MapStorageIndexToEntity(SimRegion simRegion, int StorageIndex, Entity Entity) {
        EntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        assert((Entry.Index == 0) || (Entry.Index == StorageIndex));
        Entry.Index = StorageIndex;
        Entry.Ptr = Entity;
    }

    public Entity GetEntityByStorageIndex(SimRegion simRegion, int StorageIndex) {
        EntityHash Entry = GetHashFromStorageIndex(simRegion, StorageIndex);
        Entity Result = Entry.Ptr;
        return(Result);
    }

    public EntityHash GetHashFromStorageIndex(SimRegion simRegion, int StorageIndex) {
        assert (StorageIndex != 0);

        int HashValue = StorageIndex;
        for (int Offset = 0; Offset < simRegion.Hash.length; ++Offset) {
            int HashMask = simRegion.Hash.length - 1;
            int HashIndex = ((HashValue + Offset) & HashMask);
            EntityHash Entry = simRegion.Hash[HashIndex];
            if (Entry == null) {
                simRegion.Hash[HashIndex] = new EntityHash();
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


    public boolean EntityOverlapsRectangle(Vector3f P, EntityCollisionVolume Volume, Rectangle Rect) {
        Rectangle Grown = AddRadiusTo(Rect, new Vector3f(Volume.Dim).mul(0.5f));
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
        simRegion.simEntities = new Entity[simRegion.MaxEntityCount];

        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMinCorner(simRegion.Bounds));
        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(simRegion.Origin, Rectangle.GetMaxCorner(simRegion.Bounds));

        for (int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
            for (int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
                WorldChunk Chunk = world.RemoveWorldChunk(ChunkX, ChunkY, false);
                if (Chunk != null) {
                    World.WorldPosition ChunkPosition = new World.WorldPosition();
                    ChunkPosition.ChunkX = ChunkX;
                    ChunkPosition.ChunkY = ChunkY;
                    Vector3f ChunkDelta = World.subtract(ChunkPosition, simRegion.Origin);
                    List<WorldEntityBlock> First = Chunk.getFirstBlock();
                    for (WorldEntityBlock Block : First) {
                        for (int EntityIndex = 0; EntityIndex < Block.EntityCount; ++EntityIndex) {
                            Entity Low = Block.entityData[EntityIndex];
                            Vector3f SimSpaceP = Low.P.add(ChunkDelta);
                            if (EntityOverlapsRectangle(SimSpaceP, Low.Collision.TotalVolume, simRegion.Bounds)) {
                                AddEntity(simRegion, Low, EntityIndex, ChunkDelta);
                            }
                        }
//                        WorldEntityBlock NexBlock = First.listIterator(First.indexOf(Block)).next();

                        world.AddBlockToFreeList(Block);
//                        Block = NextBlock;
                    }
                    world.AddChunkToFreeList(Chunk);
                }
            }
        }

        return simRegion;
    }

    public void EndSim(SimRegion Region) {
        if (Region != null) {
            Entity Entity = Region.simEntities[0];

            for(int EntityIndex = 0; EntityIndex < Region.EntityCount; ++EntityIndex, Entity = Region.simEntities[EntityIndex]) {
                if (!(Entity.flags.contains(EntityFlag.DELETED))) {
                    World.WorldPosition EntityP = world.MapIntoChunkSpace(Region.Origin, Entity.P);
                    World.WorldPosition ChunkP = new World.WorldPosition(EntityP);
                    ChunkP.Offset = new Vector3f(0f, 0f, 0f);
                    Vector3f temp = World.subtract(ChunkP, Region.Origin);
                    Vector3f ChunkDelta = new Vector3f(-temp.x, -temp.y, -temp.z);

                    if (Entity.StorageIndex == gameMemory.CameraFollowingEntityIndex) {
                        World.WorldPosition NewCameraP = new World.WorldPosition(EntityP);
//                        Camera.position = EntityP;
                        gameModeWorld.CameraP = NewCameraP;
                    }
                    Entity.P = new Vector3f(ChunkDelta).add(Entity.P);
                    world.PackEntityIntoWorld(Entity, Entity.ChunkP);
                }
            }
        }

        GameApp.renderGroup.clear();
    }

    public boolean IsSet(Entity Entity, EntityFlag Flag) {
        return Entity.flags.contains(Flag);
    }

    public void AddFlag(Entity Entity, EntityFlag Flag) {
        Entity.flags.add(Flag);
    }

    public void ClearFlag(Entity Entity, EntityFlag Flag) {
        Entity.flags.remove(Flag);
    }

    public void MakeEntityNonSpatial(Entity Entity) {
        AddFlag(Entity, EntityFlag.NONSPATIAL);
        Entity.P = GameMemory.InvalidP;
    }

    public void MakeEntitySpatial(Entity Entity, Vector3f P, Vector3f dP) {
        ClearFlag(Entity, EntityFlag.NONSPATIAL);
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

    public Vector3f GetEntityGroundPoint(Entity Entity) {
        Vector3f Result = new Vector3f(Entity.P);

        return(Result);
    }

    public EntityCollisionVolumeGroup MakeSimpleGroundedCollision(float DimX, float DimY, float DimZ) {
        EntityCollisionVolumeGroup Group = new EntityCollisionVolumeGroup();
        Group.VolumeCount = 1;
        Group.Volumes = new EntityCollisionVolume[1];
        Group.TotalVolume = new EntityCollisionVolume();
        Group.TotalVolume.OffsetP = new Vector3f(0, 0, 0.5f * DimZ);
        Group.TotalVolume.Dim = new Vector3f(DimX, DimY, DimZ);
        Group.Volumes[0] = Group.TotalVolume;

        return Group;
    }

    public EntityCollisionVolumeGroup MakeNullCollision() {
        EntityCollisionVolumeGroup Group = new EntityCollisionVolumeGroup();
        Group.VolumeCount = 0;
        Group.Volumes = new EntityCollisionVolume[1];
        Group.TotalVolume = new EntityCollisionVolume();
        Group.TotalVolume.Dim = new Vector3f(0, 0, 0);
        Group.Volumes[0] = Group.TotalVolume;

        return Group;
    }

    public RenderGroup BeginRenderGroup(Map<String, Asset> assets, GameRenderCommands Commands, boolean rendersInBackground) {
        RenderGroup renderGroup = new RenderGroup();
        renderGroup.Assets = assets;
        renderGroup.RendersInBackground = rendersInBackground;
        renderGroup.gameRenderCommands = Commands;

        return renderGroup;
//        return renderGroupService.AllocateRenderGroup(1000000);
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

    public static ObjectTransform DefaultUprightTransform() {
        ObjectTransform objectTransform = new ObjectTransform();
        objectTransform.Scale = 1.0f;
        objectTransform.Upright = true;

        return objectTransform;
    }

    public static ObjectTransform DefaultFlatTransform() {
        ObjectTransform objectTransform = new ObjectTransform();
        objectTransform.Scale = 1.0f;
        objectTransform.Upright = false;
        objectTransform.OffsetP = new Vector3f(0, 0, 0);

        return objectTransform;
    }
}
