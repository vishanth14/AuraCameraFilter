package com.aura.camerafilter.filters;

import android.graphics.Bitmap;

/** Passthrough — returns an unmodified copy of the source bitmap. */
public class NormalFilter extends BaseFilter {

    @Override
    public String getName() { return "Normal"; }

    @Override
    public Bitmap apply(Bitmap src) {
        return src.copy(Bitmap.Config.ARGB_8888, true);
    }
}
