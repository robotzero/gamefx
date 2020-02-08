package com.robotzero.gamefx.world;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.Pointer;

import static org.lwjgl.system.MemoryStack.stackPush;

public class MemoryArena {
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

    private int Size;
    private int Used;
    private Pointer Base;
    private MemoryStack arena;
    private PointerBuffer test;


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
        try ( MemoryStack stack = stackPush() ) {
            this.arena = stack;
            this.test = stack.mallocPointer(size);
        } // the stack frame is popped automatically
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
