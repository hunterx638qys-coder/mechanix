package com.example.mechanix.rpg;

import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;

public enum Spell {
    // Воин
    LEAP_STRIKE("leap_strike", RpgClass.WARRIOR, 1, 10, 160, SpellCasting::leapStrike),
    SHATTER("shatter", RpgClass.WARRIOR, 5, 18, 240, SpellCasting::shatter),
    BATTLE_CRY("battle_cry", RpgClass.WARRIOR, 10, 20, 600, SpellCasting::battleCry),
    IRON_WILL("iron_will", RpgClass.WARRIOR, 15, 25, 900, SpellCasting::ironWill),
    BLADE_STORM("blade_storm", RpgClass.WARRIOR, 20, 30, 400, SpellCasting::bladeStorm),
    // Маг
    FIREBALL("fireball", RpgClass.MAGE, 1, 12, 40, SpellCasting::fireball),
    ICE_SPIKE("ice_spike", RpgClass.MAGE, 3, 10, 60, SpellCasting::iceSpike),
    CHAIN_LIGHTNING("chain_lightning", RpgClass.MAGE, 8, 20, 120, SpellCasting::chainLightning),
    MANA_SHIELD("mana_shield", RpgClass.MAGE, 12, 25, 600, SpellCasting::manaShield),
    METEOR("meteor", RpgClass.MAGE, 25, 60, 1200, SpellCasting::meteor),
    // Охотник
    PRECISE_SHOT("precise_shot", RpgClass.HUNTER, 1, 8, 30, SpellCasting::preciseShot),
    NET_TRAP("net_trap", RpgClass.HUNTER, 5, 15, 300, SpellCasting::netTrap),
    SUMMON_SPIRIT("summon_spirit", RpgClass.HUNTER, 10, 35, 1200, SpellCasting::summonSpirit),
    BEAST_RUN("beast_run", RpgClass.HUNTER, 15, 20, 600, SpellCasting::beastRun),
    ARROW_RAIN("arrow_rain", RpgClass.HUNTER, 20, 40, 500, SpellCasting::arrowRain),
    // Убийца
    SHADOW_STEP("shadow_step", RpgClass.ASSASSIN, 1, 12, 100, SpellCasting::shadowStep),
    BACKSTAB("backstab", RpgClass.ASSASSIN, 5, 15, 100, SpellCasting::backstab),
    SMOKE_BOMB("smoke_bomb", RpgClass.ASSASSIN, 10, 20, 500, SpellCasting::smokeBomb),
    VENOM_STRIKE("venom_strike", RpgClass.ASSASSIN, 15, 18, 200, SpellCasting::venomStrike),
    DEATH_MARK("death_mark", RpgClass.ASSASSIN, 20, 35, 700, SpellCasting::deathMark),
    // Целитель
    HEAL("heal", RpgClass.HEALER, 1, 15, 100, SpellCasting::heal),
    CLEANSE("cleanse", RpgClass.HEALER, 5, 12, 200, SpellCasting::cleanse),
    HOLY_LIGHT("holy_light", RpgClass.HEALER, 10, 30, 300, SpellCasting::holyLight),
    BLESSING("blessing", RpgClass.HEALER, 15, 35, 900, SpellCasting::blessing),
    SANCTUARY("sanctuary", RpgClass.HEALER, 20, 45, 1200, SpellCasting::sanctuary);

    public interface Cast {
        boolean cast(ServerPlayer player, RpgData data);
    }

    public final String id;
    public final RpgClass cls;
    public final int level;
    public final int mana;
    public final int cooldown;
    public final Cast cast;

    Spell(String id, RpgClass cls, int level, int mana, int cooldown, Cast cast) {
        this.id = id;
        this.cls = cls;
        this.level = level;
        this.mana = mana;
        this.cooldown = cooldown;
        this.cast = cast;
    }

    public static Spell byId(String id) {
        for (Spell s : values()) {
            if (s.id.equals(id)) return s;
        }
        return null;
    }

    public static List<Spell> forClass(RpgClass c) {
        List<Spell> list = new ArrayList<>();
        for (Spell s : values()) {
            if (s.cls == c) list.add(s);
        }
        return list;
    }
}
