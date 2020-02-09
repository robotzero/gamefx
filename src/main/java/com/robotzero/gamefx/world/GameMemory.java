package com.robotzero.gamefx.world;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryUtil;
import org.lwjgl.system.Pointer;
import org.lwjgl.system.StructBuffer;

import java.nio.ByteBuffer;

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
    private static ByteBuffer tiles;

    public GameMemory() {
        mainStorage = org.lwjgl.system.MemoryUtil.memAlloc(PermanentStorageSize + TransientStorageSize);
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
        mainStorage.clear();
        tiles.clear();
        MemoryUtil.memFree(tiles);
        MemoryUtil.memFree(mainStorage);
    }

    public static ByteBuffer allocateTiles(int size) {
        tiles = MemoryUtil.memAlloc(size);
        return tiles;
    }
}
