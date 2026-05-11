package com.aura.camerafilter;

/** Lightweight data holder linking a filter ID to its display name. */
public class FilterItem {
    public final int    filterId;
    public final String name;

    public FilterItem(int filterId, String name) {
        this.filterId = filterId;
        this.name     = name;
    }
}
