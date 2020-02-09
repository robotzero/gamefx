package com.robotzero.gamefx.world;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

public class MemoryArena {
    private int Size;
    private int Used;
    private Pointer Base;
    private MemoryStack arena;
    private PointerBuffer test;
    private final GameMemory gameMemory;

    public MemoryArena(GameMemory gameMemory) {
        this.gameMemory = gameMemory;
    }

    public int getSize() {
        return Size;
    }

    public int getUsed() {
        return Used;
    }

    public void setSize(int size) {
        Size = size;
    }

    public void setUsed(int used) {
        Used = used;
    }

    public void initializeArena(int size, Pointer base) {
        this.Base = base;
        this.Size = size;
        this.Used = 0;
    }

    public int pushSize(int size) {
        this.arena.setPointer(arena.getPointer() + getUsed());
        int result = arena.getPointer();
        this.setUsed(this.getUsed() + size);
        return result;
    }

    public int pushArray(int count, int type) {
        int size = count * type;
        this.arena.setPointer(arena.getPointer() + getUsed());
        int result = this.arena.getPointer();
        this.setUsed(this.getUsed() + size);
        return result;
    }
}
