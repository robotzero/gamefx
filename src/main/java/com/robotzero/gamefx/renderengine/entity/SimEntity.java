package com.robotzero.gamefx.renderengine.entity;

import org.joml.Vector2f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimEntity {
    public Vector2f P = new Vector2f(0.0f, 0.0f);
    public float Width, Height;
    public EntityType Type;
    public float tBob;
    public Vector2f dP = new Vector2f(0.0f, 0.0f);
    public int StorageIndex;
    public Map<Integer, SimEntity> Hash;
    public List<SimEntityFlag> flags = new ArrayList<>();
    public boolean Updatable = false;
    public float DistanceLimit = 0.0f;

    public SimEntity(SimEntity sim) {
        P = sim.P;
        Width = sim.Width;
        Height = sim.Height;
        Type = sim.Type;
        dP = sim.dP;
        flags = sim.flags;
        tBob = sim.tBob;
        StorageIndex = sim.StorageIndex;
        Updatable = sim.Updatable;
        if(sim.Hash == null) {
            Hash = new HashMap<>();
        } else {
            Hash = new HashMap<>(sim.Hash);
        }
    }

    public SimEntity() {

    }
}



