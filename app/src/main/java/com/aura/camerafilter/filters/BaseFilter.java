package com.aura.camerafilter.filters;

import android.graphics.Bitmap;

/**
 * Contract for all camera filters.
 *
 * Every subclass implements {@link #apply(Bitmap)} to transform a source
 * bitmap and return a new, filtered bitmap. The source bitmap is never
 * mutated; implementations must create a copy internally.
 */
public abstract class BaseFilter {

    /** Human-readable filter name shown in the UI. */
    public abstract String getName();

    /**
     * Apply the filter to {@code src} and return the result.
     * The returned bitmap is always a fresh allocation; {@code src} is untouched.
     */
    public abstract Bitmap apply(Bitmap src);

    // ── Convenience pixel helpers (delegates to FilterUtils) ─────────────────

    protected static int clamp(int v)                   { return FilterUtils.clamp(v); }
    protected static int clamp(int v, int lo, int hi)   { return FilterUtils.clamp(v, lo, hi); }
    protected static int luminance(int r, int g, int b) { return FilterUtils.luminance(r, g, b); }
    protected static int sCurve(int v)                  { return FilterUtils.sCurve(v); }
    protected static int[] hsvToRgb(float h, float s, float v) { return FilterUtils.hsvToRgb(h, s, v); }
    protected static void applyVignette(android.graphics.Bitmap bmp, float str, int color) {
        FilterUtils.applyVignette(bmp, str, color);
    }
}
