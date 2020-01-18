package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.utils.Texture;
import com.robotzero.gamefx.renderengine.utils.VertexArray;

public interface Render {
    void render(final long window, Texture bgTexture, VertexArray background, float[] viewMatrix);
}
