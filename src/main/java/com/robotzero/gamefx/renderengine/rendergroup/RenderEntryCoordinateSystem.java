package com.robotzero.gamefx.renderengine.rendergroup;

import com.robotzero.gamefx.renderengine.entity.EntityType;
import org.joml.Vector2f;
import org.joml.Vector4f;

public class RenderEntryCoordinateSystem implements RenderEntry {
    public RenderGroupEntryHeader Header;
    public Vector2f Origin;
    public Vector2f XAxis;
    public Vector2f YAxis;
    public Vector4f Color;
    public EntityType EntityType;

    public Vector2f[] Points = new Vector2f[16];
}
