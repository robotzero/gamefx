package com.robotzero.gamefx.renderengine.rendergroup;

public class RenderGroupCamera {
    public float FocalLength;
    public float DistanceAboveTarget;

    public RenderGroupCamera() {}
    public RenderGroupCamera(RenderGroupCamera renderGroupCamera) {
        this.DistanceAboveTarget = renderGroupCamera.DistanceAboveTarget;
        this.FocalLength = renderGroupCamera.FocalLength;
    }
}
