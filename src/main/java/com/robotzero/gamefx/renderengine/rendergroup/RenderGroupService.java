package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.model.Texture;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class RenderGroupService {
    public RenderEntry PushRenderElement(RenderGroup Group, RenderGroupEntryType type) {
        if (Group.PushBufferSize + 1 < Group.MaxPushBufferSize) {
            RenderEntry entry;
            switch (type.toString().toLowerCase()) {
                case "bitmap": {
                    RenderEntryBitmap bitmap = new RenderEntryBitmap();
                    bitmap.Header = new RenderGroupEntryHeader();
                    bitmap.Header.Type = RenderGroupEntryType.BITMAP;
                    List<RenderEntry> bitmapList = Group.PushBufferBase.get(bitmap.Header.Type);
                    bitmapList.add(bitmap);
                    entry = bitmap;
                    break;
                }
                case "rectangle": {
                    RenderEntryRectangle rectangle = new RenderEntryRectangle();
                    rectangle.Header = new RenderGroupEntryHeader();
                    rectangle.Header.Type = RenderGroupEntryType.BITMAP;
                    List<RenderEntry> rectangleList = Group.PushBufferBase.get(rectangle.Header.Type);
                    rectangleList.add(rectangle);
                    entry = rectangle;
                    break;
                }
                case "clear": {
                    RenderEntryClear clear = new RenderEntryClear();
                    clear.Header = new RenderGroupEntryHeader();
                    clear.Header.Type = RenderGroupEntryType.CLEAR;
                    List<RenderEntry> clearList = Group.PushBufferBase.get(clear.Header.Type);
                    clearList.add(clear);
                    entry = clear;
                    break;
                }
                default: {
                    throw new RuntimeException("Buffer size exceeded");
                }
            }
            return entry;
        }
        throw new RuntimeException("Buffer size exceeded");
    }

    public void PushPiece(RenderGroup Group, Texture texture, Vector2f Offset, float OffsetZ, Vector2f Align, Vector2f Dim, Vector4f Color, float EntityZC, EntityType entityType) {
        RenderEntryBitmap piece = (RenderEntryBitmap) PushRenderElement(Group, RenderGroupEntryType.BITMAP);
        piece.EntityBasis = new RenderEntityBasis();
        piece.EntityBasis.Basis = Group.DefaultBasis;
        piece.Bitmap = texture;
        piece.EntityBasis.Offset = new Vector2f(Offset.x(), -Offset.y()).mul(Group.MetersToPixels).sub(Align);
        piece.EntityBasis.EntityZC = EntityZC;
        piece.Color = Color;
        piece.EntityBasis.OffsetZ = OffsetZ;
        piece.entityType = entityType;
    }

    public void pushBitmap(RenderGroup Group, Texture Bitmap, Vector2f Offset, float OffsetZ, Vector2f Align, float Alpha, float EntityZC, EntityType entityType) {
        PushPiece(Group, Bitmap, Offset, OffsetZ, Align, new Vector2f(0, 0f), new Vector4f(1.0f, 1.0f, 1.0f, Alpha), EntityZC, entityType);
    }

    public void pushRect(RenderGroup Group, Vector2f Offset, float OffsetZ, Vector2f Dim, Vector4f Color, float EntityZC, EntityType entityType) {
        RenderEntryRectangle piece = (RenderEntryRectangle) PushRenderElement(Group, RenderGroupEntryType.RECTANGLE);
        Vector2f HalfDim = new Vector2f(Dim).mul(Group.MetersToPixels).mul(0.5f);
        piece.EntityBasis = new RenderEntityBasis();
        piece.EntityBasis.Basis = Group.DefaultBasis;
        piece.EntityBasis.Offset = new Vector2f(Offset.x(), -Offset.y()).mul(Group.MetersToPixels).sub(HalfDim);
        piece.EntityBasis.EntityZC = EntityZC;
        piece.Color = Color;
        piece.EntityBasis.OffsetZ = OffsetZ;
        piece.entityType = entityType;
        piece.Dim = new Vector2f(Dim).mul(Group.MetersToPixels);
    }

    public void PushRectOutline(RenderGroup Group, Vector2f Offset, float OffsetZ, Vector2f Dim, Vector4f Color, float EntityZC, EntityType entityType) {
        float Thickness = 0.1f;

        // NOTE(casey): Top and bottom
        PushPiece(Group, null, Offset.sub(new Vector2f(0, 0.5f * Dim.y)), OffsetZ, new Vector2f(0, 0), new Vector2f(Dim.x, Thickness), Color, EntityZC, entityType);
        PushPiece(Group, null, Offset.add(new Vector2f(0, 0.5f * Dim.y)), OffsetZ, new Vector2f(0, 0), new Vector2f(Dim.x, Thickness), Color, EntityZC, entityType);

        // NOTE(casey): Left and right
        PushPiece(Group, null, Offset.sub(new Vector2f(0.5f * Dim.x, 0)), OffsetZ, new Vector2f(0, 0), new Vector2f(Thickness, Dim.y), Color, EntityZC, entityType);
        PushPiece(Group, null, Offset.add(new Vector2f(0.5f * Dim.x, 0)), OffsetZ, new Vector2f(0, 0), new Vector2f(Thickness, Dim.y), Color, EntityZC, entityType);
    }

    public RenderGroup AllocateRenderGroup(int MaxPushBufferSize, float MetersToPixels) {
        RenderGroup Result = new RenderGroup();
        Result.PushBufferBase = new HashMap<>();
        Result.PushBufferBase.put(RenderGroupEntryType.BITMAP, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.RECTANGLE, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.CLEAR, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.COORDINATE, new ArrayList<>());

        Result.DefaultBasis = new RenderBasis();
        Result.DefaultBasis.P = new Vector3f(0, 0, 0);
        Result.MetersToPixels = MetersToPixels;

        Result.MaxPushBufferSize = MaxPushBufferSize;
        Result.PushBufferSize = 0;

        return(Result);
    }

//    Map<EntityType, List<Vector3f>> aaa = IntStream.range(0, GameApp.renderGroup.PushBufferBase.size()).mapToObj(index -> {
//        EntityVisiblePiece Piece = GameApp.renderGroup.PushBufferBase.get(index);
//        Vector3f EntityBaseP = Piece.Basis.P;
//        float ZFudge = (1.0f + 0.1f * (EntityBaseP.z() + Piece.OffsetZ));
//        float EntityGroundPointX = GameApp.ScreenCenter.x + World.MetersToPixels * ZFudge * EntityBaseP.x();
//        float EntityGroundPointY = GameApp.ScreenCenter.y + World.MetersToPixels * ZFudge * EntityBaseP.y();
//        float EntityZ = -World.MetersToPixels * EntityBaseP.z();
//
//        Vector3f Center = new Vector3f(EntityGroundPointX + Piece.Offset.x(), EntityGroundPointY + Piece.Offset.y() + Piece.EntityZC * EntityZ, 0);
//        Vector2f HalfDim = Piece.Dim.mul(0.5f * World.MetersToPixels, new Vector2f());
//        if (Piece.entityType == EntityType.SPACE) {
////                        Vector3f translation = new Vector3f(Center.sub(new Vector3f(HalfDim.x, HalfDim.y, 0).x,
////                                Center.add(new Vector3f(HalfDim.x, HalfDim.y, 0)).y, 0));
////                        return v.identity().translate(Center.sub(new Vector3f(HalfDim.x, HalfDim.y, 0)));
//            return Map.of(Piece.entityType, Center.sub(new Vector3f(HalfDim.x, HalfDim.y, 0)));
//        }
////                    return Map.of(entity.Type, v.identity().translate(Center));
//        return Map.of(Piece.entityType, Center);
////                    return v.identity().translate(Center);
//    }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//        return a.getKey();
//    }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));
//
//        return aaa;

    public Map<EntityType, List<Vector3f>> RenderGroupToOutput(RenderGroup RenderGroup, float Width, float Height) {
        Vector2f ScreenCenter = new Vector2f(0.5f * GameApp.ScreenCenter.x, 0.5f * GameApp.ScreenCenter.y);

//        IntStream.range(0, GameApp.renderGroup.PushBufferBase.size()).mapToObj(index -> {
            List<RenderEntry> renderEntryBitmap = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.BITMAP);
            List<RenderEntry> renderEntryRectangle = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.RECTANGLE);
            List<RenderEntry> renderEntryClear = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.CLEAR);

            Map<EntityType, List<Vector3f>> collect1 = renderEntryBitmap.stream().map(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                Vector2f P = GetRenderEntityBasisP(RenderGroup, bitmap.EntityBasis, ScreenCenter);
                return Map.of(bitmap.entityType, new Vector3f(P, 0));
            }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
                return a.getKey();
            }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));

            Map<EntityType, List<Vector3f>> collect = renderEntryRectangle.stream().map(entry -> {
                RenderEntryRectangle rectangle = (RenderEntryRectangle) entry;
                Vector2f P = GetRenderEntityBasisP(RenderGroup, rectangle.EntityBasis, ScreenCenter);
//                DrawRectangle(OutputTarget, P, P + Entry -> Dim, Entry -> R, Entry -> G, Entry -> B);
                return Map.of(rectangle.entityType, new Vector3f(P, 0));
            }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
                return a.getKey();
            }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));


            return collect1;
//        });
    }

    public Vector2f GetRenderEntityBasisP(RenderGroup renderGroup, RenderEntityBasis EntityBasis, Vector2f ScreenCenter) {
        Vector3f EntityBaseP = EntityBasis.Basis.P;
        float ZFudge = (1.0f + 0.1f * (EntityBaseP.z + EntityBasis.OffsetZ));

        float EntityGroundPointX = ScreenCenter.x + renderGroup.MetersToPixels * ZFudge * EntityBaseP.x;
        float EntityGroundPointY = ScreenCenter.y + renderGroup.MetersToPixels * ZFudge * EntityBaseP.y;
        float EntityZ = -renderGroup.MetersToPixels * EntityBaseP.z;

        Vector2f Center = new Vector2f(EntityGroundPointX + EntityBasis.Offset.x,
                EntityGroundPointY - EntityBasis.Offset.y + EntityBasis.EntityZC * EntityZ);

        return(Center);
    }
}
