package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.renderengine.math.Rectangle;
import com.robotzero.gamefx.world.World;

import java.util.List;

public class SimRegion {
    public World.WorldPosition Origin;
    public Rectangle Bounds;
    public int MaxEntityCount;
    public int EntityCount;
    public List<SimEntity> simEntities;
    public SimEntityHash[] Hash;
}
