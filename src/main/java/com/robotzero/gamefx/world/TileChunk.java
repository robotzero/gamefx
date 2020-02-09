package com.robotzero.gamefx.world;

import java.nio.ByteBuffer;

public class TileChunk {
    private ByteBuffer tiles;

    public TileChunk() {
//        byte[] b = pushArray();
//        try ( MemoryStack stack = stackPush() ) {
//            this.tiles = stack.malloc(b.length);
//            this.tiles.put(b);
//        } // the stack frame is popped automatically
    }

    public ByteBuffer getTiles() {
        return tiles;
    }

    public void setTile(int coords, byte value) {
        this.tiles.put(coords, value);
    }

    public void setTiles(ByteBuffer tiles) {
        this.tiles = tiles;
    }

//    private byte[] pushArray() {
//        byte[] arr = new byte[TileMap.ChunkDim * TileMap.ChunkDim];
//        for (int i = 0; i < TileMap.ChunkDim * TileMap.ChunkDim; ++i) {
//            arr[i] = 0;
//        }
//        return arr;
//    }
}