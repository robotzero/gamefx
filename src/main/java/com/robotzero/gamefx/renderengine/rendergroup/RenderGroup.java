package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.renderengine.assets.Asset;
import com.robotzero.gamefx.renderengine.entity.CameraTransform;
import org.joml.Vector2f;

import java.util.Map;

public class RenderGroup {
    public CameraTransform CameraTransform = new CameraTransform();
    public Vector2f MonitorHalfDimInMeters;
    public Map<String, Asset> Assets;
    public boolean RendersInBackground = false;
    public GameRenderCommands gameRenderCommands;
    public int PieceCount;

    public void clear() {
        PieceCount = 0;
        gameRenderCommands.PushBufferSize = 0;
        gameRenderCommands.PushBuffer.get(RenderGroupEntryType.BITMAP).clear();
        gameRenderCommands.PushBuffer.get(RenderGroupEntryType.RECTANGLE).clear();
        gameRenderCommands.PushBuffer.get(RenderGroupEntryType.CLEAR).clear();
        gameRenderCommands.PushBuffer.get(RenderGroupEntryType.COORDINATE).clear();
    }
}
