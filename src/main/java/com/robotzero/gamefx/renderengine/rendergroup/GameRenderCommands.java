package com.robotzero.gamefx.renderengine.rendergroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GameRenderCommands {
    public int Width;
    public int Height;
    public static int MaxPushBufferSize = 1000000;
    public int PushBufferSize = 0;
    public Map<RenderGroupEntryType, List<RenderEntry>> PushBuffer;

    public static void allocate(GameRenderCommands gameRenderCommands) {
        gameRenderCommands.PushBufferSize = 0;
        gameRenderCommands.PushBuffer = new HashMap<>();
        gameRenderCommands.PushBuffer.put(RenderGroupEntryType.BITMAP, new ArrayList<>());
        gameRenderCommands.PushBuffer.put(RenderGroupEntryType.RECTANGLE, new ArrayList<>());
        gameRenderCommands.PushBuffer.put(RenderGroupEntryType.CLEAR, new ArrayList<>());
        gameRenderCommands.PushBuffer.put(RenderGroupEntryType.COORDINATE, new ArrayList<>());
    }
}
