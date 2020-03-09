package com.robotzero.gamefx.renderengine.entity;

import com.badlogic.gdx.math.Vector2;
import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Camera;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.GameMemory;
import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import com.robotzero.gamefx.world.WorldEntityBlock;
import imgui.Col;
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

        entity.Low.Height = 1.4f;
        entity.Low.Width = 1.0f;
        entity.Low.Collides = true;

        if (gameMemory.CameraFollowingEntityIndex == 0) {
            gameMemory.CameraFollowingEntityIndex = entity.LowIndex;
        }

        return entity;
    }

    public AddLowEntityResult AddLowEntity(EntityType Type, World.WorldPosition P) {
        int EntityIndex = gameMemory.LowEntityCount;
        gameMemory.LowEntityCount = EntityIndex + 1;

        gameMemory.LowEntities[EntityIndex] = new Entity.LowEntity();
        gameMemory.LowEntities[EntityIndex].Type = Type;

        //@TODO
        if (P != null) {
            gameMemory.LowEntities[EntityIndex].P = new World.WorldPosition(P);
            ChangeEntityLocation(EntityIndex, null, P);
        }

        AddLowEntityResult addLowEntityResult = new AddLowEntityResult();
        addLowEntityResult.Low = gameMemory.LowEntities[EntityIndex];
        addLowEntityResult.LowIndex = EntityIndex;
        return addLowEntityResult;
    }

    public AddLowEntityResult AddWall(int ChunkX, int ChunkY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(ChunkX, ChunkY);
        AddLowEntityResult entity = AddLowEntity(EntityType.WALL, P);

        entity.Low.Height = World.TileSideInMeters;
        entity.Low.Width = entity.Low.Height;
        entity.Low.Collides = true;

        return entity;
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

    public Entity ForceEntityIntoHigh(int LowIndex) {
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

    public Entity EntityFromHighIndex(int HighEntityIndex) {
        Entity Result = null;

        if (HighEntityIndex > 0) {
            assert(HighEntityIndex < gameMemory.HighEntities.length);
            Entity.HighEntity High = gameMemory.HighEntities[HighEntityIndex];
            if (High != null) {
                Result = new Entity();
                Result.High = High;
                Result.LowIndex = Result.High.LowEntityIndex;
                Result.Low = gameMemory.LowEntities[Result.LowIndex];
            }
        }

        return(Result);
    }

    public AddLowEntityResult AddMonstar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY);
        AddLowEntityResult Entity = AddLowEntity(EntityType.MONSTAR, P);

        Entity.Low.Height = 0.5f;
        Entity.Low.Width = 1.0f;
        Entity.Low.Collides = true;

        return(Entity);
    }

    public AddLowEntityResult AddFamiliar(int AbsTileX, int AbsTileY) {
        World.WorldPosition P = ChunkPositionFromTilePosition(AbsTileX, AbsTileY);
        AddLowEntityResult Entity = AddLowEntity(EntityType.FAMILIAR, P);

        Entity.Low.Height = 0.5f;
        Entity.Low.Width = 1.0f;
        Entity.Low.Collides = true;

        return(Entity);
    }

    public void UpdateFamiliar(Entity Entity, float dt) {
        Entity ClosestHero = null;
        float ClosestHeroDSq = (float) Math.pow(10.0f, 2);
        for (int HighEntityIndex = 1; HighEntityIndex < gameMemory.HighEntityCount; ++HighEntityIndex) {
            Entity TestEntity = EntityFromHighIndex(HighEntityIndex);
            assert (TestEntity != null);
                if (TestEntity.Low.Type == EntityType.HERO) {
                    float TestDSq = new Vector2f(TestEntity.High.P).sub(new Vector2f(Entity.High.P)).lengthSquared();
                    if (TestEntity.Low.Type == EntityType.HERO) {
                        TestDSq *= 0.75f;
                    }

                    if (ClosestHeroDSq > TestDSq) {
                        ClosestHero = TestEntity;
                        ClosestHeroDSq = TestDSq;
                    }
                }
        }

        Vector2f ddP = new Vector2f();
        if (ClosestHero != null && ClosestHero.High != null && ClosestHeroDSq > Math.pow(3.0f, 2.0f)) {
            float Acceleration = 0.5f;
            float OneOverLength = (float) (Acceleration / Math.sqrt(ClosestHeroDSq));
            ddP = new Vector2f(ClosestHero.High.P).sub(new Vector2f(Entity.High.P)).mul(OneOverLength);
        }

        moveEntity(Entity, ddP, dt, GameApp.playerSpeed);
    }

    public void UpdateMonstar(Entity entity, float dt) {
    }

    public void moveEntity(Entity entity, Vector2f ddP, float interval, int playerSpeed) {
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

        World.WorldPosition NewP = world.MapIntoChunkSpace(entity.Low.Type == EntityType.FAMILIAR ? Camera.oldPosition : Camera.position, entity.High.P);
        ChangeEntityLocation(entity.LowIndex, entity.Low.P, NewP);
        entity.Low.P = NewP;
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

        return IntStream.range(1, gameMemory.HighEntityCount).mapToObj(HighEntityIndex -> {
            pieceGroup.PieceCount = 0;
            Entity.HighEntity highEntity = gameMemory.HighEntities[HighEntityIndex];
            Entity.LowEntity lowEntity = gameMemory.LowEntities[highEntity.LowEntityIndex];

            Entity entity = new Entity();
            entity.LowIndex = highEntity.LowEntityIndex;
            entity.Low = lowEntity;
            entity.High = highEntity;
            switch (lowEntity.Type.name().toLowerCase()) {
                case ("wall") : {
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(40f, 80f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("hero") : {
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("familiar"): {
                    UpdateFamiliar(entity, GameApp.globalinterval);
                    entity.High.tbob = entity.High.tbob + GameApp.globalinterval;
                    if (entity.High.tbob > 2.0f * Math.PI) {
                        entity.High.tbob = (float) (entity.High.tbob - (2.0f * Math.PI));
                    }
                    pushPiece(pieceGroup, new Vector2f(0.0f, 0.0f), new Vector2f(72f, 182f), new Vector2f(0, 0), new Vector4f(0, 0, 0, 0), 0f);
                } break;
                case ("monstar"): {

                } break;
                default: {
                    throw new RuntimeException("INVALID PATH");
                }
            }

            highEntity.P = new Vector2f(highEntity.P);
            final Matrix4f v = new Matrix4f();
            float EntityGroundPointX = World.ScreenCenterX + World.MetersToPixels * highEntity.P.x();
            float EntityGroundPointY = World.ScreenCenterY - World.MetersToPixels * highEntity.P.y();
//            float PlayerLeft = EntityGroundPointX - 0.5f * World.MetersToPixels * lowEntity.Width;
//            float PlayerTop = EntityGroundPointY - 0.5f * World.MetersToPixels * lowEntity.Height;

            EntityVisiblePiece Piece = pieceGroup.Pieces[0];
            Vector2f Center = new Vector2f(EntityGroundPointX + Piece.Offset.x(), EntityGroundPointY + Piece.Offset.y());
            Vector2f HalfDim = Piece.Dim.mul(0.5f * World.MetersToPixels, new Vector2f(0, 0f));

            return Map.of(lowEntity.Type, v.identity().translate(new Vector3f(Center.sub(HalfDim).x(), Center.add(HalfDim).y(), 0f)));

//            return Map.of(lowEntity.Type, v.identity().translate(new Vector3f(PlayerLeft, PlayerTop, 0f)));
        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
            return a.getKey();
        }));
    }
}
