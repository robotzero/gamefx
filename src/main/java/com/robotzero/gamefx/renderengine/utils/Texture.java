package main.java.com.robotzero.gamefx.renderengine.utils;

import org.lwjgl.system.MemoryStack;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import static org.lwjgl.system.MemoryStack.stackPush;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.stb.STBImage.stbi_image_free;
import static org.lwjgl.stb.STBImage.stbi_load_from_memory;

public class Texture {
    private int width, height;
//    private int texture;
    private int id;

    public Texture(String path) {
//        texture = load(path);
        try {
            ByteBuffer blah =  FileUtils.ioResourceToByteBuffer(path, 1024);
            load2(blah);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void load2(ByteBuffer imageData) {
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


//    private int load(String path) {
//        int[] pixels;
//        try {
//            BufferedImage image = ImageIO.read(new FileInputStream(path));
//            width = image.getWidth();
//            height = image.getHeight();
//            pixels = new int[width * height];
//            image.getRGB(0, 0, width, height, pixels, 0, width);
//        } catch (IOException e) {
//            e.printStackTrace();
//            throw new RuntimeException(e.getMessage());
//        }
//
//        int[] data = new int[width * height];
//            for (int i = 0; i < width * height; i++) {
//                int a = (pixels[i] & 0xff000000) >> 24;
//                int r = (pixels[i] & 0xff0000) >> 16;
//                int g = (pixels[i] & 0xff00) >> 8;
//                int b = (pixels[i] & 0xff);
//
//                data[i] = a << 24 | b << 16 | g << 8 | r;
//            }
//
//        int result = glGenTextures();
//        glBindTexture(GL_TEXTURE_2D, result);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MIN_FILTER, GL_NEAREST);
//        glTexParameteri(GL_TEXTURE_2D, GL_TEXTURE_MAG_FILTER, GL_NEAREST);
//        glTexImage2D(GL_TEXTURE_2D, 0, GL_RGBA, width, height, 0, GL_RGBA, GL_UNSIGNED_BYTE, BufferUtils.createIntBuffer(data));
//        glBindTexture(GL_TEXTURE_2D, 0);
//        return result;
//    }

//    public void bind() {
//        glBindTexture(GL_TEXTURE_2D, texture);
//    }

    public void bind() {
        glBindTexture(GL_TEXTURE_2D, this.id);
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
