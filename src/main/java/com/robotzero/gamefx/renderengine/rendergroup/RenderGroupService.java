package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.model.Color;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.world.GameMemory;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.opengl.WGLARBContextFlushControl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

import static org.lwjgl.opengl.GL11.glFlush;

public class RenderGroupService {
    private final Renderer2D renderer2D;
    private final ExecutorService executorService;
    private final GameMemory gameMemory;

    public RenderGroupService(Renderer2D renderer2D, ExecutorService executor, GameMemory gameMemory) {
        this.renderer2D = renderer2D;
        this.executorService = executor;
        this.gameMemory = gameMemory;
    }

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

    public void pushBitmap(RenderGroup Group, LoadedBitmap Bitmap, float Height, Vector3f Offset, Vector4f Color, EntityType entityType) {
        RenderEntryBitmap piece = (RenderEntryBitmap) PushRenderElement(Group, RenderGroupEntryType.BITMAP);
        Vector3f P = new Vector3f(Offset);
        EntityBasisPResult EntityBasis = GetRenderEntityBasisP(Group.Transform, P);
        Vector2f Size = new Vector2f(Height * Bitmap.WidthOverHeight, Height);

        if (EntityBasis.Valid) {
            piece.Bitmap = Bitmap;
            piece.P = new Vector3f(EntityBasis.P, 0f);
            piece.entityType = entityType;
            piece.Color = Color;
            piece.Size = Size.mul(EntityBasis.Scale);
        }
    }

    public void PushRect(RenderGroup Group, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        RenderEntryRectangle piece = (RenderEntryRectangle) PushRenderElement(Group, RenderGroupEntryType.RECTANGLE);
        Vector3f P = new Vector3f(Offset).sub((new Vector3f(new Vector2f(Dim).mul(0.5f), 0)));

        EntityBasisPResult Basis = GetRenderEntityBasisP(Group.Transform, P);
        if (Basis.Valid) {
            piece.P = new Vector3f(Basis.P, 0);
            piece.Color = Color;
            piece.entityType = entityType;
            piece.Dim = Dim.mul(Basis.Scale);
        }
    }

