package com.robotzero.gamefx.renderengine;

import org.lwjgl.glfw.*;
import org.lwjgl.opengl.*;
import org.lwjgl.system.MemoryStack;

import java.nio.*;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.*;

public class DisplayManager {
    private long window;
    private GLFWErrorCallback errorCallback;
    private GLFWKeyCallback   keyCallback;
    private static final int WIDTH = 1280;
    private static final int HEIGHT = 720;
    private static final int FPS_CAP = 60;
    private static final String TITLE = "Our First Display";
    private static float counter = 0.0f;
    private static final double pi = 3.1415926535897932384626433832795;

    public void createDisplay() {
        GLFWErrorCallback.createPrint(System.err).set();

        // Initialize GLFW. Most GLFW functions will not work before doing this.
        if ( !glfwInit())
            throw new IllegalStateException("Unable to initialize GLFW");

        // Configure GLFW
        glfwDefaultWindowHints(); // optional, the current window hints are already the default
        glfwWindowHint(GLFW_VISIBLE, GL_FALSE); // the window will stay hidden after creation
        glfwWindowHint(GLFW_RESIZABLE, GL_TRUE); // the window will be resizable

        // Create the window
        window = glfwCreateWindow(WIDTH, HEIGHT, TITLE, NULL, NULL);
        if ( window == NULL )
            throw new RuntimeException("Failed to create the GLFW window");

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window, (window, key, scancode, action, mods) -> {
            if ( key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE )
                glfwSetWindowShouldClose(window, true); // We will detect this in the rendering loop
        });
        glfwSetFramebufferSizeCallback(window, (window, width, height) -> {
            glViewport(0, 0, width, height);
            // Creating perspective

            this.applyProjection();
        });

        try ( MemoryStack stack = stackPush() ) {
            IntBuffer pWidth = stack.mallocInt(1); // int*
            IntBuffer pHeight = stack.mallocInt(1); // int*

            // Get the window size passed to glfwCreateWindow
            glfwGetWindowSize(window, pWidth, pHeight);

            // Get the resolution of the primary monitor
            GLFWVidMode vidmode = glfwGetVideoMode(glfwGetPrimaryMonitor());

            // Center the window
            glfwSetWindowPos(
                    window,
                    (vidmode.width() - pWidth.get(0)) / 2,
                    (vidmode.height() - pHeight.get(0)) / 2
            );
        } // the stack frame is popped automatically


        // Make the OpenGL context current
        glfwMakeContextCurrent(window);

        // Enable v-sync
        glfwSwapInterval(1);

        // Make the window visible
        glfwShowWindow(window);
    }

    // Main loop
    public void updateDisplay() {
        GL.createCapabilities();

        // Set the clear color
        glClearColor(0.8f, 0.0f, 0.0f, 0.0f);
        glEnable(GL_DEPTH_TEST);
        glDepthFunc(GL_LEQUAL);

        this.applyProjection();

        // Run the rendering loop until the user has attempted to close
        // the window or has pressed the ESCAPE key.
        while ( !glfwWindowShouldClose(window) ) {
            glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT); // clear the framebuffer
            drawTriangleOldWay();
            glfwSwapBuffers(window); // swap the color buffers
            // Poll for window events. The key callback above will only be
            // invoked during this call.
            glfwPollEvents();
        }
    }

    private void drawTriangleOldWay() {
        //Identity matrix for rotation every frame
        glLoadIdentity();
        glTranslatef(counter/150.0f, 0.0f, -4.0f);
        glRotatef(counter, 0.0f, 1.0f, 0.0f);
        counter += 0.5f;
        if (counter > 200f) {
            counter = -200.0f;
        }
        glBegin(GL_TRIANGLES);

        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(-0.5f, -0.5f, 0.0f);

        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(0.5f, -0.5f, 0.0f);

        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.0f, 0.5f, 0.0f);
        glEnd();

        glLoadIdentity();
        glTranslatef(0.0f, -0.5f, -4.0f);
        glRotatef(5f, 0.0f, 1.0f, 0.0f);
        glBegin(GL_TRIANGLES);

        glColor3f(1.0f, 0.0f, 0.0f);
        glVertex3f(-0.9f, -0.1f, 0.0f);

        glColor3f(0.0f, 0.0f, 1.0f);
        glVertex3f(0.1f, -0.1f, 0.0f);

        glColor3f(0.0f, 1.0f, 0.0f);
        glVertex3f(-0.5f, 0.9f, 0.0f);

        glEnd();
    }

    private void gluPerspective(float fovY, float zNear, float zFar, int width, int height) {
        float aspect = (float) width/ (float) height;
        float fH = (float) Math.tan( (fovY / 360.0f * pi) ) * zNear;
        float fW = fH * aspect;
        // gluPerspective
        glFrustum( -fW, fW, -fH, fH, zNear, zFar );
    }

    private void applyProjection() {
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        gluPerspective(60, 1.0f, 10.0f, WIDTH, HEIGHT);
        glMatrixMode(GL_MODELVIEW);
    }
}
