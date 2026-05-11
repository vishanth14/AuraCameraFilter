package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;

/**
 * Dreamy — soft pastel overexposure with a pink-white bloom.
 *
 * Pipeline:
 *   1. Lift shadows: scale each channel down and add a floor (fades blacks)
 *   2. Partial desaturation with a slight pink/warm bias
 *   3. Radial bloom overlay — white-pink gradient at the centre, fading out
 */
public class DreamyFilter extends BaseFilter {

    private static final float SHADOW_LIFT = 0.85f;
    private static final float DESAT       = 0.35f;

    @Override
    public String getName() { return "Dreamy"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            // Lift shadows (fade blacks to a warm tone)
            int r = (int)(Color.red(p)   * SHADOW_LIFT + 38);
            int g = (int)(Color.green(p) * SHADOW_LIFT + 30);
            int b = (int)(Color.blue(p)  * SHADOW_LIFT + 38);

            // Partial desaturation toward pink-white
            float lum = r * 0.299f + g * 0.587f + b * 0.114f;
            r = clamp((int)(r * (1 - DESAT) + lum * DESAT + 8));
            g = clamp((int)(g * (1 - DESAT) + lum * DESAT - 4));
            b = clamp((int)(b * (1 - DESAT) + lum * DESAT + 4));

            pixels[i] = Color.argb(Color.alpha(p), r, g, b);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);

        // Soft bloom: white-pink radial centre
        Canvas canvas = new Canvas(out);
        Paint  bloom  = new Paint(Paint.ANTI_ALIAS_FLAG);
        float  cx     = w / 2f, cy = h / 2f;
        float  radius = Math.max(w, h) * 0.65f;
        bloom.setShader(new RadialGradient(cx, cy, radius,
                Color.argb(60,  255, 245, 250),
                Color.argb(0,   255, 200, 220),
                Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, bloom);

        return out;
    }
}
