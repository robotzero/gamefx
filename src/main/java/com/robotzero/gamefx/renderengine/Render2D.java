package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.entity.EntityService;
import com.robotzero.gamefx.renderengine.entity.EntityType;
import com.robotzero.gamefx.renderengine.model.Mesh;
import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.renderengine.utils.FileUtils;
import com.robotzero.gamefx.renderengine.utils.ShaderProgram;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;

import java.nio.ByteBuffer;
import java.nio.FloatBuffer;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
import static org.lwjgl.opengl.GL11.GL_NO_ERROR;
import static org.lwjgl.opengl.GL11.GL_TEXTURE_2D;
import static org.lwjgl.opengl.GL11.GL_TRIANGLES;
import static org.lwjgl.opengl.GL11.GL_UNSIGNED_INT;
import static org.lwjgl.opengl.GL11.glBindTexture;
import static org.lwjgl.opengl.GL11.glClear;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glDrawElements;
import static org.lwjgl.opengl.GL11.glGetError;
import static org.lwjgl.opengl.GL11.glViewport;

public class Render2D implements Render {
    private ShaderProgram sceneShaderProgram;
    private ShaderProgram birdShaderProgram;
    private ShaderProgram quadShaderProgram;
    private ShaderProgram familiarShaderProgram;
    private final Camera camera;
    private final EntityService entityService;
    private BatchData batchData;

    public Render2D(Camera camera, EntityService entityService) {
        this.camera = camera;
        this.entityService = entityService;
    }

    public void init() throws Exception {
//        setupBirdShader();
//        setUpQuadShader();
//        setUpFamiliarShader();
        init2();
    }

    public void clear() {
        glClearColor(0.0f, 0.5f, 0.5f, 1.0f);
        glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
    }

    public void render(final long window, Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1) throws Exception {
        clear();
        glViewport(0, 0, DisplayManager.WIDTH, DisplayManager.HEIGHT);
//        renderScene(bird, quad, familiar, rectangle1);
        beginScene();
        DrawQuad(new Vector3f(20.0f, 3.0f, 1.0f), new Vector2f(10.0f, 10.0f), bird.getTexture(), 10.0f, null);
        endScene();

        int error = glGetError();
        if (error != GL_NO_ERROR) {
            System.out.println(error);
        }
    }

    private void init2() throws Exception {
        batchData = new BatchData();
        BufferLayout bufferLayout = new BufferLayout(List.of(
                new BufferElement(ShaderDataType.Float3, "a_Position"),
                new BufferElement(ShaderDataType.Float4, "a_Color"),
                new BufferElement(ShaderDataType.Float2, "a_TexCoord"),
                new BufferElement(ShaderDataType.Float, "a_TexIndex"),
                new BufferElement(ShaderDataType.Float, "a_TilingFactor")
        ));
        batchData.quadVertexArray = new VertexArray();
        batchData.quadVertexBuffer = new VertexBuffer(BatchData.MaxVertices * 8);
        batchData.quadVertexBuffer.setBufferLayout(bufferLayout);
        batchData.quadVertexArray.addVertexBuffer(batchData.quadVertexBuffer);
        batchData.QuadVertexBufferBase = new QuadVertex[BatchData.MaxVertices];
        int[] quadIndices = new int[BatchData.MaxIndices];
        int offset = 0;

        for (int i = 0; i < BatchData.MaxIndices; i += 6) {
                quadIndices[i + 0] = offset + 0;
                quadIndices[i + 1] = offset + 1;
                quadIndices[i + 2] = offset + 2;

                quadIndices[i + 3] = offset + 2;
                quadIndices[i + 4] = offset + 3;
                quadIndices[i + 5] = offset + 0;

                offset += 4;
        }

//        IndexBuffer quadIB = new IndexBuffer(quadIndices, BatchData.MaxIndices);
        IndexBuffer quadIB = new IndexBuffer(new int[] {
                0, 1, 2,
                2, 3, 0
        }, 6);
        batchData.quadVertexArray.setIndexBuffer(quadIB);
//        batchData.WhiteTexture = new Texture(1, 1, new int[]{0xffffffff});
        int[] samplers = new int[BatchData.MaxTextureSlots];

        for (int i = 0; i < BatchData.MaxTextureSlots; i++) {
            samplers[i] = i;
        }

        batchData.textureShader = new ShaderProgram();
        batchData.textureShader.createFragmentShader(FileUtils.loadAsString("shaders/Texture.frag"));
        batchData.textureShader.createVertexShader(FileUtils.loadAsString("shaders/Texture.vert"));
        batchData.textureShader.link();
//        batchData.textureShader.createUniform("u_Textures");
//        batchData.textureShader.setIntArray("u_Textures", samplers, BatchData.MaxTextureSlots);
        batchData.TextureSlots[0] = batchData.WhiteTexture;
        batchData.QuadVertexPositions[0] = new Vector3f(-0.5f, -0.5f, 1.0f);
        batchData.QuadVertexPositions[1] = new Vector3f(0.5f, -0.5f, 1.0f);
        batchData.QuadVertexPositions[2] = new Vector3f(0.5f, 0.5f, 1.0f);
        batchData.QuadVertexPositions[3] = new Vector3f(-0.5f, 0.5f, 1.0f);
    }

