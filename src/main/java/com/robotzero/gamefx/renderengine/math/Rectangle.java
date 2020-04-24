package com.robotzero.gamefx.renderengine.math;

import org.joml.Vector2f;
import org.joml.Vector3f;
import org.joml.Vector3i;

public class Rectangle {
    private Vector3f Min;
    private Vector3f Max;
    private Vector3i Mini;
    private Vector3i Maxi;

    public Rectangle(Vector3f min, Vector3f max) {
        Min = min;
        Max = max;
        Mini = new Vector3i(Math.round(min.x), Math.round(min.y), 0);
        Maxi = new Vector3i(Math.round(max.x), Math.round(max.y), 0);
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

    public static boolean IsInRectangle(Rectangle rectangle, Vector3f Test) {
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

    public static Vector3f GetMaxCorner(Rectangle Rect) {
        Vector3f Result = Rect.Max;
        return(Result);
    }

    public Vector3f GetCenter(Rectangle Rect) {
        Vector3f Result = Rect.Min.add(Rect.Max).mul(0.5f);
        return(Result);
    }

    public Rectangle ToRectangleXY(Rectangle A) {
        Rectangle Result = new Rectangle(new Vector3f(A.Min.x, A.Min.y, 0), new Vector3f(A.Max.x, A.Max.y, 0));

        return(Result);
    }

    public static Vector3f GetDim(Rectangle Rect) {
        Vector3f Result = Rect.Max.sub(Rect.Min);
        return(Result);
    }

    public static Vector2f GetDimV2(Rectangle Rect) {
        Vector2f Result = new Vector2f(Rect.getMax().x, Rect.getMax().y).sub(new Vector2f(Rect.getMin().x, Rect.getMin().y));
        return(Result);
    }

    public static Rectangle Intersect(Rectangle A, Rectangle B) {
        float MinX = Math.max(A.getMin().x, B.getMin().x);
        float MinY = Math.max(A.getMin().y, B.getMin().y);
        float MaxX = Math.min(A.getMax().x, B.getMax().x);
        float MaxY = Math.min(A.getMax().y, B.getMax().y);

        return new Rectangle(new Vector3f(MinX, MinY, 0), new Vector3f(MaxX, MaxY, 0));
    }

    public static Rectangle InvertedInfinityRectangle() {
        float MinX = Integer.MAX_VALUE;
        float MinY = Integer.MAX_VALUE;
        float MaxX = -Integer.MAX_VALUE;
        float MaxY = -Integer.MAX_VALUE;

        return new Rectangle(new Vector3f(MinX, MinY, 0), new Vector3f(MaxX, MaxY, 0));
    }

    public Vector3i getMini() {
        return Mini;
    }

    public Vector3i getMaxi() {
        return Maxi;
    }

    public void setMiniy(int y) {
        this.Mini = new Vector3i(getMini().x, y, 0);
        this.Min = new Vector3f(getMin().x, y, 0);
    }
}
