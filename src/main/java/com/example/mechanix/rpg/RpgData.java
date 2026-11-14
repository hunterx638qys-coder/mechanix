package com.example.mechanix.rpg;

import com.example.mechanix.MechanixConfig;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/** Обёртка над NBT игрока (хранится в persistent data, синхронизируется на клиент). */
public class RpgData {
    public static final String KEY = "mechanix_rpg";

    private final CompoundTag t;

    private RpgData(CompoundTag t) {
        this.t = t;
    }

    public static RpgData of(Player p) {
        CompoundTag root = p.getPersistentData();
        if (!root.contains(KEY, Tag.TAG_COMPOUND)) {
            root.put(KEY, new CompoundTag());
        }
        return new RpgData(root.getCompound(KEY));
    }

    public CompoundTag tag() {
        return t;
    }

    public static int xpNeeded(int level) {
        return 80 + level * 40 + level * level * 4;
    }

    // ---- раса / класс ----
    public boolean hasRace() { return race() != null; }
    public Race race() { return Race.byId(t.getString("race")); }
    public void setRace(Race r) { t.putString("race", r == null ? "" : r.id); }
    public RpgClass rpgClass() { return RpgClass.byId(t.getString("cls")); }
    public void setClass(RpgClass c) { t.putString("cls", c == null ? "" : c.id); }

    // ---- уровень ----
    public int level() { return Math.max(1, t.getInt("level")); }
    public void setLevel(int l) { t.putInt("level", Mth.clamp(l, 1, MechanixConfig.MAX_LEVEL.get())); }
    public int xp() { return t.getInt("xp"); }
    public void setXp(int v) { t.putInt("xp", Math.max(0, v)); }
    public int statPoints() { return t.getInt("statPts"); }
    public void setStatPoints(int v) { t.putInt("statPts", Math.max(0, v)); }
    public int skillPoints() { return t.getInt("skillPts"); }
    public void setSkillPoints(int v) { t.putInt("skillPts", Math.max(0, v)); }

    // ---- характеристики ----
    public int allocated(Stat s) { return t.getInt("a_" + s.key()); }
    public void setAllocated(Stat s, int v) { t.putInt("a_" + s.key(), Math.max(0, v)); }

    public int total(Stat s) {
        int v = allocated(s);
        Race r = race();
        if (r != null) v += r.bonus(s);
        RpgClass c = rpgClass();
        if (c != null) v += c.bonus(s);
        return v;
    }

    public int allocatedSum() {
        int sum = 0;
        for (Stat s : Stat.values()) sum += allocated(s);
        return sum;
    }

    // ---- мана ----
    public double maxMana() {
        Race r = race();
        return 20 + total(Stat.INT) * 4 + level() * 2 + (r == null ? 0 : r.mana);
    }

    public double mana() {
        return t.contains("mana") ? Math.min(t.getDouble("mana"), maxMana()) : maxMana();
    }

    public void setMana(double v) { t.putDouble("mana", Mth.clamp(v, 0.0D, maxMana())); }

    // ---- заклинания ----
    public boolean knows(Spell s) {
        ListTag l = t.getList("spells", Tag.TAG_STRING);
        for (int i = 0; i < l.size(); i++) {
            if (l.getString(i).equals(s.id)) return true;
        }
        return false;
    }

    public void learn(Spell s) {
        if (knows(s)) return;
        ListTag l = t.getList("spells", Tag.TAG_STRING);
        l.add(StringTag.valueOf(s.id));
        t.put("spells", l);
    }

    public int knownCount() { return t.getList("spells", Tag.TAG_STRING).size(); }

    public void resetSpells() {
        t.remove("spells");
        for (int i = 0; i < 4; i++) t.remove("slot" + i);
    }

    public Spell slot(int i) {
        String id = t.getString("slot" + i);
        return id.isEmpty() ? null : Spell.byId(id);
    }

    public void setSlot(int i, Spell s) {
        if (s != null) {
            for (int k = 0; k < 4; k++) {
                if (k != i && s.id.equals(t.getString("slot" + k))) t.remove("slot" + k);
            }
            t.putString("slot" + i, s.id);
        } else {
            t.remove("slot" + i);
        }
    }

    public int firstFreeSlot() {
        for (int i = 0; i < 4; i++) {
            if (slot(i) == null) return i;
        }
        return -1;
    }

    // ---- кулдауны (игровое время готовности) ----
    public long cooldownReady(String id) { return t.getCompound("cd").getLong(id); }

    public void setCooldown(String id, long ready) {
        CompoundTag cd = t.getCompound("cd");
        cd.putLong(id, ready);
        t.put("cd", cd);
    }

    public long abilityReady() { return t.getLong("abReady"); }
    public void setAbilityReady(long v) { t.putLong("abReady", v); }

    // ---- состояния способностей ----
    public long flightUntil() { return t.getLong("flightUntil"); }
    public void setFlightUntil(long v) { t.putLong("flightUntil", v); }
    public long lastBreathReady() { return t.getLong("lastBreath"); }
    public void setLastBreathReady(long v) { t.putLong("lastBreath", v); }
    public long dashUntil() { return t.getLong("dashUntil"); }
    public double dashDamage() { return t.getDouble("dashDmg"); }

    public void setDash(long until, double dmg) {
        t.putLong("dashUntil", until);
        t.putDouble("dashDmg", dmg);
    }

    public boolean guideGiven() { return t.getBoolean("guide"); }
    public void setGuideGiven(boolean v) { t.putBoolean("guide", v); }
}