    public void beginScene() throws Exception {

        batchData.textureShader.createUniform("u_ViewProjection");
        batchData.textureShader.createUniform("vw_matrix");
        batchData.textureShader.createUniform("ml_matrix");
        batchData.textureShader.bind();
        batchData.textureShader.setUniform("u_ViewProjection", camera.getProjectionMatrix());
        batchData.textureShader.setUniform("vw_matrix", camera.getIdentity());
        batchData.textureShader.setUniform("ml_matrix", new Matrix4f().identity().translate(new Vector3f(300, 300, 0)));

        batchData.QuadIndexCount = 0;
        batchData.QuadVertexBufferPtr = batchData.QuadVertexBufferBase;

        batchData.TextrueSlotIndex = 1;
    }

    public void endScene() {
        int dataSize = batchData.QuadVertexBufferPtr.length - (batchData.QuadVertexBufferBase.length);

        // Create a FloatBufer of the appropriate size for one vertex
        ByteBuffer vertexByteBuffer = BufferUtils.createByteBuffer(QuadVertex.stride);

        // Put each 'Vertex' in one FloatBuffer
        ByteBuffer verticesByteBuffer = BufferUtils.createByteBuffer(batchData.QuadVertexBufferBase.length * QuadVertex.stride);
        FloatBuffer verticesFloatBuffer = verticesByteBuffer.asFloatBuffer();
        for (int i = 0; i < batchData.QuadVertexBufferBase.length; i++) {
            // Add position, color and texture floats to the buffer
            if (batchData.QuadVertexBufferBase[i] != null) {
                verticesFloatBuffer.put(batchData.QuadVertexBufferBase[i].getElements());
                dataSize = batchData.QuadVertexBufferBase[i].getElements().length;
            }
        }

        verticesFloatBuffer.flip();
        batchData.quadVertexBuffer.setData(verticesFloatBuffer, dataSize);

        flush();
    }

    public void flush() {
        // Bind textures
        for (int i = 0; i < batchData.TextrueSlotIndex; i++) {
//            batchData.TextureSlots[i].bind(i);
        }

        drawIndexed(batchData.quadVertexArray, batchData.QuadIndexCount);
    }

