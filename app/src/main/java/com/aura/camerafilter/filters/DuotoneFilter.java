package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Duotone — maps luminance to a two-color gradient.
 *
 * Shadow color : deep violet  #1A0533
 * Highlight color : amber     #F9C74F
 *
 * Each pixel is converted to luminance, then linearly interpolated
 * between the two palette colours — producing a Spotify-style duotone.
 */
public class DuotoneFilter extends BaseFilter {

    // Shadow (dark) colour components
    private static final int SHADOW_R = 0x1A, SHADOW_G = 0x05, SHADOW_B = 0x33;
    // Highlight (bright) colour components
    private static final int HIGH_R   = 0xF9, HIGH_G   = 0xC7, HIGH_B   = 0x4F;

    @Override
    public String getName() { return "Duotone"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int   p   = pixels[i];
            float lum = luminance(Color.red(p), Color.green(p), Color.blue(p)) / 255f;

            int r = clamp((int)(SHADOW_R + lum * (HIGH_R - SHADOW_R)));
            int g = clamp((int)(SHADOW_G + lum * (HIGH_G - SHADOW_G)));
            int b = clamp((int)(SHADOW_B + lum * (HIGH_B - SHADOW_B)));

            pixels[i] = Color.argb(Color.alpha(p), r, g, b);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        return out;
    }
}
