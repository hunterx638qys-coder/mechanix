package com.example.mechanix.rpg;

import com.example.mechanix.MechanixConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Реализация всех заклинаний. Возвращает true, если заклинание сработало (тратится мана и кулдаун). */
public final class SpellCasting {
    private SpellCasting() {}

    private static double pw(RpgData d, Stat s) {
        return (1.0D + d.total(s) * 0.05D + d.level() * 0.02D) * MechanixConfig.SPELL_DAMAGE.get();
    }

    private static boolean noTarget(ServerPlayer p) {
        SpellUtil.msg(p, "msg.mechanix.no_target");
        return false;
    }

    // ================= ВОИН =================
    public static boolean leapStrike(ServerPlayer p, RpgData d) {
        Vec3 dir = p.getLookAngle();
        p.setDeltaMovement(dir.x * 1.6D, 0.35D, dir.z * 1.6D);
        p.hurtMarked = true;
        p.fallDistance = 0.0F;
        d.setDash(p.level().getGameTime() + 12L, 7.0D * pw(d, Stat.STR));
        SpellUtil.sound(p, SoundEvents.PLAYER_ATTACK_STRONG);
        return true;
    }

    public static boolean shatter(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        for (LivingEntity e : SpellUtil.foes(p, p.position(), 5.0D)) {
            SpellUtil.hit(p, e, (float) (8.0D * pw(d, Stat.STR)));
            SpellUtil.knock(e, p.position(), 0.7D, 0.6D);
        }
        SpellUtil.particles(level, ParticleTypes.EXPLOSION, p.position().add(0, 0.2, 0), 4, 2.0D, 0.0D);
        SpellUtil.particles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, p.position().add(0, 0.2, 0), 20, 2.0D, 0.02D);
        SpellUtil.sound(p, SoundEvents.GENERIC_EXPLODE, 0.7F, 1.2F);
        return true;
    }

    public static boolean battleCry(ServerPlayer p, RpgData d) {
        for (Player a : SpellUtil.allies(p, 8.0D)) {
            SpellUtil.fx(a, MobEffects.DAMAGE_BOOST, 200, 1);
            SpellUtil.fx(a, MobEffects.DAMAGE_RESISTANCE, 200, 0);
        }
        SpellUtil.sound(p, SoundEvents.RAVAGER_ROAR, 0.8F, 1.2F);
        return true;
    }

    public static boolean ironWill(ServerPlayer p, RpgData d) {
        SpellUtil.fx(p, MobEffects.DAMAGE_RESISTANCE, 100, 3);
        SpellUtil.fx(p, MobEffects.ABSORPTION, 200, 1);
        SpellUtil.sound(p, SoundEvents.ANVIL_PLACE);
        return true;
    }

    public static boolean bladeStorm(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        for (LivingEntity e : SpellUtil.foes(p, p.position(), 4.0D)) {
            SpellUtil.hit(p, e, (float) (14.0D * pw(d, Stat.STR)));
            SpellUtil.knock(e, p.position(), 0.6D, 0.3D);
        }
        SpellUtil.particles(level, ParticleTypes.SWEEP_ATTACK, p.position().add(0, 1.0, 0), 8, 2.0D, 0.0D);
        SpellUtil.sound(p, SoundEvents.PLAYER_ATTACK_SWEEP);
        return true;
    }

    // ================= МАГ =================
    public static boolean fireball(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 30.0D);
        Vec3 from = p.getEyePosition().add(p.getLookAngle().scale(0.8D));
        SpellUtil.trail(level, ParticleTypes.FLAME, from, r.pos(), 24);
        for (LivingEntity e : SpellUtil.foes(p, r.pos(), 3.5D)) {
            SpellUtil.hit(p, e, (float) (12.0D * pw(d, Stat.INT)));
            e.setSecondsOnFire(5);
        }
        SpellUtil.particles(level, ParticleTypes.EXPLOSION, r.pos(), 3, 0.8D, 0.0D);
        SpellUtil.particles(level, ParticleTypes.FLAME, r.pos(), 30, 1.2D, 0.05D);
        SpellUtil.sound(p, SoundEvents.BLAZE_SHOOT);
        return true;
    }

    public static boolean iceSpike(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 25.0D);
        Vec3 from = p.getEyePosition().add(p.getLookAngle().scale(0.8D));
        SpellUtil.trail(level, ParticleTypes.SNOWFLAKE, from, r.pos(), 24);
        for (LivingEntity e : SpellUtil.foes(p, r.pos(), 2.5D)) {
            SpellUtil.hit(p, e, (float) (8.0D * pw(d, Stat.INT)));
            SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 100, 3);
            e.setTicksFrozen(200);
        }
        SpellUtil.particles(level, ParticleTypes.SNOWFLAKE, r.pos(), 30, 1.0D, 0.05D);
        SpellUtil.sound(p, SoundEvents.GLASS_BREAK, 1.0F, 1.4F);
        return true;
    }

    public static boolean chainLightning(ServerPlayer p, RpgData d) {
        SpellUtil.Ray r = SpellUtil.ray(p, 30.0D);
        LivingEntity cur = r.entity();
        if (cur == null || !SpellUtil.isFoe(p, cur)) return noTarget(p);
        ServerLevel level = SpellUtil.lvl(p);
        Set<LivingEntity> hit = new HashSet<>();
        Vec3 from = p.getEyePosition();
        double dmg = 10.0D * pw(d, Stat.INT);
        for (int i = 0; i < 4 && cur != null; i++) {
            hit.add(cur);
            SpellUtil.hit(p, cur, (float) dmg);
            Vec3 to = cur.position().add(0.0D, cur.getBbHeight() / 2.0D, 0.0D);
            SpellUtil.trail(level, ParticleTypes.ELECTRIC_SPARK, from, to, 12);
            from = to;
            dmg *= 0.8D;
            LivingEntity next = null;
            double best = 36.0D;
            for (LivingEntity e : SpellUtil.foes(p, cur.position(), 6.0D)) {
                if (hit.contains(e)) continue;
                double dist = e.distanceToSqr(cur);
                if (dist < best) {
                    best = dist;
                    next = e;
                }
            }
            cur = next;
        }
        SpellUtil.sound(p, SoundEvents.LIGHTNING_BOLT_IMPACT, 0.6F, 1.5F);
        return true;
    }

    public static boolean manaShield(ServerPlayer p, RpgData d) {
        SpellUtil.fx(p, MobEffects.ABSORPTION, 400, 2);
        SpellUtil.fx(p, MobEffects.DAMAGE_RESISTANCE, 100, 0);
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.ENCHANT, p.position().add(0, 1.0, 0), 40, 0.6D, 0.5D);
        SpellUtil.sound(p, SoundEvents.BEACON_ACTIVATE);
        return true;
    }

    public static boolean meteor(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 35.0D);
        for (int i = 0; i < 3; i++) {
            LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
            if (bolt != null) {
                bolt.moveTo(r.pos().x + (i - 1) * 1.5D, r.pos().y, r.pos().z + (i == 1 ? 1.0D : -1.0D));
                bolt.setVisualOnly(true);
                level.addFreshEntity(bolt);
            }
        }
        for (LivingEntity e : SpellUtil.foes(p, r.pos(), 6.0D)) {
            SpellUtil.hit(p, e, (float) (35.0D * pw(d, Stat.INT)));
            e.setSecondsOnFire(8);
            SpellUtil.knock(e, r.pos(), 0.8D, 0.6D);
        }
        SpellUtil.particles(level, ParticleTypes.EXPLOSION_EMITTER, r.pos(), 1, 0.0D, 0.0D);
        SpellUtil.particles(level, ParticleTypes.LAVA, r.pos(), 40, 2.0D, 0.1D);
        return true;
    }

    // ================= ОХОТНИК =================
    public static boolean preciseShot(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 50.0D);
        Vec3 from = p.getEyePosition().add(p.getLookAngle().scale(0.8D));
        SpellUtil.trail(level, ParticleTypes.CRIT, from, r.pos(), 30);
        if (r.entity() != null && SpellUtil.isFoe(p, r.entity())) {
            SpellUtil.hit(p, r.entity(), (float) (10.0D * pw(d, Stat.AGI)));
        }
        SpellUtil.sound(p, SoundEvents.ARROW_SHOOT, 1.0F, 1.3F);
        return true;
    }

    public static boolean netTrap(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 14.0D);
        for (LivingEntity e : SpellUtil.foes(p, r.pos(), 4.0D)) {
            SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 100, 5);
            SpellUtil.fx(e, MobEffects.WEAKNESS, 100, 0);
        }
        SpellUtil.particles(level, ParticleTypes.CRIT, r.pos().add(0, 0.5, 0), 40, 2.0D, 0.05D);
        SpellUtil.sound(p, SoundEvents.COBWEB_PLACE);
        return true;
    }

    public static boolean summonSpirit(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        long until = level.getGameTime() + 900L;
        for (int i = 0; i < 2; i++) {
            Wolf w = EntityType.WOLF.create(level);
            if (w == null) continue;
            w.moveTo(p.getX() + (i == 0 ? 1.5D : -1.5D), p.getY(), p.getZ() + 1.0D, p.getYRot(), 0.0F);
            w.tame(p);
            w.setOrderedToSit(false);
            w.getAttribute(Attributes.MAX_HEALTH).setBaseValue(30.0D);
            w.setHealth(30.0F);
            w.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(4.0D + pw(d, Stat.AGI) * 2.0D);
            w.setCustomName(Component.translatable("entity.mechanix.spirit_wolf"));
            w.addEffect(new MobEffectInstance(MobEffects.GLOWING, 900, 0, false, false));
            w.getPersistentData().putLong(Rpg.SPIRIT_KEY, until);
            level.addFreshEntity(w);
        }
        SpellUtil.particles(level, ParticleTypes.SOUL, p.position().add(0, 1.0, 0), 20, 0.8D, 0.05D);
        SpellUtil.sound(p, SoundEvents.WOLF_HOWL);
        return true;
    }

    public static boolean beastRun(ServerPlayer p, RpgData d) {
        SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 160, 3);
        SpellUtil.fx(p, MobEffects.JUMP, 160, 2);
        SpellUtil.sound(p, SoundEvents.WOLF_AMBIENT, 1.0F, 0.8F);
        return true;
    }

    public static boolean arrowRain(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        SpellUtil.Ray r = SpellUtil.ray(p, 30.0D);
        for (LivingEntity e : SpellUtil.foes(p, r.pos(), 6.0D)) {
            SpellUtil.hit(p, e, (float) (12.0D * pw(d, Stat.AGI)));
            SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 60, 1);
        }
        for (int i = 0; i < 6; i++) {
            SpellUtil.trail(level, ParticleTypes.CRIT, r.pos().add((i - 3) * 1.0D, 8.0D, (i % 2) * 2.0D - 1.0D),
                    r.pos().add((i - 3) * 1.0D, 0.2D, (i % 2) * 2.0D - 1.0D), 10);
        }
        SpellUtil.sound(p, SoundEvents.ARROW_HIT, 1.0F, 0.8F);
        return true;
    }

    // ================= УБИЙЦА =================
    public static boolean shadowStep(ServerPlayer p, RpgData d) {
        LivingEntity t = SpellUtil.foeInFront(p, 14.0D);
        if (t == null || !SpellUtil.teleportBehind(p, t)) return noTarget(p);
        SpellUtil.hit(p, t, (float) (6.0D * pw(d, Stat.AGI)));
        SpellUtil.fx(p, MobEffects.INVISIBILITY, 40, 0);
        SpellUtil.sound(p, SoundEvents.ENDERMAN_TELEPORT, 1.0F, 1.4F);
        return true;
    }

    public static boolean backstab(ServerPlayer p, RpgData d) {
        LivingEntity t = SpellUtil.foeInFront(p, 3.5D);
        if (t == null) return noTarget(p);
        double dmg = 10.0D * pw(d, Stat.AGI);
        Vec3 tl = t.getLookAngle();
        Vec3 pl = p.getLookAngle();
        if (tl.x * pl.x + tl.z * pl.z > 0.2D) {
            dmg *= 2.0D;
            SpellUtil.msg(p, "msg.mechanix.backstab");
        }
        SpellUtil.hit(p, t, (float) dmg);
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.CRIT, t.position().add(0, 1.0, 0), 25, 0.4D, 0.3D);
        SpellUtil.sound(p, SoundEvents.PLAYER_ATTACK_CRIT);
        return true;
    }

    public static boolean smokeBomb(ServerPlayer p, RpgData d) {
        for (LivingEntity e : SpellUtil.foes(p, p.position(), 6.0D)) {
            SpellUtil.fx(e, MobEffects.BLINDNESS, 100, 0);
            SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 100, 0);
            if (e instanceof Mob m) m.setTarget(null);
        }
        SpellUtil.fx(p, MobEffects.INVISIBILITY, 100, 0);
        SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 100, 1);
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.LARGE_SMOKE, p.position().add(0, 1.0, 0), 60, 1.5D, 0.02D);
        SpellUtil.sound(p, SoundEvents.FIRECHARGE_USE);
        return true;
    }

    public static boolean venomStrike(ServerPlayer p, RpgData d) {
        LivingEntity t = SpellUtil.foeInFront(p, 4.0D);
        if (t == null) return noTarget(p);
        SpellUtil.hit(p, t, (float) (6.0D * pw(d, Stat.AGI)));
        SpellUtil.fx(t, MobEffects.POISON, 200, 2);
        SpellUtil.fx(t, MobEffects.WITHER, 100, 0);
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.SNEEZE, t.position().add(0, 1.0, 0), 20, 0.4D, 0.05D);
        SpellUtil.sound(p, SoundEvents.SPIDER_AMBIENT, 1.0F, 0.7F);
        return true;
    }

    public static boolean deathMark(ServerPlayer p, RpgData d) {
        SpellUtil.Ray r = SpellUtil.ray(p, 30.0D);
        LivingEntity t = r.entity();
        if (t == null || !SpellUtil.isFoe(p, t)) return noTarget(p);
        t.getPersistentData().putLong(Rpg.MARK_KEY, p.level().getGameTime() + 400L);
        SpellUtil.fx(t, MobEffects.GLOWING, 400, 0);
        SpellUtil.hit(p, t, (float) (10.0D * pw(d, Stat.AGI)));
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.SOUL, t.position().add(0, 1.0, 0), 30, 0.5D, 0.05D);
        SpellUtil.sound(p, SoundEvents.WITHER_SPAWN, 0.5F, 1.6F);
        return true;
    }

    // ================= ЦЕЛИТЕЛЬ =================
    public static boolean heal(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        float amount = (float) (6.0D * pw(d, Stat.INT));
        for (Player a : SpellUtil.allies(p, 8.0D)) {
            a.heal(amount);
            SpellUtil.particles(level, ParticleTypes.HEART, a.position().add(0, 1.5, 0), 5, 0.4D, 0.02D);
        }
        SpellUtil.sound(p, SoundEvents.PLAYER_LEVELUP, 0.6F, 1.5F);
        return true;
    }

    public static boolean cleanse(ServerPlayer p, RpgData d) {
        for (Player a : SpellUtil.allies(p, 8.0D)) {
            List<MobEffectInstance> list = new ArrayList<>(a.getActiveEffects());
            for (MobEffectInstance inst : list) {
                if (inst.getEffect().getCategory() == MobEffectCategory.HARMFUL) {
                    a.removeEffect(inst.getEffect());
                }
            }
            a.heal(2.0F);
            a.clearFire();
        }
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.END_ROD, p.position().add(0, 1.0, 0), 30, 1.0D, 0.05D);
        SpellUtil.sound(p, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.2F);
        return true;
    }

    public static boolean holyLight(ServerPlayer p, RpgData d) {
        ServerLevel level = SpellUtil.lvl(p);
        double power = pw(d, Stat.INT);
        for (LivingEntity e : SpellUtil.foes(p, p.position(), 8.0D)) {
            if (e.getMobType() == MobType.UNDEAD) {
                SpellUtil.hit(p, e, (float) (16.0D * power));
                e.setSecondsOnFire(6);
            } else {
                SpellUtil.hit(p, e, (float) (5.0D * power));
            }
        }
        for (Player a : SpellUtil.allies(p, 8.0D)) a.heal((float) (4.0D * power));
        SpellUtil.particles(level, ParticleTypes.END_ROD, p.position().add(0, 1.0, 0), 80, 3.0D, 0.1D);
        SpellUtil.sound(p, SoundEvents.BEACON_POWER_SELECT, 1.0F, 1.4F);
        return true;
    }

    public static boolean blessing(ServerPlayer p, RpgData d) {
        for (Player a : SpellUtil.allies(p, 10.0D)) {
            SpellUtil.fx(a, MobEffects.REGENERATION, 200, 1);
            SpellUtil.fx(a, MobEffects.DAMAGE_BOOST, 600, 0);
            SpellUtil.fx(a, MobEffects.DAMAGE_RESISTANCE, 600, 0);
        }
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.HAPPY_VILLAGER, p.position().add(0, 1.0, 0), 40, 2.0D, 0.05D);
        SpellUtil.sound(p, SoundEvents.PLAYER_LEVELUP, 0.7F, 1.0F);
        return true;
    }

    public static boolean sanctuary(ServerPlayer p, RpgData d) {
        for (Player a : SpellUtil.allies(p, 8.0D)) {
            SpellUtil.fx(a, MobEffects.DAMAGE_RESISTANCE, 160, 2);
            SpellUtil.fx(a, MobEffects.REGENERATION, 160, 2);
            SpellUtil.fx(a, MobEffects.ABSORPTION, 160, 1);
        }
        SpellUtil.particles(SpellUtil.lvl(p), ParticleTypes.TOTEM_OF_UNDYING, p.position().add(0, 1.0, 0), 60, 2.5D, 0.1D);
        SpellUtil.sound(p, SoundEvents.TOTEM_USE, 0.7F, 1.0F);
        return true;
    }
}
