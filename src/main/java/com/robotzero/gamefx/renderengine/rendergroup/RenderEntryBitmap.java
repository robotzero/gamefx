package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.renderengine.entity.EntityType;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

public class RenderEntryBitmap implements RenderEntry {
    public LoadedBitmap Bitmap;
    public Vector4f Color;
    public RenderGroupEntryHeader Header;
    public EntityType entityType;
    public Vector2f Size;
    public Vector3f P;
}
