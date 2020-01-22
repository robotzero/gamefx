package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Mesh;

public interface Render {
    void render(final long window, Mesh background2, Mesh bird);
    void init() throws Exception;
    void cleanup();
}
