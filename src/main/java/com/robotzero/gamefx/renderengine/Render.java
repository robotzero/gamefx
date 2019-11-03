package main.java.com.robotzero.gamefx.renderengine;

import main.java.com.robotzero.gamefx.renderengine.utils.Texture;
import main.java.com.robotzero.gamefx.renderengine.utils.VertexArray;

public interface Render {
    void render(final long window, Texture bgTexture, VertexArray background);
}
