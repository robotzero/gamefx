package com.robotzero.gamefx.renderengine.model;

import com.robotzero.gamefx.renderengine.utils.FileUtils;
import org.lwjgl.opengl.GL45;
import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;

import static org.lwjgl.opengl.GL45.glBindTextureUnit;
import static org.lwjgl.opengl.GL45.glCreateTextures;
import static org.lwjgl.opengl.GL45.glTextureParameteri;
import static org.lwjgl.opengl.GL45.glTextureStorage2D;
import static org.lwjgl.opengl.GL45.glTextureSubImage2D;
import static org.lwjgl.system.MemoryStack.stackPush;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Texture {
    private int width, height;
    private int id;

    public Texture(String path) {
        try {
            ByteBuffer textureData =  FileUtils.ioResourceToByteBuffer(path, 1024);
            load(textureData);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Texture(int width, int height, int[] data) {
        this.width = width;
        this.height = height;

        int InternalFormat = GL_RGBA8;
        int DataFormat = GL_RGBA;

        this.id = glCreateTextures(GL_TEXTURE_2D);
        glTextureStorage2D(this.id, 1, InternalFormat, width, height);

        glTextureParameteri(this.id, GL_TEXTURE_MIN_FILTER, GL_LINEAR);
        glTextureParameteri(this.id, GL_TEXTURE_MAG_FILTER, GL_NEAREST);

        glTextureParameteri(this.id, GL_TEXTURE_WRAP_S, GL_REPEAT);
        glTextureParameteri(this.id, GL_TEXTURE_WRAP_T, GL_REPEAT);

        int bpp = DataFormat == GL_RGBA ? 4 : 3;
        glTextureSubImage2D(this.id, 0, 0, 0, width, height, DataFormat, GL_UNSIGNED_BYTE, data);

    }

    private void load(ByteBuffer imageData) {
        try (MemoryStack stack = stackPush()) {
            IntBuffer w = stack.mallocInt(1);
            IntBuffer h = stack.mallocInt(1);
            IntBuffer avChannels = stack.mallocInt(1);

            // Decode texture image into a byte buffer
            ByteBuffer decodedImage = stbi_load_from_memory(imageData, w, h, avChannels, 4);

            this.width = w.get();
            this.height = h.get();

            // Create a new OpenGL texture
            this.id = glGenTextures();
            // Bind the texture
            glBindTexture(GL_TEXTURE_2D, this.id);

            // Tell OpenGL how to unpack the RGBA bytes. Each component is 1 byte size
            glPixelStorei(GL_UNPACK_ALIGNMENT, 1);

            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
            glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
            // Upload the texture data
            glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, this.width, this.height, 0, GL_RGBA, GL_UNSIGNED_BYTE, decodedImage);
            // Generate Mip Map
//            glGenerateMipmap(GL_TEXTURE_2D);

            stbi_image_free(imageData);
        }
    }


    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
    }

    public void bind(int i) {
        glBindTextureUnit(i, this.id);
    }

    public void unbind() {
        glBindTexture(GL_TEXTURE_2D, 0);
    }

    public int getWidth() {
        return this.width;
    }

    public int getHeight() {
        return this.height;
    }

    public void cleanup() {
        glDeleteTextures(this.id);
    }
}
