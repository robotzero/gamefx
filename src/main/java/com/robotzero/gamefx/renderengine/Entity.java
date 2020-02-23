package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.World;
import org.joml.Vector2f;

public class Entity {
    public EntityResidence Residence = EntityResidence.Nonexistent;
    public int LowIndex = 0;
    public LowEntity Low;
    public HighEntity High;

    public static class HighEntity {
        public Vector2f P;
        public Vector2f dP;
        public int LowEntityIndex = 0;
    }

    public static class LowEntity {
        public World.WorldPosition P = new World.WorldPosition();
        public float Width, Height;
        boolean Collides;
        public EntityType Type;
        public int HighEntityIndex;
    }

    public enum EntityResidence {
        Nonexistent, Low, High
    };
}



