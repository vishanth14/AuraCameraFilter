package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Thermal — simulates a thermal / infrared heat camera.
 *
 * A 256-entry look-up table maps luminance to heatmap colours:
 *
 *   0 – 63   : deep blue → cyan       (coldest)
 *   64 – 127 : cyan → green
 *   128 – 191: green → yellow/orange
 *   192 – 255: orange → white         (hottest)
 *
 * Using a LUT avoids repeating the gradient math for every pixel.
 */
public class ThermalFilter extends BaseFilter {

    // Built once on first use, then reused
    private static final int[] LUT = buildLUT();

    @Override
    public String getName() { return "Thermal"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p   = pixels[i];
            int lum = luminance(Color.red(p), Color.green(p), Color.blue(p));
            pixels[i] = LUT[lum];
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }

    // ── LUT construction ──────────────────────────────────────────────────────

    private static int[] buildLUT() {
        int[] lut = new int[256];
        for (int i = 0; i < 256; i++) {
            float t = i / 255f;
            lut[i] = thermalColor(t);
        }
        return lut;
    }

    private static int thermalColor(float t) {
        int r, g, b;
        if (t < 0.25f) {
            float tt = t / 0.25f;
            r = 0;
            g = (int)(tt * 100);
            b = clamp((int)(80 + tt * 175));
        } else if (t < 0.5f) {
            float tt = (t - 0.25f) / 0.25f;
            r = 0;
            g = clamp((int)(100 + tt * 155));
            b = clamp((int)(255 - tt * 255));
        } else if (t < 0.75f) {
            float tt = (t - 0.5f) / 0.25f;
            r = clamp((int)(tt * 255));
            g = clamp((int)(255 - tt * 60));
            b = 0;
        } else {
            float tt = (t - 0.75f) / 0.25f;
            r = 255;
            g = clamp((int)(195 + tt * 60));
            b = clamp((int)(tt * 255));
        }
        return Color.argb(255, r, g, b);
    }
}
