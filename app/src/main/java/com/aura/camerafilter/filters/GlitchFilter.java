package com.aura.camerafilter.filters;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.Random;

/**
 * Glitch — digital artifact simulation.
 *
 * Pipeline:
 *   1. Red channel is shifted horizontally to the right
 *   2. Blue channel is shifted horizontally to the left
 *   3. Green channel stays in place (each channel read from different x coords)
 *   4. ~4 % of scanlines receive a random horizontal tear offset
 *   5. Subtle dark scanlines drawn over the result every 3 px
 */
public class GlitchFilter extends BaseFilter {

    private static final long RNG_SEED   = 42L;
    private static final float TEAR_PROB = 0.04f;

    @Override
    public String getName() { return "Glitch"; }

    @Override
    public Bitmap apply(Bitmap src) {
        int w = src.getWidth(), h = src.getHeight();
        int[] srcPx = new int[w * h];
        int[] outPx = new int[w * h];
        src.getPixels(srcPx, 0, w, 0, 0, w, h);

        int shiftR =  w / 28;   // red shifts right
        int shiftB = -(w / 40); // blue shifts left

        Random rng = new Random(RNG_SEED);

        for (int y = 0; y < h; y++) {
            int tear = 0;
            if (rng.nextFloat() < TEAR_PROB) {
                tear = (int)((rng.nextFloat() - 0.5f) * w * 0.08f);
            }

            for (int x = 0; x < w; x++) {
                int base = y * w + x;
                int xR   = clamp(x + shiftR + tear, 0, w - 1);
                int xB   = clamp(x + shiftB,        0, w - 1);

                int r = Color.red  (srcPx[y * w + xR]);
                int g = Color.green(srcPx[base]);
                int b = Color.blue (srcPx[y * w + xB]);

                outPx[base] = Color.argb(255, r, g, b);
            }
        }

        Bitmap out = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        out.setPixels(outPx, 0, w, 0, 0, w, h);

        // Scanline overlay
        Canvas canvas   = new Canvas(out);
        Paint  scanPaint = new Paint();
        scanPaint.setColor(Color.argb(30, 0, 0, 0));
        for (int y = 0; y < h; y += 3) {
            canvas.drawLine(0, y, w, y, scanPaint);
        }

        return out;
    }
}
