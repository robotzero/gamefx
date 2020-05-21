package com.robotzero.gamefx.renderengine.assets;

import com.robotzero.gamefx.renderengine.DisplayManager;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.lwjgl.glfw.GLFW.*;
import static org.lwjgl.opengl.GL11.glGenTextures;
import static org.lwjgl.system.MemoryUtil.NULL;

public class AssetService {
    ExecutorService executorService;
    GameMemory gameMemory;
    public static long sharedWindow;

    public AssetService(ExecutorService executorService, GameMemory gameMemory) {
        this.executorService = executorService;
        this.gameMemory = gameMemory;
    }

    public void LoadAssets(String path) {
        URL resources = this.getClass().getClassLoader().getResource(path);
        try {
            final var resourcesPath = Paths.get(resources.toURI());
            Stream<Path> files = Files.list(resourcesPath);
            createOpenGLContextForWorkerThread();
            Thread thread = new Thread(() -> {
                glfwInit();
                glfwMakeContextCurrent(sharedWindow);
                GL.createCapabilities();
                Asset asset1 = new Asset();
                asset1.loadAsset(resourcesPath + "/" + "tree00.bmp");
                Asset asset2 = new Asset();
                asset2.loadAsset(resourcesPath + "/" + "fred_01.png");
                Asset asset3 = new Asset();
                asset3.loadAsset(resourcesPath + "/" + "fred_01.png");
                gameMemory.gameAssets.put("tree00.bmp", asset1);
                gameMemory.gameAssets.put("fred_01.png", asset2);
                gameMemory.gameAssets.put("bird.png", asset3);
            });
//            executorService.submit(() -> {
//                glfwInit();
//                glfwMakeContextCurrent(sharedWindow);
//                GL.createCapabilities();
//                Asset asset1 = new Asset();
//                asset1.loadAsset(resourcesPath + "/" + "tree00.bmp");
//                Asset asset2 = new Asset();
//                asset2.loadAsset(resourcesPath + "/" + "fred_01.png");
//                Asset asset3 = new Asset();
//                asset3.loadAsset(resourcesPath + "/" + "fred_01.png");
//                gameMemory.gameAssets.put("tree00.bmp", asset1);
//                gameMemory.gameAssets.put("fred_01.png", asset2);
//                gameMemory.gameAssets.put("bird.png", asset3);
//            });

            thread.start();
//            CompletableFuture.supplyAsync(() -> {
////                glfwInit();
//                glfwMakeContextCurrent(sharedWindow);
//                GL.createCapabilities();
//                return files.map(asset -> {
//                    Asset asset1 = new Asset();
//                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
//                    return asset1;
//                }).collect(Collectors.toList());
//            }, executorService).thenAcceptAsync(assetList -> {
//                assetList.parallelStream().forEach(asset -> {
//                    gameMemory.gameAssets.put(asset.getFileName(), asset);
//                });
//            }).whenComplete((a, b) -> {
//                glfwMakeContextCurrent(DisplayManager.getWindow());
//            });
//            glfwInit();
//            RenderGroupService.sequence(files.map(asset -> {
//                return CompletableFuture.supplyAsync(() -> {
//                    Asset asset1 = new Asset();
//                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
//                    return asset1;
//                }, executorService);
//            }).collect(Collectors.toList())).join().parallelStream().forEach(asset -> {
//                gameMemory.gameAssets.put(asset.getFileName(), asset);
//            });

//            files.map(asset -> {
//                    Asset asset1 = new Asset();
//                        asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
//                        return asset1;
//            }).collect(Collectors.toList()).parallelStream().forEach(asset -> {
//                if (asset != null) {
//                    gameMemory.gameAssets.put(asset.getFileName(), asset);
//                }
//            });
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

    private void createOpenGLContextForWorkerThread() {
        glfwWindowHint(GLFW_VISIBLE, GLFW_FALSE);
        sharedWindow = glfwCreateWindow(1, 1, "", NULL, DisplayManager.getWindow());
    }
}
