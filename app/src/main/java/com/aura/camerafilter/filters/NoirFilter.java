package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Noir — high-contrast cinematic black & white.
 *
 * Pipeline:
 *   1. BT.601 luminance conversion
 *   2. Cubic S-curve for boosted midtone contrast
 *   3. Radial vignette to push the edges to pure black
 */
public class NoirFilter extends BaseFilter {

    @Override
    public String getName() { return "Noir"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p   = pixels[i];
            int lum = luminance(Color.red(p), Color.green(p), Color.blue(p));
            lum     = sCurve(lum);
            pixels[i] = Color.argb(Color.alpha(p), lum, lum, lum);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        applyVignette(out, 0.7f, 0xFF000000);
        return out;
    }
}
