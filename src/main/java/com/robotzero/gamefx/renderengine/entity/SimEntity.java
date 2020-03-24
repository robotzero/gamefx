package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.world.WorldChunk;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SimEntity {
    public Vector3f P = null;
    public Vector3f Dim = new Vector3f(0.0f, 0.0f, 0.0f);
    public WorldChunk OldChunk;
    public EntityType Type;
    public float tBob;
    public Vector3f dP = new Vector3f(0.0f, 0.0f, 0.0f);
    public int StorageIndex;
    public Map<Integer, SimEntity> Hash;
    public List<SimEntityFlag> flags = new ArrayList<>();
    public boolean Updatable = false;
    public float DistanceLimit = 0.0f;

    public SimEntity(SimEntity sim) {
        P = sim.P;
        Dim = sim.Dim;
        Type = sim.Type;
        dP = sim.dP;
        flags = new ArrayList<>(sim.flags);
        tBob = sim.tBob;
        StorageIndex = sim.StorageIndex;
        Updatable = sim.Updatable;
        if(sim.Hash == null) {
            Hash = new HashMap<>();
        } else {
            Hash = new HashMap<>(sim.Hash);
        }
        OldChunk = sim.OldChunk;
    }

    public SimEntity() {

    }
}



