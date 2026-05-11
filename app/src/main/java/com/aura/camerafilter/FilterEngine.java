package com.aura.camerafilter;

import android.graphics.Bitmap;

import com.aura.camerafilter.filters.BaseFilter;
import com.aura.camerafilter.filters.CyberpunkFilter;
import com.aura.camerafilter.filters.DreamyFilter;
import com.aura.camerafilter.filters.DuotoneFilter;
import com.aura.camerafilter.filters.GlitchFilter;
import com.aura.camerafilter.filters.HologramFilter;
import com.aura.camerafilter.filters.InfraredFilter;
import com.aura.camerafilter.filters.NoirFilter;
import com.aura.camerafilter.filters.NormalFilter;
import com.aura.camerafilter.filters.ThermalFilter;
import com.aura.camerafilter.filters.VintageFilter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * FilterEngine — central registry for all available filters.
 *
 * Adding a new filter requires only two changes:
 *   1. Create a new class in the filters/ package that extends {@link BaseFilter}.
 *   2. Register an instance below in {@link #buildRegistry()}.
 *
 * Everything else (UI strip, adapter, capture) picks it up automatically.
 */
public final class FilterEngine {

    // ── Filter IDs ────────────────────────────────────────────────────────────
    public static final int FILTER_NONE      = 0;
    public static final int FILTER_NOIR      = 1;
    public static final int FILTER_DUOTONE   = 2;
    public static final int FILTER_GLITCH    = 3;
    public static final int FILTER_INFRARED  = 4;
    public static final int FILTER_CYBERPUNK = 5;
    public static final int FILTER_DREAMY    = 6;
    public static final int FILTER_VINTAGE   = 7;
    public static final int FILTER_THERMAL   = 8;
    public static final int FILTER_HOLOGRAM  = 9;

    // ── Singleton registry ────────────────────────────────────────────────────
    private static final Map<Integer, BaseFilter> REGISTRY = buildRegistry();

    private FilterEngine() {}

    /** Returns all registered filters in insertion order (used by the UI strip). */
    public static List<FilterItem> getAllFilterItems() {
        List<FilterItem> items = new ArrayList<>();
        for (Map.Entry<Integer, BaseFilter> entry : REGISTRY.entrySet()) {
            items.add(new FilterItem(entry.getKey(), entry.getValue().getName()));
        }
        return items;
    }

    /**
     * Apply the filter identified by {@code filterId} to {@code source}.
     * Falls back to {@link NormalFilter} for unknown IDs.
     */
    public static Bitmap applyFilter(Bitmap source, int filterId) {
        if (source == null) return null;
        BaseFilter filter = REGISTRY.getOrDefault(filterId, REGISTRY.get(FILTER_NONE));
        return filter.apply(source);
    }

    // ── Registry definition ───────────────────────────────────────────────────

    private static Map<Integer, BaseFilter> buildRegistry() {
        // LinkedHashMap preserves insertion order for the UI strip
        Map<Integer, BaseFilter> map = new LinkedHashMap<>();
        map.put(FILTER_NONE,      new NormalFilter());
        map.put(FILTER_NOIR,      new NoirFilter());
        map.put(FILTER_DUOTONE,   new DuotoneFilter());
        map.put(FILTER_GLITCH,    new GlitchFilter());
        map.put(FILTER_INFRARED,  new InfraredFilter());
        map.put(FILTER_CYBERPUNK, new CyberpunkFilter());
        map.put(FILTER_DREAMY,    new DreamyFilter());
        map.put(FILTER_VINTAGE,   new VintageFilter());
        map.put(FILTER_THERMAL,   new ThermalFilter());
        map.put(FILTER_HOLOGRAM,  new HologramFilter());
        return map;
    }
}
