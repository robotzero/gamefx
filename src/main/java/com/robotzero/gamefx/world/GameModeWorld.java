package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.entity.EntityCollisionVolumeGroup;

public class GameModeWorld {
    public World.WorldPosition CameraP;
    public World.WorldPosition LastCameraP;

    public EntityCollisionVolumeGroup WallCollision;
    public float time;
    public float tSine;

}
