package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Shared static utilities for all filter implementations.
 * No instances — call everything statically.
 */
public final class FilterUtils {

    private FilterUtils() {}

    // ── Pixel arithmetic ──────────────────────────────────────────────────────

    /** Clamp an int to [0, 255]. */
    public static int clamp(int v) {
        return v < 0 ? 0 : (Math.min(v, 255));
    }

    /** Clamp an int to [lo, hi]. */
    public static int clamp(int v, int lo, int hi) {
        return v < lo ? lo : (Math.min(v, hi));
    }

    /** BT.601 luminance from RGB components (returns 0–255). */
    public static int luminance(int r, int g, int b) {
        return (int)(0.299f * r + 0.587f * g + 0.114f * b);
    }

    // ── Tone curves ───────────────────────────────────────────────────────────

    /**
     * Cubic S-curve contrast boost.
     * Maps 0–255 → 0–255 with boosted contrast in the midtones.
     */
    public static int sCurve(int v) {
        float t = v / 255f;
        float s = t < 0.5f
                ? 4 * t * t * t
                : 1 - (float) Math.pow(-2 * t + 2, 3) / 2f;
        return clamp((int)(s * 255));
    }

    // ── Color space conversions ───────────────────────────────────────────────

    /**
     * HSV → RGB. All inputs in [0, 1]; output is int[]{r, g, b} in [0, 255].
     */
    public static int[] hsvToRgb(float h, float s, float v) {
        float r = 0, g = 0, b = 0;
        int   i = (int)(h * 6);
        float f = h * 6 - i;
        float p = v * (1 - s);
        float q = v * (1 - f * s);
        float t = v * (1 - (1 - f) * s);
        switch (i % 6) {
            case 0: r = v; g = t; b = p; break;
            case 1: r = q; g = v; b = p; break;
            case 2: r = p; g = v; b = t; break;
            case 3: r = p; g = q; b = v; break;
            case 4: r = t; g = p; b = v; break;
            case 5: r = v; g = p; b = q; break;
        }
        return new int[]{(int)(r * 255), (int)(g * 255), (int)(b * 255)};
    }

    // ── Canvas helpers ────────────────────────────────────────────────────────

    /**
     * Draws a radial vignette gradient onto an existing mutable bitmap.
     *
     * @param bmp       target bitmap (must be mutable)
     * @param strength  unused placeholder — edgeColor alpha drives strength
     * @param edgeColor ARGB color at the outer edge (centre is transparent)
     */
    public static void applyVignette(Bitmap bmp, float strength, int edgeColor) {
        Canvas canvas = new Canvas(bmp);
        float cx     = bmp.getWidth()  / 2f;
        float cy     = bmp.getHeight() / 2f;
        float radius = Math.max(bmp.getWidth(), bmp.getHeight()) * 0.72f;

        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setShader(new RadialGradient(
                cx, cy, radius,
                Color.argb(0, 0, 0, 0), edgeColor,
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, bmp.getWidth(), bmp.getHeight(), paint);
    }
}
