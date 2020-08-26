package com.robotzero.gamefx.renderengine.entity;

import com.robotzero.gamefx.world.World;
import com.robotzero.gamefx.world.WorldChunk;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Entity {
    public Vector3f P = null;
    public Vector3f Dim = new Vector3f(0.0f, 0.0f, 0.0f);
    public WorldChunk OldChunk;
    public EntityType Type;
    public float tBob;
    public Vector3f dP = new Vector3f(0.0f, 0.0f, 0.0f);
    public Map<Integer, Entity> Hash;
    public List<EntityFlag> flags = new ArrayList<>();
    public boolean Updatable = false;
    public float DistanceLimit = 0.0f;
    public EntityCollisionVolumeGroup Collision;
    public EntityId entityId = new EntityId();
    public Vector3f MovementFrom = new Vector3f();
    public Vector3f MovementTo = new Vector3f();

    public Entity(Entity sim) {
        P = new Vector3f(sim.P);
//        P = new Vector3f(0.0f, 0.0f, 0.0f);
        Dim = sim.Dim;
        Type = sim.Type;
        dP = sim.dP;
        flags = new ArrayList<>(sim.flags);
        tBob = sim.tBob;
        Collision = sim.Collision;
        Updatable = sim.Updatable;
        if(sim.Hash == null) {
            Hash = new HashMap<>();
        } else {
            Hash = new HashMap<>(sim.Hash);
        }
        OldChunk = sim.OldChunk;
        entityId = sim.entityId;
        MovementFrom = sim.MovementFrom;
        MovementTo = sim.MovementTo;
    }

    public Entity() {

    }
}



