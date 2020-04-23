package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.math.Rectangle;
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
                    rectangle.Header.Type = RenderGroupEntryType.RECTANGLE;
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
                case "coordinate": {
                    RenderEntryCoordinateSystem coordinateSystem = new RenderEntryCoordinateSystem();
                    coordinateSystem.Header = new RenderGroupEntryHeader();
                    coordinateSystem.Header.Type = RenderGroupEntryType.COORDINATE;
                    List<RenderEntry> coordinateList = Group.PushBufferBase.get(coordinateSystem.Header.Type);
                    coordinateList.add(coordinateSystem);
                    entry = coordinateSystem;
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

    public void pushBitmap(RenderGroup Group, Texture Bitmap, Vector2f Dim, float Height, Vector3f Offset, Vector4f Color, EntityType entityType) {
        RenderEntryBitmap piece = (RenderEntryBitmap) PushRenderElement(Group, RenderGroupEntryType.BITMAP);
        Vector2f Size = new Vector2f((Dim.x() / Dim.y()) * Height, Height);
        piece.EntityBasis = new RenderEntityBasis();
        piece.EntityBasis.Basis = Group.DefaultBasis;
        piece.Bitmap = Bitmap;
        piece.EntityBasis.Offset = new Vector3f(Offset);
        piece.entityType = entityType;
        piece.Color = Color;
        piece.Size = Size;
    }

    public void PushRect(RenderGroup Group, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        RenderEntryRectangle piece = (RenderEntryRectangle) PushRenderElement(Group, RenderGroupEntryType.RECTANGLE);
        piece.EntityBasis = new RenderEntityBasis();
        piece.EntityBasis.Basis = Group.DefaultBasis;
        piece.EntityBasis.Offset = new Vector3f(Offset).sub((new Vector3f(Dim, 0).mul(0.5f)));
        piece.Color = Color;
        piece.entityType = entityType;
        piece.Dim = Dim;
    }

    public void PushRectOutline(RenderGroup Group, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        float Thickness = 0.1f;

        // NOTE(casey): Top and bottom
        PushRect(Group, Offset.sub(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);
        PushRect(Group, Offset.add(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);

        // NOTE(casey): Left and right
        PushRect(Group, Offset.sub(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
        PushRect(Group, Offset.add(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
    }

    public RenderGroup AllocateRenderGroup(int MaxPushBufferSize, int ResolutionPixelsX, int ResolutionPixelsY) {
        float WidthOfMonitor = 0.635f;
        RenderGroup Result = new RenderGroup();
        Result.PushBufferBase = new HashMap<>();
        Result.PushBufferBase.put(RenderGroupEntryType.BITMAP, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.RECTANGLE, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.CLEAR, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.COORDINATE, new ArrayList<>());

        Result.DefaultBasis = new RenderBasis();
        Result.DefaultBasis.P = new Vector3f(0, 0, 0);

        Result.GameCamera = new RenderGroupCamera();
        Result.GameCamera.FocalLength = 0.6f;
        Result.GameCamera.DistanceAboveTarget = 9.0f;
        Result.RenderCamera = Result.GameCamera;
        Result.RenderCamera.DistanceAboveTarget = 30.0f;
        Result.MetersToPixels = ResolutionPixelsX * WidthOfMonitor;
        float PixelsToMeters = 1.0f / Result.MetersToPixels;
        Result.MonitorHalfDimInMeters = new Vector2f(0.5f * ResolutionPixelsX * PixelsToMeters,
                0.5f * ResolutionPixelsY * PixelsToMeters);
        Result.MaxPushBufferSize = MaxPushBufferSize;
        Result.PushBufferSize = 0;

        return(Result);
    }

    public Map<EntityType, List<RenderData>> RenderGroupToOutput(RenderGroup RenderGroup) {
        Vector2f ScreenDim = new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT);

        List<RenderEntry> renderEntryBitmap = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.BITMAP);
        List<RenderEntry> renderEntryRectangle = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.RECTANGLE);
        List<RenderEntry> renderEntryClear = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.CLEAR);
        List<RenderEntry> renderEntryPoints = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.COORDINATE);

        Map<EntityType, List<RenderData>> collect1 = renderEntryBitmap.stream().map(entry -> {
            RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
            EntityBasisPResult P = GetRenderEntityBasisP(RenderGroup, bitmap.EntityBasis, ScreenDim);
//                return Map.of(bitmap.entityType, new Vector3f(P.P.mul(P.Scale), 0));
                return Map.of(bitmap.entityType, new RenderData(new Vector3f(P.P, 0), new Vector3f(P.P, 0).add(new Vector3f(bitmap.Size.x, bitmap.Size.y, 0).mul(P.Scale))));
            }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
                return a.getKey();
            }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));

        Map<EntityType, List<RenderData>> collect = renderEntryRectangle.stream().map(entry -> {
            RenderEntryRectangle rectangle = (RenderEntryRectangle) entry;
            EntityBasisPResult P = GetRenderEntityBasisP(RenderGroup, rectangle.EntityBasis, ScreenDim);
//                DrawRectangle(OutputTarget, P, P + Entry -> Dim, Entry -> R, Entry -> G, Entry -> B);
            return Map.of(rectangle.entityType, new RenderData(new Vector3f(P.P, 0), new Vector3f(P.P, 0).add(new Vector3f(rectangle.Dim.x(), rectangle.Dim.y(), 0).mul(P.Scale))));
//            return Map.of(rectangle.entityType, List.of(new Vector3f(P.P.mul(P.Scale), 0), new Vector3f(P.P.add(rectangle.Dim).mul(P.Scale), 0)));
        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
            return a.getKey();
//        }, Collectors.flatMapping(a -> a.getValue().stream(), Collectors.toList())));
        }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));


