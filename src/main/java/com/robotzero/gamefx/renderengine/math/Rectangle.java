package com.robotzero.gamefx.renderengine.math;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Rectangle {
    private final Vector3f Min;
    private final Vector3f Max;

    public Rectangle(Vector3f min, Vector3f max) {
        Min = min;
        Max = max;
    }

    public static Rectangle RectMinMax(Vector3f Min, Vector3f Max) {
        Rectangle Result = new Rectangle(Min, Max);

        return(Result);
    }

    public static Rectangle RectMinDim(Vector3f Min, Vector3f Dim) {
        final Vector3f min = Min;
        final Vector3f max = new Vector3f(Min).add(new Vector3f(Dim));

        Rectangle Result = new Rectangle(min, max);
        return(Result);
    }

    public static Rectangle RectCenterHalfDim(Vector3f Center, Vector3f HalfDim) {

        final Vector3f Min = new Vector3f(Center).sub(new Vector3f(HalfDim));
        final Vector3f Max = new Vector3f(Center).add(new Vector3f(HalfDim));

        Rectangle Result = new Rectangle(Min, Max);
        return(Result);
    }

    public static Rectangle RectCenterDim(Vector3f Center, Vector3f Dim) {
        Rectangle Result = RectCenterHalfDim(Center, new Vector3f(Dim).mul(0.5f));

        return(Result);
    }

    public static boolean IsInRectangle(Rectangle rectangle, Vector3f Test)
    {
        boolean Result = ((Test.x() >= rectangle.Min.x()) &&
                (Test.y() >= rectangle.Min.y()) &&
                (Test.x() < rectangle.Max.x()) &&
                (Test.y() < rectangle.Max.y()));

        return(Result);
    }

    public Vector3f getMin() {
        return this.Min;
    }

    public Vector3f getMax() {
        return this.Max;
    }

    public static Vector3f GetMinCorner(Rectangle Rect) {
        Vector3f Result = Rect.Min;
        return(Result);
    }

    public static Vector3f GetMaxCorner(Rectangle Rect)
    {
        Vector3f Result = Rect.Max;
        return(Result);
    }

    public Vector3f GetCenter(Rectangle Rect)
    {
        Vector3f Result = Rect.Min.add(Rect.Max).mul(0.5f);
        return(Result);
    }

    public Rectangle ToRectangleXY(Rectangle A) {
        Rectangle Result = new Rectangle(new Vector3f(A.Min.x, A.Min.y, 0), new Vector3f(A.Max.x, A.Max.y, 0));

        return(Result);
    }

    public static Vector3f GetDim(Rectangle Rect)
    {
        Vector3f Result = Rect.Max.sub(Rect.Min);
        return(Result);
    }

    public static Vector2f GetDimV2(Rectangle Rect)
    {
        Vector2f Result = new Vector2f(Rect.getMax().x, Rect.getMax().y).sub(new Vector2f(Rect.getMin().x, Rect.getMin().y));
        return(Result);
    }
}
