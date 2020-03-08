package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.world.World;
import org.joml.Vector2f;

public class Entity {
    public int LowIndex = 0;
    public LowEntity Low;
    public HighEntity High;

    public static class HighEntity {
        public Vector2f P;
        public Vector2f dP;
        public int LowEntityIndex = 0;
        public float tbob = 0;
    }

    public static class LowEntity {
        public World.WorldPosition P = new World.WorldPosition();
        public float Width, Height;
        public boolean Collides;
        public EntityType Type;
        public int HighEntityIndex;
    }
}



