package com.example.mechanix.rpg;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.network.ActionPacket;
import com.example.mechanix.network.ModNetwork;
import com.example.mechanix.network.OpenScreenPacket;
import com.example.mechanix.network.SyncPacket;
import com.example.mechanix.registry.ModItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

/** Серверная логика RPG: опыт, действия из меню, касты. */
public final class Rpg {
    public static final String SPIRIT_KEY = "mechanix_spirit_until";
    public static final String MARK_KEY = "mechanix_mark_until";

    private Rpg() {}

    public static void sync(ServerPlayer p) {
        ModNetwork.sendTo(p, new SyncPacket(RpgData.of(p).tag().copy()));
    }

    public static void refresh(ServerPlayer p) {
        StatApplier.apply(p);
        sync(p);
    }

    // ---------------- опыт ----------------
    public static void addXp(ServerPlayer p, int amount) {
        RpgData d = RpgData.of(p);
        Race r = d.race();
        if (r == null || amount <= 0) return;
        int max = MechanixConfig.MAX_LEVEL.get();
        if (d.level() >= max) return;

        double mul = (MechanixConfig.XP_PERCENT.get() + r.xpPct) / 100.0D;
        int gain = Math.max(1, (int) Math.round(amount * mul));
        d.setXp(d.xp() + gain);

        boolean leveled = false;
        while (d.level() < max && d.xp() >= RpgData.xpNeeded(d.level())) {
            d.setXp(d.xp() - RpgData.xpNeeded(d.level()));
            d.setLevel(d.level() + 1);
            d.setStatPoints(d.statPoints() + MechanixConfig.STAT_POINTS.get());
            d.setSkillPoints(d.skillPoints() + 1);
            leveled = true;
        }
        if (d.level() >= max) d.setXp(0);

        if (leveled) {
            StatApplier.apply(p);
            p.setHealth(p.getMaxHealth());
            d.setMana(d.maxMana());
            p.sendSystemMessage(Component.translatable("msg.mechanix.levelup", d.level()));
            p.level().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            ((ServerLevel) p.level()).sendParticles(ParticleTypes.TOTEM_OF_UNDYING,
                    p.getX(), p.getY() + 1.0D, p.getZ(), 40, 0.5D, 0.8D, 0.5D, 0.3D);
        }
        sync(p);
    }

    // ---------------- действия из меню ----------------
    public static void handleAction(ServerPlayer p, int type, int a, String s) {
        RpgData d = RpgData.of(p);

        switch (type) {
            case ActionPacket.RACE -> {
                if (d.hasRace()) return;
                Race[] races = Race.values();
                if (a < 0 || a >= races.length) return;
                d.setRace(races[a]);
                d.setMana(d.maxMana());
                if (!d.guideGiven()) {
                    d.setGuideGiven(true);
                    ItemStack book = new ItemStack(ModItems.RPG_GUIDE.get());
                    if (!p.getInventory().add(book)) p.drop(book, false);
                }
                p.sendSystemMessage(Component.translatable("msg.mechanix.race_chosen",
                        Component.translatable("race.mechanix." + races[a].id)));
                StatApplier.apply(p);
                p.setHealth(p.getMaxHealth());
                sync(p);
            }
            case ActionPacket.STAT -> {
                Stat st = Stat.byIndex(a);
                if (st == null || d.statPoints() <= 0) return;
                d.setAllocated(st, d.allocated(st) + 1);
                d.setStatPoints(d.statPoints() - 1);
                refresh(p);
            }
            case ActionPacket.CLASS -> {
                RpgClass[] cs = RpgClass.values();
                if (a < 0 || a >= cs.length || d.rpgClass() != null || d.level() < 3) return;
                d.setClass(cs[a]);
                refresh(p);
            }
            case ActionPacket.LEARN -> {
                Spell sp = Spell.byId(s);
                if (sp == null || d.knows(sp) || d.skillPoints() <= 0) return;
                if (d.rpgClass() != sp.cls || d.level() < sp.level) return;
                d.learn(sp);
                d.setSkillPoints(d.skillPoints() - 1);
                int free = d.firstFreeSlot();
                if (free >= 0) d.setSlot(free, sp);
                sync(p);
            }
            case ActionPacket.BIND -> {
                Spell sp = Spell.byId(s);
                if (a < 0 || a > 3 || sp == null || !d.knows(sp)) return;
                d.setSlot(a, sp);
                sync(p);
            }
            default -> { }
        }
    }

    // ---------------- касты ----------------
    public static void cast(ServerPlayer p, int slot) {
        RpgData d = RpgData.of(p);
        Race race = d.race();
        if (race == null) {
            ModNetwork.sendTo(p, new OpenScreenPacket(0));
            return;
        }
        long now = p.level().getGameTime();

        if (slot == 4) {
            if (now < d.abilityReady()) {
                p.displayClientMessage(Component.translatable("msg.mechanix.cooldown",
                        (d.abilityReady() - now + 19) / 20), true);
                return;
            }
            if (RaceAbilities.cast(p, d, race)) {
                d.setAbilityReady(now + race.cooldownSec * 20L);
                sync(p);
            }
            return;
        }

        if (slot < 0 || slot > 3) return;
        Spell sp = d.slot(slot);
        if (sp == null) {
            SpellUtil.msg(p, "msg.mechanix.empty_slot");
            return;
        }
        long ready = d.cooldownReady(sp.id);
        if (now < ready) {
            p.displayClientMessage(Component.translatable("msg.mechanix.cooldown", (ready - now + 19) / 20), true);
            return;
        }
        if (d.mana() < sp.mana) {
            SpellUtil.msg(p, "msg.mechanix.no_mana");
            return;
        }
        if (sp.cast.cast(p, d)) {
            d.setMana(d.mana() - sp.mana);
            d.setCooldown(sp.id, now + sp.cooldown);
            sync(p);
        }
    }

    // ---------------- свитки ----------------
    public static boolean rebirth(ServerPlayer p) {
        RpgData d = RpgData.of(p);
        if (!MechanixConfig.ALLOW_REBIRTH.get() || !d.hasRace()) return false;
        d.setRace(null);
        StatApplier.apply(p);
        sync(p);
        ModNetwork.sendTo(p, new OpenScreenPacket(0));
        return true;
    }

    public static boolean resetStats(ServerPlayer p) {
        RpgData d = RpgData.of(p);
        if (!d.hasRace()) return false;
        d.setStatPoints(d.statPoints() + d.allocatedSum());
        for (Stat st : Stat.values()) d.setAllocated(st, 0);
        d.setSkillPoints(d.skillPoints() + d.knownCount());
        d.resetSpells();
        d.setClass(null);
        refresh(p);
        return true;
    }
}
