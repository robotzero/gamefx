package com.robotzero.gamefx.renderengine.math;

import org.joml.Vector2f;

public class Rectangle {
    private final Vector2f Min;
    private final Vector2f Max;

    public Rectangle(Vector2f min, Vector2f max) {
        Min = min;
        Max = max;
    }

    public static Rectangle RectMinMax(Vector2f Min, Vector2f Max) {
        Rectangle Result = new Rectangle(Min, Max);

        return(Result);
    }

    public static Rectangle RectMinDim(Vector2f Min, Vector2f Dim) {
        final Vector2f min = Min;
        final Vector2f max = new Vector2f(Min).add(new Vector2f(Dim));

        Rectangle Result = new Rectangle(min, max);
        return(Result);
    }

    public static Rectangle RectCenterHalfDim(Vector2f Center, Vector2f HalfDim) {

        final Vector2f Min = new Vector2f(Center).sub(new Vector2f(HalfDim));
        final Vector2f Max = new Vector2f(Center).add(new Vector2f(HalfDim));

        Rectangle Result = new Rectangle(Min, Max);
        return(Result);
    }

    public static Rectangle RectCenterDim(Vector2f Center, Vector2f Dim) {
        Rectangle Result = RectCenterHalfDim(Center, new Vector2f(Dim).mul(0.5f));

        return(Result);
    }

    public static boolean IsInRectangle(Rectangle rectangle, Vector2f Test)
    {
        boolean Result = ((Test.x() >= rectangle.Min.x()) &&
                (Test.y() >= rectangle.Min.y()) &&
                (Test.x() < rectangle.Max.x()) &&
                (Test.y() < rectangle.Max.y()));

        return(Result);
    }

    public Vector2f getMin() {
        return this.Min;
    }

    public Vector2f getMax() {
        return this.Max;
    }

    public static Vector2f GetMinCorner(Rectangle Rect) {
        Vector2f Result = Rect.Min;
        return(Result);
    }

    public static Vector2f GetMaxCorner(Rectangle Rect)
    {
        Vector2f Result = Rect.Max;
        return(Result);
    }

    public Vector2f GetCenter(Rectangle Rect)
    {
        Vector2f Result = Rect.Min.add(Rect.Max).mul(0.5f);
        return(Result);
    }
}
