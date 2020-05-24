package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.World;

public class SimRegion {
    public World.WorldPosition Origin;
    public Rectangle Bounds;
    public Rectangle UpdatableBounds;
    public int MaxEntityCount = 4096;
    public int EntityCount = 0;
    public Entity[] simEntities;
    public EntityHash[] Hash = new EntityHash[4096];
    public float MaxEntityRadius;
    public float MaxEntityVelocity;
}
