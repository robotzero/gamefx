package com.robotzero.gamefx.renderengine.math;

import org.joml.Vector2f;
import org.joml.Vector3f;

public class Calc {

    public float Lerp(float A, float t, float B) {
        float Result = (1.0f - t) * A + t * B;
        return Result;
    }

    public float Clamp(float Min, float Value, float Max) {
        float Result = Value;

        if (Result < Min) {
            Result = Min;
        } else if(Result > Max) {
            Result = Max;
        }

        return(Result);
    }

    public float Clamp01(float Value) {
        float Result = Clamp(0.0f, Value, 1.0f);

        return(Result);
    }

    public Vector3f Clamp01(Vector3f Value) {
        Vector3f Result = new Vector3f();

        Result.x = Clamp01(Value.x);
        Result.y = Clamp01(Value.y);
        Result.z = Clamp01(Value.z);

        return(Result);
    }

    public float SafeRatioN(float Numerator, float Divisor, float N) {
        float Result = N;

        if (Divisor != 0.0f) {
            Result = Numerator / Divisor;
        }

        return(Result);
    }

    public float SafeRatio0(float Numerator, float Divisor) {
        float Result = SafeRatioN(Numerator, Divisor, 0.0f);

        return(Result);
    }

    public float SafeRatio1(float Numerator, float Divisor) {
        float Result = SafeRatioN(Numerator, Divisor, 1.0f);

        return(Result);
    }

    public Vector3f GetBarycentric(Rectangle A, Vector3f P) {
        Vector3f Result = new Vector3f();

        Result.x = SafeRatio0(P.x - A.getMin().x, A.getMax().x - A.getMin().x);
        Result.y = SafeRatio0(P.y - A.getMin().y, A.getMax().y - A.getMin().y);
        Result.z = SafeRatio0(P.z - A.getMin().z, A.getMax().z - A.getMin().z);

        return(Result);
    }

    public Vector2f GetBarycentric(Rectangle A, Vector2f P) {
        Vector2f Result = new Vector2f();

        Result.x = SafeRatio0(P.x - A.getMin().x, A.getMax().x - A.getMin().x);
        Result.y = SafeRatio0(P.y - A.getMin().y, A.getMax().y - A.getMin().y);

        return(Result);
    }

    public static Vector3f Hadamard(Vector3f A, Vector3f B) {
        Vector3f Result = new Vector3f(A.x * B.x, A.y * B.y, A.z * B.z);
        return Result;
    }
}
