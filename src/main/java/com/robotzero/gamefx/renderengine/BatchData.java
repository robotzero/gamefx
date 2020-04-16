package com.robotzero.gamefx.renderengine;

import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.renderengine.utils.ShaderProgram;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

import static org.lwjgl.opengl.GL11.GL_FLOAT;
import static org.lwjgl.opengl.GL15.GL_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_DYNAMIC_DRAW;
import static org.lwjgl.opengl.GL15.GL_ELEMENT_ARRAY_BUFFER;
import static org.lwjgl.opengl.GL15.GL_STATIC_DRAW;
import static org.lwjgl.opengl.GL15.glBindBuffer;
import static org.lwjgl.opengl.GL15.glBufferData;
import static org.lwjgl.opengl.GL15.glBufferSubData;
import static org.lwjgl.opengl.GL15.glDeleteBuffers;
import static org.lwjgl.opengl.GL20.glEnableVertexAttribArray;
import static org.lwjgl.opengl.GL20.glVertexAttribPointer;
import static org.lwjgl.opengl.GL30.glBindVertexArray;
import static org.lwjgl.opengl.GL30.glDeleteVertexArrays;
import static org.lwjgl.opengl.GL45.glCreateBuffers;
import static org.lwjgl.opengl.GL45.glCreateVertexArrays;

public class BatchData {
    public final static int MaxQuads = 10000;
    public final static int MaxVertices = MaxQuads * 4;
    public final static int MaxIndices = MaxQuads * 6;
    public final static int MaxTextureSlots = 32; // Interrogate opengl capabilites for this.
    public VertexBuffer quadVertexBuffer;
    public VertexArray quadVertexArray;
    public ShaderProgram textureShader;
    public Texture WhiteTexture;
    public int QuadIndexCount = 0;
    public QuadVertex[] QuadVertexBufferBase = null;
    public QuadVertex[] QuadVertexBufferPtr = null;
    public Texture[] TextureSlots = new Texture[2];
    public int TextrueSlotIndex = 1;
    public Vector3f[] QuadVertexPositions = new Vector3f[4];
}

class VertexArray {
    private int id;
    private List<VertexBuffer> vertexBuffers = new ArrayList<>();
    private int vertexBufferIndex = 0;
    private IndexBuffer indexBuffer;
    public VertexArray() {
        this.id = glCreateVertexArrays();
    }

    public void cleanUp() {
        glDeleteVertexArrays(this.id);
    }

    public void bind() {
        glBindVertexArray(this.id);
    }

    public void unbind() {
        glBindVertexArray(0);
    }

    public void addVertexBuffer(VertexBuffer vertexBuffer) {
        glBindVertexArray(this.id);
        vertexBuffer.bind();
//        List<BufferElement> elementList = vertexBuffer.getBufferLayout().getElementList();
//        for (BufferElement element : elementList) {
//            glEnableVertexAttribArray(vertexBufferIndex);
//            glVertexAttribPointer(vertexBufferIndex, element.type.getSize(), GL_FLOAT, false, vertexBuffer.getBufferLayout().getStride(), element.offset);
//            vertexBufferIndex++;
//        }
        vertexBuffers.add(vertexBuffer);
    }

    void setIndexBuffer(IndexBuffer indexBuffer) {
        glBindVertexArray(this.id);
        indexBuffer.bind();
        this.indexBuffer = indexBuffer;
    }

    public IndexBuffer getIndexBuffer() {
        return indexBuffer;
    }
}

class VertexBuffer {
    private int id;
    private BufferLayout bufferLayout;
    public VertexBuffer(int size) {
        this.id = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, id);
        glBufferData(id, size, GL_DYNAMIC_DRAW);
    }

    public VertexBuffer(float[] vertices, int size) {
        this.id = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, id);
        glBufferData(id, vertices, GL_STATIC_DRAW);
    }

    public void cleanUp() {
        glDeleteBuffers(id);
    }

    public void bind() {
        glBindBuffer(GL_ARRAY_BUFFER, id);
    }

    void unbind() {
        glBindBuffer(GL_ARRAY_BUFFER, 0);
    }

    void setData(FloatBuffer data, int size) {
        glBindBuffer(GL_ARRAY_BUFFER, id);
        glBufferSubData(id, 0, data);
    }

    public BufferLayout getBufferLayout() {
        return bufferLayout;
    }

    public void setBufferLayout(BufferLayout bufferLayout) {
        this.bufferLayout = bufferLayout;
    }
}

