package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Color;

/**
 * Cyberpunk — neon teal highlights, magenta shadows, heavy S-curve contrast.
 *
 * Pipeline:
 *   1. S-curve each channel independently for punchy contrast
 *   2. Split-tone by luminance:
 *        • Bright pixels  → push toward teal  (−R, +G, +B)
 *        • Dark pixels    → push toward magenta (+R, −G, +B)
 *   3. Deep dark vignette with a blue tint at the edges
 */
public class CyberpunkFilter extends BaseFilter {

    @Override
    public String getName() { return "Cyberpunk"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] pixels = new int[w * h];
        src.getPixels(pixels, 0, w, 0, 0, w, h);

        for (int i = 0; i < pixels.length; i++) {
            int p = pixels[i];
            int r = sCurve(Color.red(p));
            int g = sCurve(Color.green(p));
            int b = sCurve(Color.blue(p));

            float lum = luminance(r, g, b) / 255f;

            if (lum > 0.5f) {
                // Highlight → teal
                r = clamp((int)(r * 0.6f));
                g = clamp((int)(g + 20));
                b = clamp((int)(b * 1.3f + 30));
            } else {
                // Shadow → magenta
                r = clamp((int)(r * 1.3f + 20));
                g = clamp((int)(g * 0.5f));
                b = clamp((int)(b * 1.2f + 15));
            }

            pixels[i] = Color.argb(Color.alpha(p), r, g, b);
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(pixels, 0, w, 0, 0, w, h);
        applyVignette(out, 0.6f, 0xAA000020);
        return out;
    }
}
