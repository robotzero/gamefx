package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.translations.MoveSpec;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import com.robotzero.gamefx.world.WorldEntityBlock;
import com.robotzero.gamefx.world.WorldGenerator;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class EntityService {
    private final GameMemory gameMemory;
    private final World world;
    public static final float PlayerHeight = 0.5f;
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
        AddLowEntityResult entity = AddGroundedEntity(EntityType.HERO, P, new Vector3f(PlayerWidth, PlayerHeight, 1.2f), SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = entity.LowIndex;
        }

        return entity;
    }

    public AddLowEntityResult AddLowEntity(EntityType Type, World.WorldPosition P, Vector3f Dim, SimEntityFlag ...flags) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new LowEntity();
        gameMemory.LowEntities[EntityIndex].Sim = new SimEntity();
        gameMemory.LowEntities[EntityIndex].Sim.Type = Type;
        gameMemory.LowEntities[EntityIndex].Sim.Dim = Dim;
        gameMemory.LowEntities[EntityIndex].Sim.P = new Vector3f();
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
        AddLowEntityResult entity = AddGroundedEntity(EntityType.WALL, P, Dim, SimEntityFlag.COLLIDES);

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
        AddLowEntityResult Entity = AddGroundedEntity(EntityType.MONSTAR, P,  new Vector3f(1.0f, 0.5f, 0.5f), SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        return(Entity);
    }

    public AddLowEntityResult AddFamiliar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult Entity = AddGroundedEntity(EntityType.FAMILIAR, P, new Vector3f(1.0f, 0.5f, 0.5f), SimEntityFlag.COLLIDES, SimEntityFlag.MOVEABLE);

        return(Entity);
    }

    public AddLowEntityResult AddGroundedEntity(EntityType Type, World.WorldPosition P, Vector3f Dim, SimEntityFlag ...flags) {
        World.WorldPosition OffsetP = world.MapIntoChunkSpace(P, new Vector3f(0, 0, 0));
        AddLowEntityResult Entity = AddLowEntity(Type, OffsetP, Dim, flags);
        return(Entity);
    }

    public AddLowEntityResult AddStandardRoom(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY, new Vector3f(0.0f, 0.0f, 0.0f));
        AddLowEntityResult Entity = AddGroundedEntity(
                EntityType.SPACE,
                P,
                new Vector3f(WorldGenerator.tilesPerWidth * World.TileSideInMeters, WorldGenerator.tilesPerHeight * World.TileSideInMeters, 0.9f * World.TileDepthInMeters),
                SimEntityFlag.TRAVERSABLE
        );

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

    public void PushRectOutline(EntityVisiblePieceGroup group, Vector2f Offset, Vector2f Dim, Vector4f Color, float EntityZC) {
        float Thickness = 0.1f;

        // NOTE(casey): Top and bottom
        pushPiece(group, Offset.sub(new Vector2f(0, 0.5f * Dim.y)), new Vector2f(0, 0), new Vector2f(Dim.x, Thickness), Color, EntityZC);
        pushPiece(group, Offset.add(new Vector2f(0, 0.5f * Dim.y)), new Vector2f(0, 0), new Vector2f(Dim.x, Thickness), Color, EntityZC);

        // NOTE(casey): Left and right
        pushPiece(group, Offset.sub(new Vector2f(0.5f * Dim.x, 0)), new Vector2f(0, 0), new Vector2f(Thickness, Dim.y), Color, EntityZC);
        pushPiece(group, Offset.add(new Vector2f(0.5f * Dim.x, 0)), new Vector2f(0, 0), new Vector2f(Thickness, Dim.y), Color, EntityZC);
    }

    public Map<EntityType, List<Matrix4f>> getModelMatrix() {
        EntityVisiblePieceGroup pieceGroup = new EntityVisiblePieceGroup();

        if (gameMemory.simRegion == null) {
            return Map.of();
        }
        Map<EntityType, List<Matrix4f>> aaa = IntStream.range(0, gameMemory.simRegion.EntityCount).mapToObj(HighEntityIndex -> {
            pieceGroup.PieceCount = 0;
            SimEntity entity = gameMemory.simRegion.simEntities[HighEntityIndex];

            if (entity.Updatable) {
                MoveSpec MoveSpec = null;
                switch (entity.Type.name().toLowerCase()) {
                    case ("wall"): {
                        pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(40f, 80f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                    }
                    break;
                    case ("hero"): {
                        MoveSpec = DefaultMoveSpec();
                        MoveSpec.UnitMaxAccelVector = true;
                        MoveSpec.Speed = GameApp.playerSpeed == 1 ? 50.0f : GameApp.playerSpeed;
                        MoveSpec.Drag = 8.0f;
                        pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
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

                    }
                    break;
                    case ("space"): {
//                        MakeSimpleGroundedCollision(GameState,
//                                TilesPerWidth*GameState->World->TileSideInMeters,
//                                TilesPerHeight*GameState->World->TileSideInMeters,
//                                0.9f*GameState->World->TileDepthInMeters);
//                        Group->TotalVolume.OffsetP = V3(0, 0, 0.5f*DimZ);
//                        Group->TotalVolume.Dim = V3(DimX, DimY, DimZ);

                        Vector2f OffsetP = new Vector2f(0.0f, 0.0f);
                        Vector2f Dim = new Vector2f(WorldGenerator.tilesPerWidth * World.TileSideInMeters, WorldGenerator.tilesPerHeight * World.TileSideInMeters);
                        PushRectOutline(pieceGroup, OffsetP, Dim, new Vector4f(0, 0.5f, 1.0f, 1), 1.0f);
                    }
                    break;
                    default: {
                        throw new RuntimeException("INVALID PATH");
                    }
                }

                if (!IsSet(entity, SimEntityFlag.NONSPATIAL) && MoveSpec != null && IsSet(entity, SimEntityFlag.MOVEABLE)) {
                    moveEntity(gameMemory.simRegion, entity, gameMemory.ControlledHero.ddP, GameApp.globalinterval, MoveSpec);
                }

                List<Matrix4f> listOfTranslactions = IntStream.range(0, pieceGroup.PieceCount).mapToObj(index -> {
                    final Matrix4f v = new Matrix4f();
                    float EntityGroundPointX = World.ScreenCenterX + World.MetersToPixels * new Vector3f(entity.P).x() * 0.2f;
                    float EntityGroundPointY = World.ScreenCenterY - World.MetersToPixels * new Vector3f(entity.P).y() * 0.2f;

                    EntityVisiblePiece Piece = pieceGroup.Pieces[index];
//                Vector3f Center = new Vector3f(EntityGroundPointX + Piece.Offset.x(), EntityGroundPointY + Piece.Offset.y(), 0);
                    Vector3f Center = new Vector3f(EntityGroundPointX, EntityGroundPointY, 0);
                    Vector2f HalfDim = Piece.Dim.mul(0.5f * World.MetersToPixels, new Vector2f());
                    if (entity.Type == EntityType.SPACE) {
//                        Vector3f translation = new Vector3f(Center.sub(new Vector3f(HalfDim.x, HalfDim.y, 0).x,
//                                Center.add(new Vector3f(HalfDim.x, HalfDim.y, 0)).y, 0));
                        return v.identity().translate(Center.sub(new Vector3f(HalfDim.x, HalfDim.y, 0)));
                    }
//                    return Map.of(entity.Type, v.identity().translate(Center));
                    return v.identity().translate(Center);
                }).collect(Collectors.toList());

                return Map.of(entity.Type, listOfTranslactions);
            }
            return null;
        }).filter(map -> map != null).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
            return a.getKey();
        }, Collectors.flatMapping(a -> a.getValue().stream(), Collectors.toList())));

        float ScreenWidthInMeters = DisplayManager.WIDTH * World.PixelsToMeters;
        float ScreenHeightInMeters = DisplayManager.HEIGHT * World.PixelsToMeters;
        Rectangle CameraBoundsInMeters = Rectangle.RectCenterDim(new Vector3f(0, 0, 0),
                new Vector3f(ScreenWidthInMeters, ScreenHeightInMeters, 0.0f));

        // Debug drawing
        World.WorldPosition MinChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMinCorner(CameraBoundsInMeters));
        World.WorldPosition MaxChunkP = world.MapIntoChunkSpace(Camera.position, Rectangle.GetMaxCorner(CameraBoundsInMeters));
        Vector2f ScreenCenter = new Vector2f(DisplayManager.WIDTH * 0.5f, DisplayManager.HEIGHT * 0.5f);

        List<Matrix4f> blah = new ArrayList();
