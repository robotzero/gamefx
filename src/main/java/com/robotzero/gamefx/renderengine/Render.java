package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Mesh;

public interface Render {
    void render(final long window, Mesh background2, Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1);
    void init() throws Exception;
    void cleanup();
}
