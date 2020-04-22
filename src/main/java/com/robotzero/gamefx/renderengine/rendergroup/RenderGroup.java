package com.robotzero.gamefx.renderengine.rendergroup;

import org.joml.Vector2f;

import java.util.List;
import java.util.Map;

public class RenderGroup {
    public RenderBasis DefaultBasis;
    public float MetersToPixels;
    public int PieceCount;
    public int MaxPushBufferSize;
    public int PushBufferSize;
    public Vector2f MonitorHalfDimInMeters;
    RenderGroupCamera GameCamera;
    RenderGroupCamera RenderCamera;
    public Map<RenderGroupEntryType, List<RenderEntry>> PushBufferBase;

    public void clear() {
        PieceCount = 0;
        PushBufferSize = 0;
        PushBufferBase.get(RenderGroupEntryType.BITMAP).clear();
        PushBufferBase.get(RenderGroupEntryType.RECTANGLE).clear();
        PushBufferBase.get(RenderGroupEntryType.CLEAR).clear();
        PushBufferBase.get(RenderGroupEntryType.COORDINATE).clear();
    }
}
