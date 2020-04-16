package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Mesh;

public interface Render {
    void render(final long window, Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1) throws Exception;
    void init() throws Exception;
    void cleanup();
}
