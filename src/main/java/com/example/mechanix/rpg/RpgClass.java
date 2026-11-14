package com.example.mechanix.rpg;

public enum RpgClass {
    WARRIOR("warrior", 3, 0, 0, 2),
    MAGE("mage", 0, 0, 4, 1),
    HUNTER("hunter", 1, 3, 0, 1),
    ASSASSIN("assassin", 1, 4, 0, 0),
    HEALER("healer", 0, 0, 2, 3);

    public final String id;
    public final int str, agi, intel, vit;

    RpgClass(String id, int str, int agi, int intel, int vit) {
        this.id = id;
        this.str = str;
        this.agi = agi;
        this.intel = intel;
        this.vit = vit;
    }

    public int bonus(Stat s) {
        return switch (s) {
            case STR -> str;
            case AGI -> agi;
            case INT -> intel;
            case VIT -> vit;
        };
    }

    public static RpgClass byId(String id) {
        for (RpgClass c : values()) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }
}
