package com.example.mechanix.rpg;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Уникальные активные (клавиша R) и пассивные способности рас. */
public final class RaceAbilities {
    private RaceAbilities() {}

    public static boolean cast(ServerPlayer p, RpgData d, Race r) {
        ServerLevel level = SpellUtil.lvl(p);
        double power = 1.0D + d.level() * 0.03D;
        switch (r) {
            case HUMAN -> {
                p.heal(p.getMaxHealth() * 0.5F);
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                p.removeEffect(MobEffects.WEAKNESS);
                p.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                p.removeEffect(MobEffects.BLINDNESS);
                SpellUtil.fx(p, MobEffects.REGENERATION, 100, 1);
                SpellUtil.particles(level, ParticleTypes.HEART, p.position().add(0, 1.5, 0), 8, 0.5D, 0.02D);
                SpellUtil.sound(p, SoundEvents.PLAYER_BREATH);
            }
            case ELF -> {
                SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 200, 2);
                SpellUtil.fx(p, MobEffects.JUMP, 200, 2);
                SpellUtil.fx(p, MobEffects.SLOW_FALLING, 200, 0);
                SpellUtil.particles(level, ParticleTypes.HAPPY_VILLAGER, p.position().add(0, 0.5, 0), 25, 0.6D, 0.05D);
                SpellUtil.sound(p, SoundEvents.AMETHYST_BLOCK_CHIME, 1.0F, 1.5F);
            }
            case DWARF -> {
                SpellUtil.fx(p, MobEffects.DAMAGE_RESISTANCE, 300, 1);
                SpellUtil.fx(p, MobEffects.MOVEMENT_SLOWDOWN, 300, 0);
                SpellUtil.fx(p, MobEffects.DIG_SPEED, 300, 1);
                SpellUtil.sound(p, SoundEvents.ANVIL_PLACE);
            }
            case BEASTKIN -> {
                Vec3 dir = p.getLookAngle();
                p.setDeltaMovement(dir.x * 1.8D, 0.2D, dir.z * 1.8D);
                p.hurtMarked = true;
                d.setDash(p.level().getGameTime() + 10L, 5.0D * power);
                SpellUtil.sound(p, SoundEvents.WOLF_GROWL);
            }
            case VAMPIRE -> {
                LivingEntity t = SpellUtil.foeInFront(p, 5.0D);
                if (t == null) return fail(p);
                float dmg = (float) (8.0D * power);
                SpellUtil.hit(p, t, dmg);
                p.heal(dmg * 0.8F);
                SpellUtil.particles(level, ParticleTypes.DAMAGE_INDICATOR, t.position().add(0, 1.0, 0), 10, 0.3D, 0.1D);
                SpellUtil.sound(p, SoundEvents.PHANTOM_BITE);
            }
            case DEMON -> {
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 7.0D)) {
                    SpellUtil.hit(p, e, (float) (6.0D * power));
                    e.setSecondsOnFire(8);
                }
                SpellUtil.particles(level, ParticleTypes.SOUL_FIRE_FLAME, p.position().add(0, 0.5, 0), 80, 3.0D, 0.1D);
                SpellUtil.sound(p, SoundEvents.BLAZE_SHOOT, 1.0F, 0.6F);
            }
            case DRAGONBORN -> {
                Vec3 look = p.getLookAngle();
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 8.0D)) {
                    Vec3 to = e.position().add(0, e.getBbHeight() / 2.0D, 0).subtract(p.getEyePosition());
                    if (to.lengthSqr() > 0.01D && to.normalize().dot(look) > 0.8D) {
                        SpellUtil.hit(p, e, (float) (10.0D * power));
                        e.setSecondsOnFire(6);
                    }
                }
                Vec3 start = p.getEyePosition();
                for (int i = 1; i <= 8; i++) {
                    Vec3 pos = start.add(look.scale(i));
                    level.sendParticles(ParticleTypes.FLAME, pos.x, pos.y, pos.z, 6, 0.2D * i, 0.2D * i, 0.2D * i, 0.02D);
                }
                SpellUtil.sound(p, SoundEvents.ENDER_DRAGON_SHOOT, 0.8F, 1.2F);
            }
            case ORC -> {
                for (Player a : SpellUtil.allies(p, 8.0D)) {
                    SpellUtil.fx(a, MobEffects.DAMAGE_BOOST, 400, 1);
                }
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 8.0D)) {
                    SpellUtil.fx(e, MobEffects.WEAKNESS, 200, 0);
                }
                SpellUtil.sound(p, SoundEvents.RAVAGER_ROAR, 1.0F, 0.7F);
            }
            case UNDEAD -> {
                SpellUtil.fx(p, MobEffects.ABSORPTION, 400, 3);
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 4.0D)) {
                    SpellUtil.knock(e, p.position(), 0.9D, 0.4D);
                }
                SpellUtil.particles(level, ParticleTypes.SOUL, p.position().add(0, 1.0, 0), 30, 1.0D, 0.05D);
                SpellUtil.sound(p, SoundEvents.WITHER_SKELETON_AMBIENT);
            }
            case ANGEL -> {
                long until = p.level().getGameTime() + 300L;
                d.setFlightUntil(until);
                if (!p.isCreative() && !p.isSpectator()) {
                    p.getAbilities().mayfly = true;
                    p.onUpdateAbilities();
                }
                SpellUtil.particles(level, ParticleTypes.END_ROD, p.position().add(0, 1.0, 0), 40, 0.8D, 0.05D);
                SpellUtil.sound(p, SoundEvents.ELYTRA_FLYING, 1.0F, 1.0F);
            }
            case GOBLIN -> {
                SpellUtil.fx(p, MobEffects.INVISIBILITY, 160, 0);
                SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 160, 2);
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 16.0D)) {
                    if (e instanceof Mob m && m.getTarget() == p) m.setTarget(null);
                }
                SpellUtil.particles(level, ParticleTypes.POOF, p.position().add(0, 1.0, 0), 20, 0.4D, 0.02D);
                SpellUtil.sound(p, SoundEvents.FOX_SNIFF);
            }
            case MERFOLK -> {
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 8.0D)) {
                    SpellUtil.hit(p, e, (float) (7.0D * power));
                    SpellUtil.knock(e, p.position(), 1.4D, 0.5D);
                    SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 80, 1);
                }
                p.clearFire();
                SpellUtil.particles(level, ParticleTypes.SPLASH, p.position().add(0, 1.0, 0), 120, 3.0D, 0.2D);
                SpellUtil.particles(level, ParticleTypes.BUBBLE_POP, p.position().add(0, 1.0, 0), 60, 3.0D, 0.1D);
                SpellUtil.sound(p, SoundEvents.PLAYER_SPLASH_HIGH_SPEED);
            }
            case KITSUNE -> {
                LivingEntity t = SpellUtil.foeInFront(p, 24.0D);
                if (t == null || !SpellUtil.teleportBehind(p, t)) return fail(p);
                SpellUtil.fx(t, MobEffects.BLINDNESS, 100, 0);
                SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 100, 1);
                SpellUtil.sound(p, SoundEvents.FOX_TELEPORT);
            }
            case WEREWOLF -> {
                for (LivingEntity e : SpellUtil.foes(p, p.position(), 10.0D)) {
                    SpellUtil.fx(e, MobEffects.WEAKNESS, 200, 1);
                    SpellUtil.fx(e, MobEffects.MOVEMENT_SLOWDOWN, 100, 1);
                    if (e instanceof Mob m && m.getTarget() == p) m.setTarget(null);
                }
                SpellUtil.fx(p, MobEffects.DAMAGE_BOOST, 300, 1);
                SpellUtil.fx(p, MobEffects.MOVEMENT_SPEED, 300, 1);
                SpellUtil.sound(p, SoundEvents.WOLF_HOWL, 1.5F, 0.7F);
            }
        }
        return true;
    }

    private static boolean fail(ServerPlayer p) {
        SpellUtil.msg(p, "msg.mechanix.no_target");
        return false;
    }

    /** Пассивные способности. Вызывается раз в секунду. */
    public static void passives(ServerPlayer p, RpgData d, Race r) {
        ServerLevel level = SpellUtil.lvl(p);
        switch (r) {
            case ELF -> {
                if (level.getBiome(p.blockPosition()).is(BiomeTags.IS_FOREST)) {
                    p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
                }
            }
            case DWARF -> {
                if (!level.canSeeSky(p.blockPosition()) && p.getY() < 60) {
                    p.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, 60, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                }
            }
            case BEASTKIN -> p.addEffect(new MobEffectInstance(MobEffects.JUMP, 60, 0, true, false));
            case VAMPIRE -> {
                boolean sun = level.isDay() && level.canSeeSky(p.blockPosition().above()) && !p.isInWaterOrRain()
                        && p.getItemBySlot(EquipmentSlot.HEAD).isEmpty();
                if (sun) p.setSecondsOnFire(4);
                if (!level.isDay()) p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
            }
            case DEMON -> p.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 60, 0, true, false));
            case DRAGONBORN -> p.causeFoodExhaustion(0.15F);
            case UNDEAD -> {
                p.removeEffect(MobEffects.POISON);
                p.removeEffect(MobEffects.WITHER);
                p.removeEffect(MobEffects.HUNGER);
            }
            case MERFOLK -> {
                if (p.isInWater()) {
                    p.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 60, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 60, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                }
            }
            case KITSUNE -> p.addEffect(new MobEffectInstance(MobEffects.LUCK, 60, 0, true, false));
            case WEREWOLF -> {
                if (level.isNight()) {
                    p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
                    p.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 260, 0, true, false));
                }
            }
            default -> { }
        }
    }
}
