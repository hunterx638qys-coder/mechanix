package com.example.mechanix.rpg;

import java.util.Locale;

public enum Stat {
    STR, AGI, INT, VIT;

    public String key() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Stat byIndex(int i) {
        Stat[] v = values();
        return i >= 0 && i < v.length ? v[i] : null;
    }
}
