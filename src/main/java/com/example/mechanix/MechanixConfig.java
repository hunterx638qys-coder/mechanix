package com.example.mechanix;

import net.minecraftforge.common.ForgeConfigSpec;

/** Файл настроек: config/mechanix-common.toml. Каждую механику можно выключить. */
public class MechanixConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue TREE_FELLING;
    public static final ForgeConfigSpec.IntValue TREE_LIMIT;
    public static final ForgeConfigSpec.BooleanValue LUCKY_MINING;
    public static final ForgeConfigSpec.IntValue LUCKY_CHANCE;
    public static final ForgeConfigSpec.IntValue VEIN_LIMIT;
    public static final ForgeConfigSpec.IntValue BLINK_DISTANCE;
    public static final ForgeConfigSpec.IntValue MAGNET_RADIUS;
    public static final ForgeConfigSpec.BooleanValue VAMPIRISM;
    public static final ForgeConfigSpec.BooleanValue FANG_DROPS;
    public static final ForgeConfigSpec.IntValue FANG_CHANCE;
    public static final ForgeConfigSpec.BooleanValue DEATH_COORDS;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("tree_felling");
        TREE_FELLING = b.comment("Sneak + axe on a log fells the whole tree").define("enabled", true);
        TREE_LIMIT = b.defineInRange("maxLogs", 128, 1, 1024);
        b.pop();

        b.push("lucky_mining");
        LUCKY_MINING = b.comment("Chance of double drops from ores").define("enabled", true);
        LUCKY_CHANCE = b.defineInRange("chancePercent", 8, 0, 100);
        b.pop();

        b.push("items");
        VEIN_LIMIT = b.comment("Max blocks mined by the Vein Pickaxe at once").defineInRange("veinPickaxeLimit", 64, 1, 512);
        BLINK_DISTANCE = b.comment("Blink Staff range in blocks").defineInRange("blinkDistance", 16, 2, 64);
        MAGNET_RADIUS = b.comment("Magnet Amulet radius in blocks").defineInRange("magnetRadius", 6, 1, 24);
        b.pop();

        b.push("vampirism");
        VAMPIRISM = b.comment("Vampirism effect heals the attacker").define("enabled", true);
        FANG_DROPS = b.comment("Monsters can drop Vampire Fangs").define("fangDrops", true);
        FANG_CHANCE = b.defineInRange("fangChancePercent", 5, 0, 100);
        b.pop();

        DEATH_COORDS = b.comment("Show death coordinates in chat").define("deathCoordinates", true);

        SPEC = b.build();
    }
}
