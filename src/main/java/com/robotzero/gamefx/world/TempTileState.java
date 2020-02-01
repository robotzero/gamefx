package com.robotzero.gamefx.world;

public class TempTileState {
    private float PlayerX = 150f;

    private float PlayerY = 150f;

    private int PlayerTileMapX;
    private int PlayerTileMapY;

    public float getPlayerX() {
        return PlayerX;
    }

    public void setPlayerX(float playerX) {
        PlayerX = playerX;
    }

    public int getPlayerTileMapX() {
        return PlayerTileMapX;
    }

    public void setPlayerTileMapX(int playerTileMapX) {
        PlayerTileMapX = playerTileMapX;
    }
}
