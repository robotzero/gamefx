package com.robotzero.gamefx.world;

import com.robotzero.gamefx.renderengine.entity.ControlledHero;
import com.robotzero.gamefx.renderengine.entity.LowEntity;
import com.robotzero.gamefx.renderengine.entity.SimEntity;
import com.robotzero.gamefx.renderengine.entity.SimRegion;
import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.StructBuffer;

import java.nio.ByteBuffer;
import java.util.LinkedHashMap;
import java.util.Map;

public class GameMemory {
    public static final int OBJECT_SHELL_SIZE   = 8;
    public static final int OBJREF_SIZE         = 4;
    public static final int LONG_FIELD_SIZE     = 8;
    public static final int INT_FIELD_SIZE      = 4;
    public static final int SHORT_FIELD_SIZE    = 2;
    public static final int CHAR_FIELD_SIZE     = 2;
    public static final int BYTE_FIELD_SIZE     = 1;
    public static final int BOOLEAN_FIELD_SIZE  = 1;
    public static final int DOUBLE_FIELD_SIZE   = 8;
    public static final int FLOAT_FIELD_SIZE    = 4;

    private boolean isInitialized;
    private PointerBuffer PermanentStorage;
    private PointerBuffer TransientStorage;
    private StructBuffer structBuffer;
    private int PermanentStorageSize = Megabytes(64);
    private int TransientStorageSize = Gigabytes(1);
    private ByteBuffer mainStorage;
    private PointerBuffer b;
    private static Map<Long, ByteBuffer> tiles = new LinkedHashMap<>();
    public LowEntity[] LowEntities = new LowEntity[100000];
    public int CameraFollowingEntityIndex = 0;
    public int LowEntityCount = 0;
    public int HighEntityCount = 0;
    public SimRegion simRegion;
    public ControlledHero ControlledHero;

    public GameMemory() {
        mainStorage = org.lwjgl.system.MemoryUtil.memAlloc(PermanentStorageSize + TransientStorageSize);
        ControlledHero = new ControlledHero();
    }

    public boolean isInitialized() {
        return isInitialized;
    }

    public void setInitialized(boolean initialized) {
        isInitialized = initialized;
    }

    public Pointer getPermanentStorage() {
        return PermanentStorage;
    }

    public Pointer getTransientStorage() {
        return TransientStorage;
    }

    public int Kilobytes(int value) {
        return value * 1024;
    }

    public int Megabytes(int value) {
        return Kilobytes(value * 1024);
    }

    public int Gigabytes(int value) {
        return Megabytes(value * 1024);
    }

    public void free() {
        try {
            mainStorage.clear();
            for (long i = 0; i < tiles.size(); i++) {
                if (tiles.get(i) != null) {
                    tiles.get(i).clear();
                    MemoryUtil.memFree(tiles.get(i));
                }
            }
            tiles.clear();
            MemoryUtil.memFree(mainStorage);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public static ByteBuffer allocateTiles(int size, long hash) {
        final var localtiles = MemoryUtil.memAlloc(size);
        for(int i = 0; i < size; i++) {
            localtiles.put((byte)1);
        }
        tiles.put(hash, localtiles);
        return localtiles;
    }
}
