
package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.model.Color;
import com.robotzero.gamefx.renderengine.model.Shader;
import com.robotzero.gamefx.renderengine.model.ShaderProgram;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.renderengine.model.VertexArrayObject;
import com.robotzero.gamefx.renderengine.model.VertexBufferObject;
import org.joml.Matrix4f;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawArrays;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL20.GL_FRAGMENT_SHADER;
import static org.lwjgl.opengl.GL20.GL_VERTEX_SHADER;


public class Renderer2D {
    private VertexArrayObject vao;
    private VertexBufferObject vbo;
    private ShaderProgram program;

    private FloatBuffer vertices;
    private int numVertices;
    private boolean drawing;
    private final EntityService entityService;
    private final Camera camera;
    public static Texture texture;

    public Renderer2D(EntityService entityService, Camera camera) {
        this.entityService = entityService;
        this.camera = camera;
    }

    /**
     * Initializes the renderer.
     */
    public void init() {
        /* Setup shader programs */
        texture = Texture.loadTexture(this.getClass().getClassLoader().getResource("bird.png").getFile());
        setupShaderProgram();
    }

    /**
     * Clears the drawing area.
     */
    public void clear() {
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    /**
     * Begin rendering.
     */
    public void begin() {
        if (drawing) {
            throw new IllegalStateException("Renderer is already drawing!");
        }
        drawing = true;
        numVertices = 0;
    }

    public void render() {
        final var entityStates = entityService.getModelMatrix();
//        final var debugStates = entityService.getDebug();
        if (!entityStates.isEmpty()) {
            clear();
            begin();
            final var hero = entityStates.get(EntityType.HERO).get(0);
//            drawTextureRegion(hero.x, hero.y, hero.x + World.MetersToPixels * EntityService.PlayerWidth, hero.y + World.MetersToPixels * EntityService.PlayerHeight, 0, 0, 1, 1, 1.0f, Color.WHITE);
            drawTextureRegion(hero.getMin().x, hero.getMin().y, hero.getMax().x, hero.getMax().y, 0, 0, 1, 1, 1.0f, Color.WHITE);
            entityStates.get(EntityType.WALL).forEach(wall -> {
                drawTextureRegion(wall.getMin().x, wall.getMin().y, wall.getMax().x, wall.getMax().y, 0, 0, 1, 1, 0.0f, new Color(1.0f, 0.5f, 0.0f, 1.0f));
            });
//            entityStates.get(EntityType.SPACE).forEach(a -> {
//                drawTextureRegion(a.getMin().x, a.getMin().y, a.getMax().x, a.getMax().y, 0, 0, 1, 1, 0.0f, new Color(1.0f, 0.5f, 1.0f, 1.0f));
//            });

            entityStates.get(EntityType.DEBUG).forEach(a -> {
                drawTextureRegion(a.getMin().x, a.getMin().y, a.getMax().x, a.getMax().y, 0, 0, 1, 1,0.0f, new Color(a.getColor().x, a.getColor().y, a.getColor().z));
            });
//            debugStates.forEach((a, b) -> {
//                Vector3f Min = b.get(0);
//                Vector3f Max = b.get(1);
//
//                drawTextureRegion(Min.x - 2.0f, Min.y - 2.0f, Max.x + 2.0f, Min.y + 2.0f, 0, 0, 1, 1, 0.0f, Color.BLACK);
//                drawTextureRegion(Min.x - 2.0f, Max.y - 2.0f, Max.x + 2.0f, Max.y + 2.0f, 0, 0, 1, 1, 0.0f, Color.BLACK);
//                drawTextureRegion(Min.x - 2.0f, Min.y - 2.0f, Min.x + 2.0f, Max.y + 2.0f, 0, 0, 1, 1, 0.0f, Color.BLACK);
//                drawTextureRegion(Max.x - 2.0f, Min.y - 2.0f, Max.x + 2.0f, Max.y + 2.0f, 0, 0, 1, 1, 0.0f, Color.BLACK);
//
//            });
//            entityStates.get(EntityType.DEBUG).forEach(a -> {
//                drawTextureRegion(a.x, a.y, a.x + World.TileSideInPixels, a.y + World.TileSideInPixels, 0, 0, 1, 1, 0.0f, new Color(1.0f, 0.5f, 0.0f, 1.0f));
//            });
            end();
        }
    }

    /**
     * End rendering.
     */
    public void end() {
        if (!drawing) {
            throw new IllegalStateException("Renderer isn't drawing!");
        }
        drawing = false;
        flush();
    }

    /**
     * Flushes the data to the GPU to let it get rendered.
     */
    public void flush() {
        if (numVertices > 0) {
            vertices.flip();

            if (vao != null) {
                vao.bind();
            } else {
                vbo.bind(GL_ARRAY_BUFFER);
                specifyVertexAttributes();
            }
            program.use();

            /* Upload the new vertex data */
            vbo.bind(GL_ARRAY_BUFFER);
            vbo.uploadSubData(GL_ARRAY_BUFFER, 0, vertices);

            /* Draw batch */
            glDrawArrays(GL_TRIANGLES, 0, numVertices);

            /* Clear vertex data for next batch */
            vertices.clear();
            numVertices = 0;
        }
    }

    /**
     * Draws the currently bound texture on specified coordinates.
     *
     * @param texture Used for getting width and height of the texture
     * @param x       X position of the texture
     * @param y       Y position of the texture
     */
    public void drawTexture(Texture texture, float x, float y) {
        drawTexture(texture, x, y, Color.WHITE);
    }

    /**
     * Draws the currently bound texture on specified coordinates and with
     * specified color.
     *
     * @param texture Used for getting width and height of the texture
     * @param x       X position of the texture
     * @param y       Y position of the texture
     * @param c       The color to use
     */
    public void drawTexture(Texture texture, float x, float y, Color c) {
        /* Vertex positions */
        float x1 = x;
        float y1 = y;
        float x2 = x1 + texture.getWidth();
        float y2 = y1 + texture.getHeight();

        /* Texture coordinates */
        float s1 = 0f;
        float t1 = 0f;
        float s2 = 1f;
        float t2 = 1f;

        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, 1.0f, c);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param texture   Used for getting width and height of the texture
     * @param x         X position of the texture
     * @param y         Y position of the texture
     * @param regX      X position of the texture region
     * @param regY      Y position of the texture region
     * @param regWidth  Width of the texture region
     * @param regHeight Height of the texture region
     */
    public void drawTextureRegion(Texture texture, float x, float y, float regX, float regY, float regWidth, float regHeight, float isTexture) {
        drawTextureRegion(texture, x, y, regX, regY, regWidth, regHeight, isTexture, Color.WHITE);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param texture   Used for getting width and height of the texture
     * @param x         X position of the texture
     * @param y         Y position of the texture
     * @param regX      X position of the texture region
     * @param regY      Y position of the texture region
     * @param regWidth  Width of the texture region
     * @param regHeight Height of the texture region
     * @param c         The color to use
     */
    public void drawTextureRegion(Texture texture, float x, float y, float regX, float regY, float regWidth, float regHeight, float isTexture, Color c) {
        /* Vertex positions */
        float x1 = x;
        float y1 = y;
        float x2 = x + regWidth;
        float y2 = y + regHeight;

        /* Texture coordinates */
        float s1 = regX / texture.getWidth();
        float t1 = regY / texture.getHeight();
        float s2 = (regX + regWidth) / texture.getWidth();
        float t2 = (regY + regHeight) / texture.getHeight();

        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, isTexture, c);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param x1 Bottom left x position
     * @param y1 Bottom left y position
     * @param x2 Top right x position
     * @param y2 Top right y position
     * @param s1 Bottom left s coordinate
     * @param t1 Bottom left t coordinate
     * @param s2 Top right s coordinate
     * @param t2 Top right t coordinate
     */
    public void drawTextureRegion(float x1, float y1, float x2, float y2, float s1, float t1, float s2, float t2, float isTexture) {
        drawTextureRegion(x1, y1, x2, y2, s1, t1, s2, t2, isTexture, Color.WHITE);
    }

    /**
     * Draws a texture region with the currently bound texture on specified
     * coordinates.
     *
     * @param x1 Bottom left x position
     * @param y1 Bottom left y position
     * @param x2 Top right x position
     * @param y2 Top right y position
     * @param s1 Bottom left s coordinate
     * @param t1 Bottom left t coordinate
     * @param s2 Top right s coordinate
     * @param t2 Top right t coordinate
     * @param c  The color to use
     */
    public void drawTextureRegion(float x1, float y1, float x2, float y2, float s1, float t1, float s2, float t2, float isTexture, Color c) {
        if (vertices.remaining() < 8 * 6) {
            /* We need more space in the buffer, so flush it */
            flush();
        }

        float r = c.getRed();
        float g = c.getGreen();
        float b = c.getBlue();
        float a = c.getAlpha();

        vertices.put(x1).put(y1).put(r).put(g).put(b).put(a).put(s1).put(t1).put(isTexture);
        vertices.put(x1).put(y2).put(r).put(g).put(b).put(a).put(s1).put(t2).put(isTexture);
        vertices.put(x2).put(y2).put(r).put(g).put(b).put(a).put(s2).put(t2).put(isTexture);

        vertices.put(x1).put(y1).put(r).put(g).put(b).put(a).put(s1).put(t1).put(isTexture);
        vertices.put(x2).put(y2).put(r).put(g).put(b).put(a).put(s2).put(t2).put(isTexture);
        vertices.put(x2).put(y1).put(r).put(g).put(b).put(a).put(s2).put(t1).put(isTexture);

        numVertices += 6;
    }

    /**
     * Dispose renderer and clean up its used data.
     */
    public void dispose() {
        MemoryUtil.memFree(vertices);

        if (vao != null) {
            vao.delete();
        }
        vbo.delete();
        program.delete();
        texture.delete();
    }

    /**
     * Setups the default shader program.
     */
    private void setupShaderProgram() {
        vao = new VertexArrayObject();
        vao.bind();

        /* Generate Vertex Buffer Object */
        vbo = new VertexBufferObject();
        vbo.bind(GL_ARRAY_BUFFER);

        /* Create FloatBuffer */
        vertices = MemoryUtil.memAllocFloat(4096);

        /* Upload null data to allocate storage for the VBO */
        long size = vertices.capacity() * Float.BYTES;
        vbo.uploadData(GL_ARRAY_BUFFER, size, GL_DYNAMIC_DRAW);

        /* Initialize variables */
        numVertices = 0;
        drawing = false;

        /* Load shaders */
        Shader vertexShader, fragmentShader;
        vertexShader = Shader.loadShader2(GL_VERTEX_SHADER, "shaders/default.vert");
        fragmentShader = Shader.loadShader2(GL_FRAGMENT_SHADER, "shaders/default.frag");

        /* Create shader program */
        program = new ShaderProgram();
        program.attachShader(vertexShader);
        program.attachShader(fragmentShader);
        program.bindFragmentDataLocation(0, "fragColor");
        program.link();
        program.use();

        /* Delete linked shaders */
        vertexShader.delete();
        fragmentShader.delete();

        /* Get width and height of framebuffer */
        long window = GLFW.glfwGetCurrentContext();
        int width, height;
        try (MemoryStack stack = MemoryStack.stackPush()) {
            IntBuffer widthBuffer = stack.mallocInt(1);
            IntBuffer heightBuffer = stack.mallocInt(1);
            GLFW.glfwGetFramebufferSize(window, widthBuffer, heightBuffer);
            width = widthBuffer.get();
            height = heightBuffer.get();
        }

        /* Specify Vertex Pointers */
        specifyVertexAttributes();

        /* Set texture uniform */
        int uniTex = program.getUniformLocation("texImage");
        program.setUniform(uniTex, 0);

        /* Set model matrix to identity matrix */
        Matrix4f model = new Matrix4f();
        int uniModel = program.getUniformLocation("model");
        program.setUniform(uniModel, model);

        /* Set view matrix to identity matrix */
        Matrix4f view = new Matrix4f();
        int uniView = program.getUniformLocation("view");
        program.setUniform(uniView, view);

        /* Set projection matrix to an orthographic projection */
        int uniProjection = program.getUniformLocation("projection");
        program.setUniform(uniProjection, camera.getProjectionMatrix());
    }

    /**
     * Specifies the vertex pointers.
     */
    private void specifyVertexAttributes() {
        /* Specify Vertex Pointer */
        int posAttrib = program.getAttributeLocation("position");
        program.enableVertexAttribute(posAttrib);
        program.pointVertexAttribute(posAttrib, 2, 9 * Float.BYTES, 0);

        /* Specify Color Pointer */
        int colAttrib = program.getAttributeLocation("color");
        program.enableVertexAttribute(colAttrib);
        program.pointVertexAttribute(colAttrib, 4, 9 * Float.BYTES, 2 * Float.BYTES);

        /* Specify Texture Pointer */
        int texAttrib = program.getAttributeLocation("texcoord");
        program.enableVertexAttribute(texAttrib);
        program.pointVertexAttribute(texAttrib, 2, 9 * Float.BYTES, 6 * Float.BYTES);

        int boolAttib = program.getAttributeLocation("renderTexture");
        program.enableVertexAttribute(boolAttib);
        program.pointVertexAttribute(boolAttib, 1, 9 * Float.BYTES, 8 * Float.BYTES);
    }
}
