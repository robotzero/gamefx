package com.robotzero.gamefx.renderengine;

import org.joml.Matrix4f;
import org.joml.Vector3f;

public class Player {
    private static final float PLAYER_POS_STEP = 640;
    private static final float targetSecondsPerFrame = 1.0f / 60f;
    private static final float playerHeight = 1.4f;
    private static final float playerWidth = 0.75f * playerHeight;
    private Vector3f dPlayerP = new Vector3f(200f, 300f, 1f);

    public Matrix4f getModelMatrix() {
        final Matrix4f v = new Matrix4f();
        return v.identity().translate(dPlayerP);
    }

    public void movePosition(Vector3f ddPlayerP) {
        //final var dPlayerPTemp = new Vector3f(ddPlayerP.x, ddPlayerP.y, ddPlayerP.z);
        //dPlayerPTemp.mul(-1.5f);
        //ddPlayerP.add(dPlayerPTemp);
        dPlayerP = dPlayerP.add(ddPlayerP.mul(PLAYER_POS_STEP * targetSecondsPerFrame));
    }
}