    public void PushRectOutline(RenderGroup Group, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        float Thickness = 0.1f;

        // NOTE(casey): Top and bottom
        PushRect(Group, new Vector3f(Offset).sub(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);
        PushRect(Group, new Vector3f(Offset).add(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);

        // NOTE(casey): Left and right
        PushRect(Group, new Vector3f(Offset).sub(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
        PushRect(Group, new Vector3f(Offset).add(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
    }

    public RenderGroup AllocateRenderGroup(int MaxPushBufferSize) {
        RenderGroup Result = new RenderGroup();
        Result.PushBufferBase = new HashMap<>();
        Result.PushBufferBase.put(RenderGroupEntryType.BITMAP, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.RECTANGLE, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.CLEAR, new ArrayList<>());
        Result.PushBufferBase.put(RenderGroupEntryType.COORDINATE, new ArrayList<>());

        Result.MaxPushBufferSize = MaxPushBufferSize;
        Result.PushBufferSize = 0;
        Result.Transform = new RenderTransform();
        Result.Transform.OffsetP = new Vector3f(0.0f, 0.0f, 0.0f);
        Result.Transform.Scale = 1.0f;

        return(Result);
    }

//    public Map<EntityType, List<RenderData>> RenderGroupToOutput(RenderGroup RenderGroup) {
//        Vector2f ScreenDim = new Vector2f(DisplayManager.WIDTH, DisplayManager.HEIGHT);
//
//        List<RenderEntry> renderEntryBitmap = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.BITMAP);
//        List<RenderEntry> renderEntryRectangle = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.RECTANGLE);
//        List<RenderEntry> renderEntryClear = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.CLEAR);
//        List<RenderEntry> renderEntryPoints = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.COORDINATE);
//
//        Map<EntityType, List<RenderData>> collect1 = renderEntryBitmap.stream().map(entry -> {
//            RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
//            EntityBasisPResult P = GetRenderEntityBasisP(RenderGroup, bitmap.EntityBasis, ScreenDim);
//                return Map.of(bitmap.entityType, new RenderData(new Vector3f(P.P, 0), new Vector3f(P.P, 0).add(new Vector3f(bitmap.Size.x, bitmap.Size.y, 0).mul(P.Scale)), bitmap.Color));
//            }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//                return a.getKey();
//            }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));
//
//        Map<EntityType, List<RenderData>> collect = renderEntryRectangle.stream().map(entry -> {
//            RenderEntryRectangle rectangle = (RenderEntryRectangle) entry;
//            EntityBasisPResult P = GetRenderEntityBasisP(RenderGroup, rectangle.EntityBasis, ScreenDim);
////                DrawRectangle(OutputTarget, P, P + Entry -> Dim, Entry -> R, Entry -> G, Entry -> B);
//            return Map.of(rectangle.entityType, new RenderData(new Vector3f(P.P, 0), new Vector3f(P.P, 0).add((new Vector3f(rectangle.Dim.x, rectangle.Dim.y, 0).mul(P.Scale))), rectangle.Color));
////            return Map.of(rectangle.entityType, List.of(new Vector3f(P.P.mul(P.Scale), 0), new Vector3f(P.P.add(rectangle.Dim).mul(P.Scale), 0)));
//        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//            return a.getKey();
////        }, Collectors.flatMapping(a -> a.getValue().stream(), Collectors.toList())));
//        }, Collectors.mapping(a -> a.getValue(), Collectors.toList())));
//
//
//        Map<EntityType, List<RenderData>> collect3 = renderEntryPoints.stream().map(entry -> {
//            RenderEntryCoordinateSystem coordinateSystem = (RenderEntryCoordinateSystem) entry;
//            Vector2f Dim = new Vector2f(2, 2);
//            Vector2f P = coordinateSystem.Origin;
//            List<RenderData> points = new ArrayList<>();
//            points.add(new RenderData(new Vector3f(new Vector2f(P).sub(Dim), 0), new Vector3f(new Vector2f(P).add(Dim), 0), new Vector4f(1, 1, 1, 1)));
//            P = new Vector2f(coordinateSystem.Origin).add(coordinateSystem.XAxis);
//            points.add(new RenderData(new Vector3f(new Vector2f(P).sub(Dim), 0), new Vector3f(new Vector2f(P).add(Dim), 0), new Vector4f(1, 1, 1, 1)));
//            P = new Vector2f(coordinateSystem.Origin).add(coordinateSystem.YAxis);
//            points.add(new RenderData(new Vector3f(new Vector2f(P).sub(Dim), 0), new Vector3f(new Vector2f(P).add(Dim), 0), new Vector4f(1, 1, 1, 1)));
//
//            for (int Pindex = 0; Pindex < coordinateSystem.Points.length; ++Pindex) {
//                Vector2f PP = coordinateSystem.Points[Pindex];
//                Vector2f PPP = new Vector2f(coordinateSystem.Origin).add(new Vector2f(coordinateSystem.XAxis).mul(PP.x)).add(new Vector2f(coordinateSystem.YAxis).mul(PP.y));
//                points.add(new RenderData(new Vector3f(new Vector2f(PPP).sub(Dim), 0), new Vector3f(new Vector2f(PPP).add(Dim), 0), new Vector4f(1, 1, 1, 1)));
//            }
//            return Map.of(coordinateSystem.EntityType, points);
//        }).flatMap(matrixes -> matrixes.entrySet().stream()).collect(Collectors.groupingBy(a -> {
//            return a.getKey();
//        }, Collectors.flatMapping(a -> a.getValue().stream(), Collectors.toList())));
//
//        collect.forEach((type, list) -> {
//            collect1.merge(type, list, ((a, b) -> {
//                b.addAll(list);
//                return b;
//            }));
//        });
//
//        collect3.forEach((type, list) -> {
//            collect1.merge(type, list, ((a, b) -> {
//                b.addAll(list);
//                return b;
//            }));
//        });
//
//        return collect1;
//    }

    public EntityBasisPResult GetRenderEntityBasisP(RenderTransform Transform, Vector3f OriginalP) {
        EntityBasisPResult Result = new EntityBasisPResult();
        Vector3f P = new Vector3f(OriginalP).add(Transform.OffsetP);

        if (Transform.Orthographic) {
            Result.P = new Vector2f(Transform.ScreenCenter).add((new Vector2f(P.x, P.y).mul(Transform.MetersToPixels)));
            Result.Scale = Transform.MetersToPixels;
            Result.Valid = true;
        } else {
            float OffsetZ = 0.0f;
            float DistanceAboveTarget = Transform.DistanceAboveTarget;
//            DistanceAboveTarget += 50f;
            float DistanceToPZ = (DistanceAboveTarget - P.z);
            float NearClipPlane = 0.2f;

            Vector3f RawXY = new Vector3f(new Vector2f(P.x, P.y), 1.0f);

            if (DistanceToPZ > NearClipPlane) {
                Vector3f ProjectedXY = RawXY.mul((1.0f / DistanceToPZ) * Transform.FocalLength);
                Result.P = new Vector2f(Transform.ScreenCenter).add(new Vector2f(ProjectedXY.x, ProjectedXY.y).mul(Transform.MetersToPixels)).add(new Vector2f(0.0f, Result.Scale * OffsetZ));
                Result.Scale = ProjectedXY.z * Transform.MetersToPixels;
                Result.Valid = true;
            }
        }

        return Result;
    }

    public Vector2f Unproject(RenderGroup Group, Vector2f ProjectedXY, float AtDistanceFromCamera) {
        Vector2f WorldXY = new Vector2f(ProjectedXY).mul(AtDistanceFromCamera / Group.Transform.FocalLength);
        return(WorldXY);
    }

    public Rectangle GetCameraRectangleAtDistance(RenderGroup Group, float DistanceFromCamera) {
        Vector2f RawXY = Unproject(Group, Group.MonitorHalfDimInMeters, DistanceFromCamera);

        Rectangle Result = Rectangle.RectCenterHalfDim(new Vector3f(0, 0, 0), new Vector3f(RawXY, 0f));

        return(Result);
    }

    public Rectangle GetCameraRectangleAtTarget(RenderGroup Group) {
        Rectangle Result = GetCameraRectangleAtDistance(Group, Group.Transform.DistanceAboveTarget);

        return(Result);
    }

//    public void TiledRenderGroupToOutput(RenderGroup renderGroup, LoadedBitmap OutputTarget, int Even) {
//        renderer2D.clear();
//        renderer2D.begin();
//        int TileCountX = 4;
//        int TileCountY = 4;
//
//        // TODO(casey): Make sure that allocator allocates enough space so we can round these?
//        // TODO(casey): Round to 4??
//        int TileWidth = OutputTarget.Width / TileCountX;
//        int TileHeight = OutputTarget.Height / TileCountY;
//        for(int TileY = 0; TileY < TileCountY; ++TileY) {
//            for(int TileX = 0; TileX < TileCountX; ++TileX)
//            {
//                float MinX = TileX * TileWidth + 4;
//                float MaxX = MinX + TileWidth - 4;
//                float MinY = TileY * TileHeight + 4;
//                float MaxY = MinY + TileHeight - 4;
//
//                Rectangle ClipRect = new Rectangle(new Vector3f(MinX, MinY, 0), new Vector3f(MaxX, MaxY, 0));
//
//                render(renderGroup, OutputTarget, ClipRect, Even);
//            }
//        }
//        renderer2D.end();
//    }

    public void render(RenderGroup RenderGroup, LoadedBitmap output, Rectangle ClipRect, int Even) {
        if (RenderGroup != null && !RenderGroup.PushBufferBase.isEmpty()) {
            renderer2D.clear();
            renderer2D.begin();

            List<RenderEntry> renderEntryBitmap = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.BITMAP);
            List<RenderEntry> renderEntryRectangle = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.RECTANGLE);
            List<RenderEntry> renderEntryClear = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.CLEAR);
            List<RenderEntry> renderEntryPoints = GameApp.renderGroup.PushBufferBase.get(RenderGroupEntryType.COORDINATE);

            renderEntryBitmap.stream().filter(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                return bitmap.entityType == EntityType.HERO;
            }).forEach(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                Vector3f Max = new Vector3f(bitmap.P).add(new Vector3f(bitmap.Size, 0f));
                if (bitmap.Bitmap.texture == null) {
                    gameMemory.gameAssets.get("fred_01.png").createTexture();
                    bitmap.Bitmap.texture = gameMemory.gameAssets.get("fred_01.png").getTexture();
                }
                bitmap.Bitmap.texture.bind();
                renderer2D.drawTextureRegion(bitmap.P.x, bitmap.P.y, Max.x, Max.y, 0, 0, 1, 1, 1.0f, new Color(bitmap.Color.x, bitmap.Color.y, bitmap.Color.z));
                bitmap.Bitmap.texture.unbind();
            });

            renderEntryBitmap.stream().filter(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                return bitmap.entityType != EntityType.HERO;
            }).forEach(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                if (bitmap.Bitmap.texture == null) {
                    gameMemory.gameAssets.get("bird.png").createTexture();
                    bitmap.Bitmap.texture = gameMemory.gameAssets.get("bird.png").getTexture();
                }
                bitmap.Bitmap.texture.bind();
                Vector3f Max = new Vector3f(bitmap.P).add(new Vector3f(bitmap.Size, 0f));
                renderer2D.drawTextureRegion(bitmap.P.x, bitmap.P.y, Max.x, Max.y, 0, 0, 1, 1, 1.0f, new Color(bitmap.Color.x, bitmap.Color.y, bitmap.Color.z));
                bitmap.Bitmap.texture.unbind();

            });

            renderEntryRectangle.forEach(entry -> {
                RenderEntryRectangle rectangle = (RenderEntryRectangle) entry;
                Vector3f Max = new Vector3f(rectangle.P).add(new Vector3f(rectangle.Dim, 0));
                renderer2D.drawTextureRegion(rectangle.P.x, rectangle.P.y, Max.x, Max.y, 0, 0, 1, 1, 0.0f, new Color(rectangle.Color.x, rectangle.Color.y, rectangle.Color.z));
            });
            renderer2D.end();
        }
    }

    public static<T> CompletableFuture<List<T>> sequence(List<CompletableFuture<T>> com) {
        return CompletableFuture.allOf(com.toArray(new CompletableFuture<?>[0]))
                .thenApply(v -> com.stream()
                        .map(CompletableFuture::join)
                        .collect(Collectors.toList())
                );
    }

    private Rectangle getIntersect(Vector2f Min, Vector2f Max, Rectangle ClipRect, int Even) {
        Rectangle FillRectTemp = new Rectangle(new Vector3f(Min, 0), new Vector3f(Max, 0));
        Rectangle fillRect = Rectangle.Intersect(FillRectTemp, ClipRect);

        if(Even != (Math.round(fillRect.getMin().y) & 1)) {
            fillRect.setMiniy(fillRect.getMini().y + 1);
        }

        return fillRect;
    }

    public void Perspective(RenderGroup renderGroup, int PixelWidth, int PixelHeight, float MetersToPixels, float FocalLength, float DistanceAboveTarget) {
        float PixelsToMeters = 1.0f / MetersToPixels;

        renderGroup.MonitorHalfDimInMeters = new Vector2f(0.5f * PixelWidth * PixelsToMeters, 0.5f * PixelHeight*PixelsToMeters);

        renderGroup.Transform.MetersToPixels = MetersToPixels;
        renderGroup.Transform.FocalLength =  FocalLength;
        renderGroup.Transform.DistanceAboveTarget = DistanceAboveTarget;
        renderGroup.Transform.ScreenCenter = new Vector2f(0.5f * PixelWidth, 0.5f * PixelHeight);

        renderGroup.Transform.Orthographic = false;
    }

    public void Orthographic(RenderGroup renderGroup, int PixelWidth, int PixelHeight, float MetersToPixels) {
        float PixelsToMeters = 1.0f / MetersToPixels;
        renderGroup.MonitorHalfDimInMeters = new Vector2f(0.5f * PixelWidth*PixelsToMeters,0.5f*PixelHeight*PixelsToMeters);

        renderGroup.Transform.MetersToPixels = MetersToPixels;
        renderGroup.Transform.FocalLength = 1.0f;
        renderGroup.Transform.DistanceAboveTarget = 1.0f;
        renderGroup.Transform.ScreenCenter = new Vector2f(0.5f * PixelWidth, 0.5f * PixelHeight);

        renderGroup.Transform.Orthographic = true;
    }
}
