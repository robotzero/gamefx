package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.entity.Entity;

public class WorldEntityBlock {
    public int EntityCount = 0;
    public int[] LowEntityIndex = new int[16];
    public Integer next = null;
    public Entity[] entityData = new Entity[1 << 16];
}
