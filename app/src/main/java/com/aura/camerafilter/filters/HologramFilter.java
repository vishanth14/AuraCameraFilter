package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;

/**
 * Hologram — prismatic rainbow with scanlines and a screen-blend sheen.
 *
 * Pipeline:
 *   1. Desaturate each pixel to luminance
 *   2. Derive a hue from the pixel's diagonal position (x + y×0.4) for banded rainbow
 *   3. Blend desaturated value with the position-derived hue colour
 *   4. Darken every 4th row (scanline simulation)
 *   5. Draw a four-stop multi-colour LinearGradient in SCREEN blend mode on top
 */
public class HologramFilter extends BaseFilter {

    private static final float HUE_BLEND    = 0.45f;
    private static final float SCANLINE_DIM = 0.65f;
    private static final int   SCANLINE_STEP = 4;

    @Override
    public String getName() { return "Hologram"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p   = pixels[i];
            int x   = i % w, y = i / w;
            int lum = luminance(Color.red(p), Color.green(p), Color.blue(p));

            // Position-based rainbow hue (diagonal bands)
            float hue      = ((x + y * 0.4f) % w) / (float) w;
            int[] hueColor = hsvToRgb(hue, 0.85f, 1.0f);

            int r = clamp((int)(lum * (1 - HUE_BLEND) + hueColor[0] * HUE_BLEND));
            int g = clamp((int)(lum * (1 - HUE_BLEND) + hueColor[1] * HUE_BLEND));
            int b = clamp((int)(lum * (1 - HUE_BLEND) + hueColor[2] * HUE_BLEND));

            if (y % SCANLINE_STEP == 0) {
                r = (int)(r * SCANLINE_DIM);
                g = (int)(g * SCANLINE_DIM);
                b = (int)(b * SCANLINE_DIM);
            }

            pixels[i] = Color.argb(Color.alpha(p), r, g, b);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);

        // Prismatic sheen overlay (SCREEN blend — lightens only)
        Canvas canvas = new Canvas(out);
        Paint  sheen  = new Paint(Paint.ANTI_ALIAS_FLAG);
        sheen.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SCREEN));
        sheen.setShader(new LinearGradient(0, 0, w, h,
                new int[]{
                        Color.argb( 0, 255,   0, 255),
                        Color.argb(40,   0, 255, 255),
                        Color.argb( 0, 255, 255,   0),
                        Color.argb(40, 255,   0, 128)
                },
                null, Shader.TileMode.CLAMP));
        canvas.drawRect(0, 0, w, h, sheen);

        return out;
    }
}
