package com.robotzero.gamefx.renderengine.assets;

import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.world.GameMemory;
import org.lwjgl.opengl.GL;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AssetService {
    ExecutorService executorService;
    GameMemory gameMemory;
    DisplayManager displayManager;
    private long sharedWindow;

    public AssetService(ExecutorService executorService, GameMemory gameMemory, DisplayManager displayManager) {
        this.executorService = executorService;
        this.gameMemory = gameMemory;
        this.displayManager = displayManager;
    }

    public void LoadAssets(String path) {
        URL resources = this.getClass().getClassLoader().getResource(path);
        try {
            final var resourcesPath = Paths.get(resources.toURI());
            Stream<Path> files = Files.list(resourcesPath);
            sharedWindow = createOpenGLContextForWorkerThread();
            CompletableFuture.runAsync(() -> {
                glfwInit();
                glfwMakeContextCurrent(sharedWindow);
                GL.createCapabilities();
                files.forEach(asset -> {
                    Asset asset1 = new Asset();
                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
                    gameMemory.gameAssets.put(asset1.getFileName(), asset1);
                });
            }, executorService).whenComplete((void_, throwable) -> {
                glfwSwapBuffers(sharedWindow);
               Optional.ofNullable(throwable).ifPresent(t -> {
                   t.printStackTrace();
                   throw new RuntimeException("Failed to load assets");
               });
            });
//            Thread t = new Thread(() -> {
//                glfwInit();
//                glfwMakeContextCurrent(sharedWindow);
//                GL.createCapabilities();
//                files.forEach(asset -> {
//                    Asset asset1 = new Asset();
//                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
//                    gameMemory.gameAssets.put(asset1.getFileName(), asset1);
//                });
//            });
////            t.start();
//            executorService.submit(t);
//            executorService.shutdown();
            glfwMakeContextCurrent(displayManager.getWindow());
        } catch (IOException | NullPointerException | URISyntaxException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load assets");
        }
    }

    public void cleanUp() {
        gameMemory.gameAssets.entrySet().forEach(assetEntry -> {
            Optional.ofNullable(assetEntry.getValue().getTexture()).ifPresent(texture -> {
                texture.unbind();
                texture.delete();
            });
            Optional.ofNullable(assetEntry.getValue().getData()).ifPresent(ByteBuffer::clear);
        });
        gameMemory.gameAssets.clear();
    }

    public long getSharedWindow() {
        return sharedWindow;
    }

    public long createOpenGLContextForWorkerThread() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        return glfwCreateWindow(1, 1, "", NULL, displayManager.getWindow());
    }
}