    private void drawIndexed(VertexArray vertexArray, int indexCount) {
        int count = indexCount != 0 ? vertexArray.getIndexBuffer().getCount() : indexCount;
        glDrawElements(GL_TRIANGLES, count, GL_UNSIGNED_INT, 0);
//        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public void DrawQuad(Vector3f position, Vector2f size, Texture texture, float tilingFactor, Vector4f tintColor) {

        Vector4f color = new Vector4f(1.0f, 1.0f, 1.0f, 1.0f);

        float textureIndex = 0.0f;
        for (int i = 1; i < batchData.TextrueSlotIndex; i++) {
            if (batchData.TextureSlots[i] == texture) {
                textureIndex = (float)i;
                break;
            }
        }

        if (textureIndex == 0.0f) {
            textureIndex = (float)batchData.TextrueSlotIndex;
            batchData.TextureSlots[batchData.TextrueSlotIndex] = texture;
            batchData.TextrueSlotIndex++;
        }

        batchData.QuadVertexBufferPtr[0] = new QuadVertex();
        batchData.QuadVertexBufferPtr[0].Position = batchData.QuadVertexPositions[0];
        batchData.QuadVertexBufferPtr[0].Color = color;
        batchData.QuadVertexBufferPtr[0].TexCoord = new Vector2f(0.0f, 0.0f);
        batchData.QuadVertexBufferPtr[0].TexIndex = textureIndex;
        batchData.QuadVertexBufferPtr[0].TilingFactor = tilingFactor;
//
//        s_Data.QuadVertexBufferPtr->Position = transform * s_Data.QuadVertexPositions[1];
//        s_Data.QuadVertexBufferPtr->Color = color;
//        s_Data.QuadVertexBufferPtr->TexCoord = { 1.0f, 0.0f };
//        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
//        s_Data.QuadVertexBufferPtr->TilingFactor = tilingFactor;
//        s_Data.QuadVertexBufferPtr++;
//
//        s_Data.QuadVertexBufferPtr->Position = transform * s_Data.QuadVertexPositions[2];
//        s_Data.QuadVertexBufferPtr->Color = color;
//        s_Data.QuadVertexBufferPtr->TexCoord = { 1.0f, 1.0f };
//        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
//        s_Data.QuadVertexBufferPtr->TilingFactor = tilingFactor;
//        s_Data.QuadVertexBufferPtr++;
//
//        s_Data.QuadVertexBufferPtr->Position = transform * s_Data.QuadVertexPositions[3];
//        s_Data.QuadVertexBufferPtr->Color = color;
//        s_Data.QuadVertexBufferPtr->TexCoord = { 0.0f, 1.0f };
//        s_Data.QuadVertexBufferPtr->TexIndex = textureIndex;
//        s_Data.QuadVertexBufferPtr->TilingFactor = tilingFactor;
//        s_Data.QuadVertexBufferPtr++;

        batchData.QuadIndexCount += 6;
    }


