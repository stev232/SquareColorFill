package com.example.squarecolorfill;

public class ScaleSetter {
    private int width, squareHeightWidth;

    public void setScreenWidth(int width) { this.width = width; }
    public int getScreenWidth() { return this.width; }

    public ScaleSetter (int screenWidth) {
        setScreenWidth(screenWidth);
        setSquareHeightWidth(getScreenWidth()/4);
    }

    public void setSquareHeightWidth(int squareHeightWidth) {
        this.squareHeightWidth = squareHeightWidth;
    }

    public int getHeightWidth() {
        return this.squareHeightWidth;
    }
}
