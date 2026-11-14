package com.example.mechanix;

import net.minecraftforge.common.ForgeConfigSpec;

/** config/mechanix-common.toml */
public class MechanixConfig {
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue MAX_LEVEL;
    public static final ForgeConfigSpec.IntValue STAT_POINTS;
    public static final ForgeConfigSpec.IntValue XP_PERCENT;
    public static final ForgeConfigSpec.IntValue MANA_REGEN_PERCENT;
    public static final ForgeConfigSpec.DoubleValue SPELL_DAMAGE;
    public static final ForgeConfigSpec.BooleanValue SHARE_XP;
    public static final ForgeConfigSpec.BooleanValue ALLOW_REBIRTH;
    public static final ForgeConfigSpec.BooleanValue STRUCTURES;
    public static final ForgeConfigSpec.BooleanValue BOSS_SPAWNS;
    public static final ForgeConfigSpec.BooleanValue SHOW_HUD;

    static {
        ForgeConfigSpec.Builder b = new ForgeConfigSpec.Builder();

        b.push("progression");
        MAX_LEVEL = b.comment("Maximum player level").defineInRange("maxLevel", 50, 5, 200);
        STAT_POINTS = b.comment("Stat points per level").defineInRange("statPointsPerLevel", 3, 1, 20);
        XP_PERCENT = b.comment("Experience gain, percent (100 = normal)").defineInRange("xpPercent", 100, 10, 1000);
        SHARE_XP = b.comment("Nearby players get a share of kill experience").define("shareXp", true);
        b.pop();

        b.push("magic");
        MANA_REGEN_PERCENT = b.comment("Mana regeneration, percent").defineInRange("manaRegenPercent", 100, 10, 1000);
        SPELL_DAMAGE = b.comment("Spell damage multiplier").defineInRange("spellDamageMultiplier", 1.0D, 0.1D, 10.0D);
        b.pop();

        b.push("world");
        STRUCTURES = b.comment("Generate towers, camps and ruins").define("structures", true);
        BOSS_SPAWNS = b.comment("Spawn bosses inside structures").define("bossSpawns", true);
        b.pop();

        b.push("misc");
        ALLOW_REBIRTH = b.comment("Allow Rebirth Scroll (race change)").define("allowRebirth", true);
        SHOW_HUD = b.comment("Show RPG HUD").define("showHud", true);
        b.pop();

        SPEC = b.build();
    }
}
