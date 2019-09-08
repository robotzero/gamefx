package com.robotzero.gamefx;

public class Vec2D {
    @Override
    public String toString() {
        return "Vec2D{" +
                "x=" + x +
                ", y=" + y +
                '}';
    }

    private final double x;
    private final double y;

    public Vec2D(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public Vec2D add(Vec2D two) {
        return new Vec2D(this.getX() + two.getX(), this.getY() + two.getY());
    }

    public Vec2D addScalar(double scalar) {
        return new Vec2D(this.getX() + scalar, this.getY() + scalar);
    }
}
