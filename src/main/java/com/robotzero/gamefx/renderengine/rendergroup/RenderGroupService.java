package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.GameApp;
import com.robotzero.gamefx.renderengine.Renderer2D;
import com.robotzero.gamefx.renderengine.entity.CameraTransform;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.entity.ObjectTransform;
import com.robotzero.gamefx.renderengine.math.Calc;
import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.renderengine.model.Color;
import com.robotzero.gamefx.world.GameMemory;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Collectors;

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
        if (Group.gameRenderCommands.PushBufferSize + 1 < GameRenderCommands.MaxPushBufferSize) {
            RenderEntry entry;
            switch (type.toString().toLowerCase()) {
                case "bitmap": {
                    RenderEntryBitmap bitmap = new RenderEntryBitmap();
                    bitmap.Header = new RenderGroupEntryHeader();
                    bitmap.Header.Type = RenderGroupEntryType.BITMAP;
                    List<RenderEntry> bitmapList = Group.gameRenderCommands.PushBuffer.get(bitmap.Header.Type);
                    bitmapList.add(bitmap);
                    entry = bitmap;
                    break;
                }
                case "rectangle": {
                    RenderEntryRectangle rectangle = new RenderEntryRectangle();
                    rectangle.Header = new RenderGroupEntryHeader();
                    rectangle.Header.Type = RenderGroupEntryType.RECTANGLE;
                    List<RenderEntry> rectangleList = Group.gameRenderCommands.PushBuffer.get(rectangle.Header.Type);
                    rectangleList.add(rectangle);
                    entry = rectangle;
                    break;
                }
                case "clear": {
                    RenderEntryClear clear = new RenderEntryClear();
                    clear.Header = new RenderGroupEntryHeader();
                    clear.Header.Type = RenderGroupEntryType.CLEAR;
                    List<RenderEntry> clearList = Group.gameRenderCommands.PushBuffer.get(clear.Header.Type);
                    clearList.add(clear);
                    entry = clear;
                    break;
                }
                case "coordinate": {
                    RenderEntryCoordinateSystem coordinateSystem = new RenderEntryCoordinateSystem();
                    coordinateSystem.Header = new RenderGroupEntryHeader();
                    coordinateSystem.Header.Type = RenderGroupEntryType.COORDINATE;
                    List<RenderEntry> coordinateList = Group.gameRenderCommands.PushBuffer.get(coordinateSystem.Header.Type);
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

    public void pushBitmap(RenderGroup Group, ObjectTransform ObjectTransform, LoadedBitmap Bitmap, float Height, Vector3f Offset, Vector4f Color, float CAlign, EntityType entityType) {
        UsedBitmapDim Dim = GetBitmapDim(Group, ObjectTransform, Bitmap, Height, Offset, CAlign);
        if (Dim.Basis.Valid) {
            RenderEntryBitmap piece = (RenderEntryBitmap) PushRenderElement(Group, RenderGroupEntryType.BITMAP);
            piece.Bitmap = Bitmap;
            piece.P = new Vector3f(Dim.Basis.P, 0f);
            piece.entityType = entityType;
            piece.Color = Color;
            piece.Size = Dim.Size.mul(Dim.Basis.Scale);

        }
    }

    public void PushRect(RenderGroup Group, ObjectTransform objectTransform, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        Vector3f P = new Vector3f(Offset).sub((new Vector3f(new Vector2f(Dim).mul(0.5f), 0)));
        EntityBasisPResult Basis = GetRenderEntityBasisP(Group.CameraTransform, objectTransform, P);
        if (Basis.Valid) {
            RenderEntryRectangle piece = (RenderEntryRectangle) PushRenderElement(Group, RenderGroupEntryType.RECTANGLE);
            piece.P = new Vector3f(Basis.P, 0);
            piece.Color = Color;
            piece.entityType = entityType;
            piece.Dim = Dim.mul(Basis.Scale);
        }
    }

    public void PushRectOutline(RenderGroup Group, ObjectTransform objectTransform, Vector3f Offset, Vector2f Dim, Vector4f Color, EntityType entityType) {
        float Thickness = 0.1f;

        // NOTE(casey): Top and bottom
        PushRect(Group, objectTransform, new Vector3f(Offset).sub(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);
        PushRect(Group, objectTransform, new Vector3f(Offset).add(new Vector3f(0, 0.5f * Dim.y, 0)), new Vector2f(Dim.x, Thickness), Color, entityType);

        // NOTE(casey): Left and right
        PushRect(Group, objectTransform, new Vector3f(Offset).sub(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
        PushRect(Group, objectTransform, new Vector3f(Offset).add(new Vector3f(0.5f * Dim.x, 0, 0)), new Vector2f(Thickness, Dim.y), Color, entityType);
    }

    public EntityBasisPResult GetRenderEntityBasisP(CameraTransform CameraTransform, ObjectTransform ObjectTransform, Vector3f OriginalP) {
        EntityBasisPResult Result = new EntityBasisPResult();
        Vector3f P = new Vector3f(OriginalP).add(ObjectTransform.OffsetP);

        if (CameraTransform.Orthographic) {
            Result.P = new Vector2f(CameraTransform.ScreenCenter).add((new Vector2f(P.x, P.y).mul(CameraTransform.MetersToPixels)));
            Result.Scale = CameraTransform.MetersToPixels;
            Result.Valid = true;
        } else {
            float OffsetZ = 0.0f;
            float DistanceAboveTarget = CameraTransform.DistanceAboveTarget;
//            DistanceAboveTarget += 50f;
            float DistanceToPZ = (DistanceAboveTarget - P.z);
            float NearClipPlane = 0.1f;

            Vector3f RawXY = new Vector3f(new Vector2f(P.x, P.y), 1.0f);

            if (DistanceToPZ > NearClipPlane) {
                Vector3f ProjectedXY = RawXY.mul((1.0f / DistanceToPZ) * CameraTransform.FocalLength);
                Result.P = new Vector2f(CameraTransform.ScreenCenter).add(new Vector2f(ProjectedXY.x, ProjectedXY.y).mul(CameraTransform.MetersToPixels)).add(new Vector2f(0.0f, Result.Scale * OffsetZ));
                Result.Scale = ProjectedXY.z * CameraTransform.MetersToPixels;
                Result.Valid = true;
            }
        }

        return Result;
    }

    public Vector3f Unproject(RenderGroup Group, ObjectTransform ObjectTransform, Vector2f PixelsXY) {
        CameraTransform Transform = Group.CameraTransform;

        Vector2f UnprojectedXY;
        if(Transform.Orthographic) {
            UnprojectedXY = PixelsXY.sub(new Vector2f(Transform.ScreenCenter)).mul(1.0f / Transform.MetersToPixels);
        } else {
            Vector2f A = PixelsXY.sub(new Vector2f(Transform.ScreenCenter)).mul(1.0f / Transform.MetersToPixels);
            UnprojectedXY = A.mul((Transform.DistanceAboveTarget - ObjectTransform.OffsetP.z) / Transform.FocalLength);
        }

        Vector3f Result = new Vector3f(UnprojectedXY, ObjectTransform.OffsetP.z);
        Result = new Vector3f(Result).sub(ObjectTransform.OffsetP);

        return(Result);
    }

    public Vector2f UnprojectOld(RenderGroup Group, Vector2f ProjectedXY, float AtDistanceFromCamera) {
        Vector2f WorldXY = new Vector2f(ProjectedXY).mul(AtDistanceFromCamera / Group.CameraTransform.FocalLength);
        return(WorldXY);
    }

    public Rectangle GetCameraRectangleAtDistance(RenderGroup Group, float DistanceFromCamera) {
        Vector2f RawXY = UnprojectOld(Group, Group.MonitorHalfDimInMeters, DistanceFromCamera);

        Rectangle Result = Rectangle.RectCenterHalfDim(new Vector3f(0, 0, 0), new Vector3f(RawXY, 0f));

        return(Result);
    }

    public Rectangle GetCameraRectangleAtTarget(RenderGroup Group) {
        Rectangle Result = GetCameraRectangleAtDistance(Group, Group.CameraTransform.DistanceAboveTarget);

        return(Result);
    }

    public void render(RenderGroup RenderGroup, Rectangle ClipRect, int Even) {
        if (RenderGroup != null && !RenderGroup.gameRenderCommands.PushBuffer.isEmpty() && !RenderGroup.Assets.isEmpty()) {
            renderer2D.clear();

            List<RenderEntry> renderEntryBitmap = GameApp.renderGroup.gameRenderCommands.PushBuffer.get(RenderGroupEntryType.BITMAP);
            List<RenderEntry> renderEntryRectangle = GameApp.renderGroup.gameRenderCommands.PushBuffer.get(RenderGroupEntryType.RECTANGLE);
            List<RenderEntry> renderEntryClear = GameApp.renderGroup.gameRenderCommands.PushBuffer.get(RenderGroupEntryType.CLEAR);
            List<RenderEntry> renderEntryPoints = GameApp.renderGroup.gameRenderCommands.PushBuffer.get(RenderGroupEntryType.COORDINATE);

            RenderGroup.Assets.get("fred_01.png").getTexture().bind();
            renderer2D.begin();
            renderEntryBitmap.stream().filter(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                return bitmap.entityType == EntityType.HERO;
            }).findFirst().ifPresent(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                Vector3f Max = new Vector3f(bitmap.P).add(new Vector3f(bitmap.Size, 0f));
                renderer2D.drawTextureRegion(bitmap.P.x, bitmap.P.y, Max.x, Max.y, 0, 0, 1, 1, new Color(bitmap.Color.x, bitmap.Color.y, bitmap.Color.z));
            });

            renderer2D.end();
            RenderGroup.Assets.get("tree00.bmp").getTexture().bind();
            renderer2D.begin();

            renderEntryBitmap.stream().filter(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                return bitmap.entityType != EntityType.HERO;
            }).forEach(entry -> {
                RenderEntryBitmap bitmap = (RenderEntryBitmap) entry;
                Vector3f Max = new Vector3f(bitmap.P).add(new Vector3f(bitmap.Size, 0f));
                renderer2D.drawTextureRegion(bitmap.P.x, bitmap.P.y, Max.x, Max.y, 0, 0, 1, 1, new Color(bitmap.Color.x, bitmap.Color.y, bitmap.Color.z));
            });
            renderer2D.end();

            RenderGroup.Assets.get("fred_01.png").getTexture().unbind();
            RenderGroup.Assets.get("tree00.bmp").getTexture().unbind();
            //DISABLE TEXTURE HERE
//            renderer2D.begin();
//
//            renderEntryRectangle.forEach(entry -> {
//                RenderEntryRectangle rectangle = (RenderEntryRectangle) entry;
//                Vector3f Max = new Vector3f(rectangle.P).add(new Vector3f(rectangle.Dim, 0));
//                renderer2D.drawTextureRegion(rectangle.P.x, rectangle.P.y, Max.x, Max.y, 0, 0, 1, 1, new Color(rectangle.Color.x, rectangle.Color.y, rectangle.Color.z));
//            });
//            renderer2D.end();
            //ENABLE TEXTURE HERE
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

        renderGroup.MonitorHalfDimInMeters = new Vector2f(0.5f * PixelWidth * PixelsToMeters, 0.5f * PixelHeight * PixelsToMeters);

        renderGroup.CameraTransform.MetersToPixels = MetersToPixels;
        renderGroup.CameraTransform.FocalLength =  FocalLength;
        renderGroup.CameraTransform.DistanceAboveTarget = DistanceAboveTarget;
        renderGroup.CameraTransform.ScreenCenter = new Vector2f(0.5f * PixelWidth, 0.5f * PixelHeight);

        renderGroup.CameraTransform.Orthographic = false;
    }

    public void Orthographic(RenderGroup renderGroup, int PixelWidth, int PixelHeight, float MetersToPixels) {
        float PixelsToMeters = 1.0f / MetersToPixels;
        renderGroup.MonitorHalfDimInMeters = new Vector2f(0.5f * PixelWidth * PixelsToMeters,0.5f * PixelHeight * PixelsToMeters);

        renderGroup.CameraTransform.MetersToPixels = MetersToPixels;
        renderGroup.CameraTransform.FocalLength = 1.0f;
        renderGroup.CameraTransform.DistanceAboveTarget = 1.0f;
        renderGroup.CameraTransform.ScreenCenter = new Vector2f(0.5f * PixelWidth, 0.5f * PixelHeight);

        renderGroup.CameraTransform.Orthographic = true;
    }

    public UsedBitmapDim GetBitmapDim(RenderGroup Group, ObjectTransform ObjectTransform, LoadedBitmap Bitmap, float Height, Vector3f Offset, float CAlign) {
        UsedBitmapDim Dim = new UsedBitmapDim();

        Dim.Size = new Vector2f(Height * Bitmap.WidthOverHeight, Height);
        Dim.Align = Calc.Hadamard(new Vector3f(Bitmap.AlignPercentage.x, Bitmap.AlignPercentage.y, 0), new Vector3f(Dim.Size.x, Dim.Size.y, 0)).mul(CAlign);
        Dim.P = Offset.sub(new Vector3f(Dim.Align));
        Dim.Basis = GetRenderEntityBasisP(Group.CameraTransform, ObjectTransform, Dim.P);

        return(Dim);
    }
}
