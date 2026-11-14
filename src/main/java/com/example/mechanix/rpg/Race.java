package com.example.mechanix.rpg;

public enum Race {
    //    id          str agi int vit  hp   spd% dmg  arm mana xp% cd(sec)
    HUMAN("human",      1, 1, 1, 1,   0,   0,  0.0, 0,   0, 15, 120),
    ELF("elf",          0, 2, 3, 0,  -2,   3,  0.0, 0,  20,  0,  45),
    DWARF("dwarf",      2, 0, 0, 3,   6,  -4,  1.0, 2,  -5,  0,  75),
    BEASTKIN("beastkin",1, 3, 0, 1,   0,   8,  1.0, 0,   0,  0,  12),
    VAMPIRE("vampire",  1, 2, 2, 0,  -2,   4,  1.5, 0,  10,  0,  20),
    DEMON("demon",      2, 0, 2, 1,   0,   0,  2.0, 0,  10,  0,  40),
    DRAGONBORN("dragonborn", 3, 0, 0, 2, 4, -2, 1.0, 3,   0,  0,  30),
    ORC("orc",          4, 0, 0, 2,   6,  -2,  2.0, 1, -10,  0,  90),
    UNDEAD("undead",    0, 0, 2, 2,  -4,   0,  0.0, 0,  10,  0,  60),
    ANGEL("angel",      0, 1, 3, 1,   0,   2,  0.0, 0,  30,  0, 120),
    GOBLIN("goblin",    0, 3, 1, 0,  -6,   7,  0.0, 0,   0, 10,  40),
    MERFOLK("merfolk",  0, 1, 2, 2,   0,   0,  0.0, 0,  10,  0,  40),
    KITSUNE("kitsune",  0, 2, 3, 0,  -2,   5,  0.0, 0,  25,  0,  25),
    WEREWOLF("werewolf",3, 2, 0, 0,   2,   2,  1.0, 0,   0,  0,  60);

    public final String id;
    public final int str, agi, intel, vit;
    public final double health;
    public final int speedPct;
    public final double damage;
    public final double armor;
    public final int mana;
    public final int xpPct;
    public final int cooldownSec;

    Race(String id, int str, int agi, int intel, int vit, double health, int speedPct,
         double damage, double armor, int mana, int xpPct, int cooldownSec) {
        this.id = id;
        this.str = str;
        this.agi = agi;
        this.intel = intel;
        this.vit = vit;
        this.health = health;
        this.speedPct = speedPct;
        this.damage = damage;
        this.armor = armor;
        this.mana = mana;
        this.xpPct = xpPct;
        this.cooldownSec = cooldownSec;
    }

    public int bonus(Stat s) {
        return switch (s) {
            case STR -> str;
            case AGI -> agi;
            case INT -> intel;
            case VIT -> vit;
        };
    }

    public static Race byId(String id) {
        for (Race r : values()) {
            if (r.id.equals(id)) return r;
        }
        return null;
    }
}
