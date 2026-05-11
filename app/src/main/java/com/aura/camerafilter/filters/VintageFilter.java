package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

import java.util.Random;

/**
 * Vintage — warm film grain with cross-process amber tones.
 *
 * Pipeline:
 *   1. Lift shadows with an amber tint (warm blacks)
 *   2. Suppress blue globally (warm overall temperature)
 *   3. Add per-pixel film grain (seeded for deterministic output)
 *   4. Heavy amber-tinted vignette (film-burn corners)
 */
public class VintageFilter extends BaseFilter {

    private static final long  RNG_SEED   = 7L;
    private static final float GRAIN_MAG  = 22f;

    @Override
    public String getName() { return "Vintage"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        Random rng = new Random(RNG_SEED);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            // Warm shadow lift
            int r = clamp((int)(Color.red(p)   * 0.90f + 20));
            int g = clamp((int)(Color.green(p) * 0.85f + 10));
            int b = clamp((int)(Color.blue(p)  * 0.75f +  5));

            // Suppress blue further for warmer temperature
            b = clamp((int)(b * 0.80f));

            // Film grain (same noise applied to all channels, less on blue)
            int grain = (int)((rng.nextFloat() - 0.5f) * GRAIN_MAG);
            r = clamp(r + grain);
            g = clamp(g + grain);
            b = clamp(b + (int)(grain * 0.6f));

            pixels[i] = Color.argb(Color.alpha(p), r, g, b);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        applyVignette(out, 0.75f, 0xDD200A00); // dark amber corners
        return out;
    }
}
