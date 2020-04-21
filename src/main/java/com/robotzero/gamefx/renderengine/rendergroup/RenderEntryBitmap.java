package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.model.Texture;
import org.joml.Vector4f;

public class RenderEntryBitmap implements RenderEntry {
    public Texture Bitmap;
    public RenderEntityBasis EntityBasis;
    public Vector4f Color;
    public RenderGroupEntryHeader Header;
    public EntityType entityType;
}
