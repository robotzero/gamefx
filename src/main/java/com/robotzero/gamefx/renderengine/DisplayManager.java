package main.java.com.robotzero.gamefx.renderengine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWKeyCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class DisplayManager {
    private long window;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback   keyCallback;
    public static int WIDTH = 1280;
    public static int HEIGHT = 720;
    private static final int FPS_CAP = 60;
    private static final String TITLE = "Our First Display";
    private static float counter = 0.0f;
    private static final double pi = 3.1415926535897932384626433832795;

    public long getWindow() {
        return this.window;
    }

    public void createDisplay() {
//        floatBuffer.put(vertices);
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable
        glfwWindowHint(GLFW_OPENGL_DEBUG_CONTEXT, GLFW_TRUE);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MAJOR, 3);
        glfwWindowHint(GLFW_CONTEXT_VERSION_MINOR, 2);
        glfwWindowHint(GLFW_OPENGL_PROFILE, GLFW_OPENGL_CORE_PROFILE);
        glfwWindowHint(GLFW_OPENGL_FORWARD_COMPAT, GLFW_TRUE);

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });

        glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                System.out.println(GLFWErrorCallback.getDescription(description));
            }
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            if (width > 0
                    && height > 0
                    && (WIDTH != width || HEIGHT != height)) {
                WIDTH = width;
                HEIGHT = height;
            }
            // Creating perspective

//            this.applyProjection();
        });

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer framebufferSize = stack.mallocInt(2);
            nglfwGetFramebufferSize(window, memAddress(framebufferSize), memAddress(framebufferSize) + 4);
            WIDTH = framebufferSize.get(0);
            HEIGHT = framebufferSize.get(1);
        } // the stack frame is popped automatically


        GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());
        glfwSetWindowPos(window, (vidmode.width() - WIDTH) / 2, (vidmode.height() - HEIGHT) / 2);

        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync (1)
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);

        GL.createCapabilities();
    }
}