class IndexBuffer {
    private int id;
    private int count;
    public IndexBuffer(int[] indices, int count) {
        this.count = indices.length;
        this.id = glCreateBuffers();
        glBindBuffer(GL_ARRAY_BUFFER, id);
        glBufferData(GL_ELEMENT_ARRAY_BUFFER, indices, GL_STATIC_DRAW);
    }

    public void cleanUp() {
        glDeleteBuffers(this.id);
    }

    public void bind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, id);
    }

    public void unbind() {
        glBindBuffer(GL_ELEMENT_ARRAY_BUFFER, 0);
    }

    public int getCount() {
        return count;
    }
}

class QuadVertex {
    public Vector3f Position;
    public Vector4f Color;
    public Vector2f TexCoord;
    public float TexIndex;
    public float TilingFactor;

    private float[] xyz = new float[] {0f, 0f, 0f};
    private float[] rgba = new float[] {1f, 1f, 1f, 1f};
    private float[] st = new float[] {0f, 0f};

    // The amount of bytes an element has
    public static final int elementBytes = 4;

    // Elements per parameter
    public static final int positionElementCount = 3;
    public static final int colorElementCount = 4;
    public static final int textureElementCount = 2;

    // Bytes per parameter
    public static final int positionBytesCount = positionElementCount * elementBytes;
    public static final int colorByteCount = colorElementCount * elementBytes;
    public static final int textureByteCount = textureElementCount * elementBytes;

    // Byte offsets per parameter
    public static final int positionByteOffset = 0;
    public static final int colorByteOffset = positionByteOffset + positionBytesCount;
    public static final int textureByteOffset = colorByteOffset + colorByteCount;
//    public static final int texIndexByteOffset = textureByteOffset + 4;
//    public static final int tilingFactorByteOffset = texIndexByteOffset + 4;

    // The amount of elements that a vertex has
    public static final int elementCount = positionElementCount +
            colorElementCount + textureElementCount;
    // The size of a vertex in bytes, like in C/C++: sizeof(Vertex)
    public static final int stride = positionBytesCount + colorByteCount +
            textureByteCount;

    public float[] getElements() {
        return new float[] {
                0.0f, 0.0f, 0.0f,
                0.0f, 200, 0.0f,
                100, 200, 0.0f,
                100, 0.0f, 0.0f
        };
//        float[] out = new float[QuadVertex.elementCount];
//        int i = 0;
//        // Insert XYZW elements
//        out[i++] = Position.x;
//        out[i++] = Position.y;
//        out[i++] = Position.z;
//
//        // Insert RGBA elements
//        out[i++] = Color.x;
//        out[i++] = Color.y;
//        out[i++] = Color.z;
//        out[i++] = Color.w;
//        // Insert ST elements
//        out[i++] = TexCoord.x;
//        out[i++] = TexCoord.y;
////        out[i++] = TexIndex;
////        out[i++] = TilingFactor;
//
//        return out;
    }
}

class BufferLayout {
    private List<BufferElement> elementList;
    private int stride;

    public BufferLayout(List<BufferElement> elementList) {
        stride = 0;
        int offset = 0;
        for (BufferElement e : elementList) {
            e.offset = offset;
            offset += e.size;
            stride += e.size;
        }
        this.elementList = elementList;
    }

    public List<BufferElement> getElementList() {
        return elementList;
    }

    public int getStride() {
        return stride;
    }
}

class BufferElement {
    public ShaderDataType type;
    public String name;
    public int size;
    public int offset;
    public boolean normalized = false;

    public BufferElement(ShaderDataType type, String name) {
        this.type = type;
        this.name = name;
        this.size = type.getSize();
        this.offset = 0;
    }
}

enum ShaderDataType {
    None(0), Float(4), Float2(4 * 2), Float3(4 * 3), Float4(4 * 4), Mat3(4 * 3 * 3), Mat4(4 * 4 * 4), Int(4), Int2(4 * 2), Int3(4 * 3), Int4(4 * 4), Bool(1);

    private final int size;

    ShaderDataType(int i) {
        this.size = i;
    }

    public int getSize() {
        return size;
    }
}