//        for(int ChunkY = MinChunkP.ChunkY; ChunkY <= MaxChunkP.ChunkY; ++ChunkY) {
//            for(int ChunkX = MinChunkP.ChunkX; ChunkX <= MaxChunkP.ChunkX; ++ChunkX) {
//                        World.WorldPosition ChunkCenterP = world.CenteredChunkPoint(ChunkX, ChunkY);
//                        Vector3f RelP = World.subtract(ChunkCenterP, Camera.position);
//                        Vector2f ScreenP = new Vector2f(ScreenCenter.x + World.MetersToPixels * RelP.x, ScreenCenter.y - World.MetersToPixels * RelP.y);
//                        Vector2f ScreenDim = new Vector2f(World.MetersToPixels * World.ChunkDimInMeters.x, World.MetersToPixels * World.ChunkDimInMeters.y);
//
//                        Matrix4f aa = new Matrix4f().identity().translate(new Vector3f(new Vector2f(ScreenP).sub(new Vector2f(ScreenDim).mul(0.5f)).x,
//                                new Vector2f(ScreenP).add(new Vector2f(ScreenDim).mul(0.5f)).y,
//                                0.0f));
//                        blah.add(aa);
////                        DrawRectangleOutline(DrawBuffer, ScreenP - 0.5f*ScreenDim, ScreenP + 0.5f*ScreenDim, V3(1.0f, 1.0f, 0.0f));
//                }
//            }
//        blah.add(new Matrix4f().identity().translate(new Vector3f(0.0f, 0.0f, 0.0f)));
//        aaa.put(EntityType.DEBUG, blah);
        return aaa;
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
        simRegion.UpdatableBounds = AddRadiusTo(Bounds, new Vector3f(simRegion.MaxEntityRadius, simRegion.MaxEntityRadius, simRegion.MaxEntityRadius));
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
                    World.WorldPosition NewCameraP = new World.WorldPosition(Camera.position);
                    NewCameraP = new World.WorldPosition(Stored.P);
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
                A.getMin().sub(Radius),
                A.getMax().add(Radius)
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
}
