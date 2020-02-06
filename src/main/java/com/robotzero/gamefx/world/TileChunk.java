package com.robotzero.gamefx.world;

import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;

import static org.lwjgl.system.MemoryStack.stackPush;

public class TileChunk {
    private ByteBuffer tiles;

    public TileChunk(byte[] tiles) {
        try ( MemoryStack stack = stackPush() ) {
            this.tiles = stack.malloc(tiles.length);
            this.tiles.put(tiles);
        } // the stack frame is popped automatically
    }

    public ByteBuffer getTiles() {
        return tiles;
    }
}