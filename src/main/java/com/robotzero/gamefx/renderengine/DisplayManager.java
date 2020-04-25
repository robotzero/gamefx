package com.robotzero.gamefx.renderengine;

import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GLUtil;
import org.lwjgl.system.MemoryStack;

import java.nio.IntBuffer;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.GL_BLEND;
import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_FALSE;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_TRUE;
import static org.lwjgl.opengl.GL11.glBlendFunc;
import static org.lwjgl.opengl.GL11.glClearColor;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.system.MemoryUtil.memAddress;

public class DisplayManager {
    public static int WIDTH = 1920;
    public static int HEIGHT = 1080;
    public static float refreshRate = 60f;
    public static String TITLE = "Fred64";
    private static long window;


    public void createDisplay() {
//        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();

//        GraphicsDevice[] devices = env.getScreenDevices();
//        refreshRate = Arrays.stream(devices)
//                .filter(dinfo -> dinfo.getDisplayMode().getRefreshRate() != 0)
//                .map(dinfo -> dinfo.getDisplayMode().getRefreshRate())
//                .findAny()
//                .orElse(60);

        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit()) {
            throw new IllegalStateException("Unable to initialize GLFW");
        }

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
        if ( window == NULL ) {
            throw new RuntimeException("Failed to create the GLFW window");
        }

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE ) {
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
            }
        });

        glfwSetErrorCallback(new GLFWErrorCallback() {
            @Override
            public void invoke(int error, long description) {
                System.out.println(GLFWErrorCallback.getDescription(description));
            }
        });

        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            if (width > 0 && height > 0 && (WIDTH != width || HEIGHT != height)) {
                WIDTH = width;
                HEIGHT = height;
            }
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
        GLUtil.setupDebugMessageCallback();

//        glDepthFunc(GL_NEVER);
        glEnable(GL_DEPTH_TEST);
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

        glfwWindowHint(GLFW_SAMPLES, 4);
    }

    public void setClearColor(float r, float g, float b, float alpha) {
        glClearColor(r, g, b, alpha);
    }

    public boolean isKeyPressed(int keyCode, boolean initialized) {
//        if (keyCode == GLFW_KEY_D && initialized) {
//            return true;
//        }
        return glfwGetKey(window, keyCode) == GLFW_PRESS;
    }

    public boolean windowShouldClose() {
        return glfwWindowShouldClose(window);
    }

    public void updateDisplay() {
        glfwSwapBuffers(window);
        glfwPollEvents();
    }

    public static void closeDisplay() {
//        Display.destroy();
    }

    public boolean isClosing() {
        return glfwWindowShouldClose(this.window);
    }

    public long getWindow() {
        return window;
    }

    public void setWindowTitle(String title) {
        glfwSetWindowTitle(window, title);
    }

    public static long getW() {
        return window;
    }
}
