package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.renderengine.entity.EntityType;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class RenderEntryRectangle implements RenderEntry {
    public RenderGroupEntryHeader Header;
    public RenderEntityBasis EntityBasis;
    public Vector4f Color;
    public Vector2f Dim;
    public EntityType entityType;
}
