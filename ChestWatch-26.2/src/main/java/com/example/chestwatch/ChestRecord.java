package com.example.chestwatch;

import java.util.HashMap;
import java.util.Map;

public class ChestRecord {
    public final String dimension;
    public final long x, y, z;
    public final Map<String, Integer> items = new HashMap<>();

    public ChestRecord(String dimension, long x, long y, long z) {
        this.dimension = dimension;
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public String id() {
        return dimension + "|" + x + "|" + y + "|" + z;
    }
}
