package com.robotzero.gamefx.renderengine.rendergroup;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class RenderTransform {
    public float FocalLength;
    public float DistanceAboveTarget;
    public float MetersToPixels;
    public Vector2f ScreenCenter;
    public Vector3f OffsetP;
    public float Scale;

    public RenderTransform() {}
    public RenderTransform(RenderTransform renderGroupCamera) {
        this.DistanceAboveTarget = renderGroupCamera.DistanceAboveTarget;
        this.FocalLength = renderGroupCamera.FocalLength;
        this.MetersToPixels = renderGroupCamera.MetersToPixels;
        this.ScreenCenter = renderGroupCamera.ScreenCenter;
        this.OffsetP = renderGroupCamera.OffsetP;
        this.Scale = renderGroupCamera.Scale;
    }
}