//        Map<EntityType, List<Vector3f>> collect3 = renderEntryPoints.stream().map(entry -> {
//            RenderEntryCoordinateSystem coordinateSystem = (RenderEntryCoordinateSystem) entry;
//            Vector2f Dim = new Vector2f(2, 2);
//            Vector2f P = coordinateSystem.Origin;
//            List<Vector3f> points = new ArrayList<>();
//            points.add(new Vector3f(new Vector2f(P).sub(Dim), 0));
//            points.add(new Vector3f(new Vector2f(P).add(Dim), 0));
//            P = new Vector2f(coordinateSystem.Origin).add(coordinateSystem.XAxis);
//            points.add(new Vector3f(new Vector2f(P).sub(Dim), 0));
//            points.add(new Vector3f(new Vector2f(P).add(Dim), 0));
//
//            P = new Vector2f(coordinateSystem.Origin).add(coordinateSystem.YAxis);
//            points.add(new Vector3f(new Vector2f(P).sub(Dim), 0));
//            points.add(new Vector3f(new Vector2f(P).add(Dim), 0));
//
//            for (int Pindex = 0; Pindex < coordinateSystem.Points.length; ++Pindex) {
//                Vector2f PP = coordinateSystem.Points[Pindex];
//                Vector2f PPP = new Vector2f(coordinateSystem.Origin).add(new Vector2f(coordinateSystem.XAxis).mul(PP.x)).add(new Vector2f(coordinateSystem.YAxis).mul(PP.y));
//                points.add(new Vector3f(new Vector2f(PPP).sub(Dim), 0));
//                points.add(new Vector3f(new Vector2f(PPP).add(Dim), 0));
//            }
//            return Map.of(coordinateSystem.EntityType, points);
//        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//            return a.getKey();
//        }, Collectors.flatMapping(a -> a.getValue().stream(), Collectors.toList())));

        collect.forEach((type, list) -> {
            collect1.merge(type, list, ((a, b) -> {
                b.addAll(list);
                return b;
            }));
        });

//        collect3.forEach((type, list) -> {
//            collect1.merge(type, list, ((a, b) -> {
//                b.addAll(list);
//                return b;
//            }));
//        });

        return collect1;
    }

    public EntityBasisPResult GetRenderEntityBasisP(RenderGroup renderGroup, RenderEntityBasis EntityBasis, Vector2f ScreenDim) {
        EntityBasisPResult Result = new EntityBasisPResult();
        Vector2f ScreenCenter = new Vector2f(ScreenDim).mul(0.5f);

        Vector3f EntityBaseP = new Vector3f(EntityBasis.Basis.P);

        float DistanceToPZ = (renderGroup.RenderCamera.DistanceAboveTarget - EntityBaseP.z);
        float NearClipPlane = 0.2f;

        Vector3f RawXY = new Vector3f(new Vector2f(EntityBaseP.x, EntityBaseP.y).add(new Vector2f(EntityBasis.Offset.x, EntityBasis.Offset.y)), 1.0f);

        if (DistanceToPZ > NearClipPlane) {
            Vector3f ProjectedXY = RawXY.mul((1.0f / DistanceToPZ) * renderGroup.RenderCamera.FocalLength);
            Result.P = new Vector2f(ScreenCenter).add(new Vector2f(ProjectedXY.x, ProjectedXY.y).mul(renderGroup.MetersToPixels));
            Result.Scale = ProjectedXY.z * renderGroup.MetersToPixels;
            Result.Valid = true;
        }

        return Result;
    }

    public Vector2f Unproject(RenderGroup Group, Vector2f ProjectedXY, float AtDistanceFromCamera) {
        Vector2f WorldXY = ProjectedXY.mul(AtDistanceFromCamera / Group.GameCamera.FocalLength);
        return(WorldXY);
    }

    public Rectangle GetCameraRectangleAtDistance(RenderGroup Group, float DistanceFromCamera) {
        Vector2f RawXY = Unproject(Group, Group.MonitorHalfDimInMeters, DistanceFromCamera);

        Rectangle Result = Rectangle.RectCenterHalfDim(new Vector3f(0, 0, 0), new Vector3f(RawXY, 0f));

        return(Result);
    }

    public Rectangle GetCameraRectangleAtTarget(RenderGroup Group) {
        Rectangle Result = GetCameraRectangleAtDistance(Group, Group.GameCamera.DistanceAboveTarget);

        return(Result);
    }
}