    private void renderScene(Mesh bird, Mesh quad, Mesh familiar, Mesh rectangle1) {
        final var translations = entityService.getModelMatrix();
        if (!translations.isEmpty()) {
            Matrix4f viewMatrix = camera.getIdentity();
            Matrix4f projectionMatrix = camera.getProjectionMatrix();
            //@@TODO one player so far so we just get it.
//            Matrix4f playerModelMatrix = translations.get(EntityType.HERO).get(0);
//        sceneShaderProgram.bind();
//        sceneShaderProgram.setUniform("vw_matrix", viewMatrix);
//        sceneShaderProgram.setUniform("pr_matrix", projectionMatrix);
//        sceneShaderProgram.setUniform("tex", 1);sssssssssssss
//
//        background.render();
//        background.endRender();
//        sceneShaderProgram.unbind();
//
//            birdShaderProgram.bind();
//            birdShaderProgram.setUniform("vw_matrix", viewMatrix);
//            birdShaderProgram.setUniform("pr_matrix", projectionMatrix);
//            birdShaderProgram.setUniform("ml_matrix", playerModelMatrix);
//            birdShaderProgram.setUniform("tex", 1);
//            bird.render();
//            bird.endRender();
//            birdShaderProgram.unbind();
//
//            quadShaderProgram.bind();
//            quadShaderProgram.setUniform("pr_matrix", projectionMatrix);
//            quadShaderProgram.setUniform("t_color", quad.getMaterial().getColor());
//            quadShaderProgram.setUniform("vw_matrix", viewMatrix);
//            translations.get(EntityType.WALL).forEach((key) -> {
//                quadShaderProgram.setUniform("ml_matrix", key);
//                quadShaderProgram.setUniform("t_color", new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
//                quad.render();
//                quad.endRender();
//            });
//            translations.get(EntityType.SPACE).forEach((key) -> {
//                  quadShaderProgram.setUniform("ml_matrix", key);
//                  quadShaderProgram.setUniform("t_color", new Vector4f(1.0f, 0.5f, 0.0f, 1.0f));
//                  quad.render();
//                  quad.endRender();
//            });

//            translations.get(EntityType.DEBUG).forEach((key) -> {
//                quadShaderProgram.setUniform("ml_matrix", key);
//                quadShaderProgram.setUniform("t_color", new Vector4f(0.5f, 0.5f, 0.5f, 1.0f));
//                rectangle1.render();
//                rectangle1.endRender();
//            });
//            quadShaderProgram.unbind();

//            Matrix4f familiarMatrix = entityService.getModelMatrix().get(EntityType.FAMILIAR).get(0).getValue();
//            familiarShaderProgram.bind();
//            familiarShaderProgram.setUniform("pr_matrix", projectionMatrix);
//            familiarShaderProgram.setUniform("ml_matrix", familiarMatrix);
//            familiarShaderProgram.setUniform("t_color", new Vector4f(1.0f, 1.0f, 0.0f, 1.0f));
//            familiar.render();
//            familiar.endRender();
//            familiarShaderProgram.unbind();
        }
    }

    private void setupSceneShader() throws Exception {
        sceneShaderProgram = new ShaderProgram();
        sceneShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/background.vert"));
        sceneShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/background.frag"));
        sceneShaderProgram.link();

        // Create uniforms for view and projection matrices
        sceneShaderProgram.createUniform("vw_matrix");
        sceneShaderProgram.createUniform("pr_matrix");
        sceneShaderProgram.createUniform("tex");
    }

    private void setupBirdShader() throws Exception {
        birdShaderProgram = new ShaderProgram();
        birdShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/bird.vert"));
        birdShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/bird.frag"));
        birdShaderProgram.link();

        // Create uniforms for view and projection matrices
        birdShaderProgram.createUniform("vw_matrix");
        birdShaderProgram.createUniform("pr_matrix");
        birdShaderProgram.createUniform("tex");
        birdShaderProgram.createUniform("ml_matrix");
    }

    private void setUpQuadShader() throws Exception {
        quadShaderProgram = new ShaderProgram();
        quadShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/quad2D.vert"));
        quadShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/quad2D.frag"));
        quadShaderProgram.link();

        // Create uniforms for view and projection matrices
        quadShaderProgram.createUniform("vw_matrix");
        quadShaderProgram.createUniform("pr_matrix");
        quadShaderProgram.createUniform("t_color");
        quadShaderProgram.createUniform("ml_matrix");
    }

    private void setUpFamiliarShader() throws Exception {
        familiarShaderProgram = new ShaderProgram();
        familiarShaderProgram.createVertexShader(FileUtils.loadAsString("shaders/familiar.vert"));
        familiarShaderProgram.createFragmentShader(FileUtils.loadAsString("shaders/familiar.frag"));
        familiarShaderProgram.link();

        // Create uniforms for view and projection matrices
        familiarShaderProgram.createUniform("vw_matrix");
        familiarShaderProgram.createUniform("pr_matrix");
        familiarShaderProgram.createUniform("t_color");
        familiarShaderProgram.createUniform("ml_matrix");
    }

    public void cleanup() {
//        sceneShaderProgram.cleanup();
        //familiarShaderProgram.cleanup();
        //birdShaderProgram.cleanup();
        //quadShaderProgram.cleanup();
    }
}
