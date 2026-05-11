package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Infrared — simulates IR film photography.
 *
 * In real infrared photography:
 *   • Vegetation (high IR reflectance) renders bright / white
 *   • Clear sky (low IR reflectance) renders very dark
 *   • Skin tones glow with a creamy warmth
 *
 * Approximation:
 *   1. Swap red & green channels with boosts to simulate IR channel sensitivity
 *   2. Heavily suppress blue (sky darkening)
 *   3. Convert result to luminance with IR-weighted coefficients
 *   4. Apply a slight warm (cream) tint
 *   5. Light vignette
 */
public class InfraredFilter extends BaseFilter {

    @Override
    public String getName() { return "Infrared"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int r = Color.red(p), g = Color.green(p), b = Color.blue(p);

            // Simulate IR channel: vegetation (green) reflects IR strongly
            int irR = clamp((int)(g * 1.4f));
            int irG = clamp((int)(r * 0.9f + g * 0.3f));
            int irB = clamp((int)(b * 0.3f));

            // Collapse to luminance with IR weightings
            int lum = clamp((int)(irR * 0.5f + irG * 0.4f + irB * 0.1f));
            lum     = clamp((int)(lum * 1.2f - 10)); // slight brightness + contrast

            // Warm cream tint (r+15, g+8, b-10)
            pixels[i] = Color.argb(Color.alpha(p),
                    clamp(lum + 15),
                    clamp(lum + 8),
                    clamp(lum - 10));
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        applyVignette(out, 0.5f, 0xCC000000);
        return out;
    }
}
