package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.world.TileMap;
import org.joml.Vector2f;

public class Entity {
    public EntityResidence Residence;
    public LowEntity Low;
    public HighEntity High;
    public DormantEntity Dormant;

    public static class HighEntity {
        public boolean Exists;
        public Vector2f P;
        public Vector2f dP;
    }

    public static class LowEntity {

    }

    public static class DormantEntity {
        public TileMap.TileMapPosition P = new TileMap.TileMapPosition();
        public float Width, Height;
        boolean Collides;
    }

    public enum EntityResidence {
        Nonexistent, Dormant, Low, High;
    };
}



