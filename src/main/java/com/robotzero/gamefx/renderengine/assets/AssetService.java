package com.robotzero.gamefx.renderengine.assets;

import com.robotzero.gamefx.renderengine.model.Texture;
import com.robotzero.gamefx.renderengine.rendergroup.RenderGroupService;
import com.robotzero.gamefx.world.GameMemory;

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

public class AssetService {
    ExecutorService executorService;
    GameMemory gameMemory;

    public AssetService(ExecutorService executorService, GameMemory gameMemory) {
        this.executorService = executorService;
        this.gameMemory = gameMemory;
    }

    public void LoadAssets(String path) {
        URL resources = this.getClass().getClassLoader().getResource(path);
        try {
            final var resourcesPath = Paths.get(resources.toURI().getPath());
            Stream<Path> files = Files.list(resourcesPath);
            RenderGroupService.sequence(files.map(asset -> {
                return CompletableFuture.supplyAsync(() -> {
                    Asset asset1 = new Asset();
                    asset1.loadAsset(resourcesPath + "/" + asset.getFileName().toString());
                    return asset1;
                }, executorService);
            }).collect(Collectors.toList())).join().parallelStream().forEach(asset -> {
                gameMemory.gameAssets.put(asset.getFileName(), asset);
            });
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
}
