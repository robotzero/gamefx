package com.robotzero.gamefx.renderengine.entity;

import org.joml.Vector2f;

import java.util.HashMap;
import java.util.Map;

public class SimEntity {
    public Vector2f P = new Vector2f(0.0f, 0.0f);
    public float Width, Height;
    public boolean Collides;
    public EntityType Type;
    public float tBob;
    public Vector2f dP = new Vector2f(0.0f, 0.0f);
    public int StorageIndex;
    public Map<Integer, SimEntity> Hash;

    public SimEntity(SimEntity sim) {
        P = sim.P;
        Width = sim.Width;
        Height = sim.Height;
        Type = sim.Type;
        dP = sim.dP;
        StorageIndex = sim.StorageIndex;
        Hash = new HashMap<>(sim.Hash);
    }

    public SimEntity() {

    }
}



