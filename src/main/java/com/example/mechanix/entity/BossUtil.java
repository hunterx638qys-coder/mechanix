package com.example.mechanix.entity;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

/** Общие действия боссов. */
public final class BossUtil {
    private BossUtil() {}

    public static void broadcast(Mob boss, String key) {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        for (ServerPlayer sp : sl.getPlayers(pl -> pl.distanceToSqr(boss) < 2500.0D)) {
            sp.sendSystemMessage(Component.translatable(key).withStyle(ChatFormatting.RED));
        }
    }

    public static void knock(LivingEntity e, Vec3 from, double horizontal, double up) {
        Vec3 d = e.position().subtract(from);
        Vec3 flat = new Vec3(d.x, 0.0D, d.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        e.setDeltaMovement(flat.x * horizontal, up, flat.z * horizontal);
        e.hurtMarked = true;
    }

    /** Удар по земле: урон и отбрасывание игроков вокруг. */
    public static void slam(Mob boss, double radius, float damage, ParticleOptions particle) {
        for (Player pl : boss.level().getEntitiesOfClass(Player.class, boss.getBoundingBox().inflate(radius),
                e -> !e.isSpectator() && !e.isCreative() && e.distanceToSqr(boss) <= radius * radius)) {
            pl.hurt(boss.damageSources().mobAttack(boss), damage);
            knock(pl, boss.position(), 1.1D, 0.5D);
        }
        if (boss.level() instanceof ServerLevel sl) {
            sl.sendParticles(particle, boss.getX(), boss.getY() + 0.3D, boss.getZ(), 40, radius / 2.0D, 0.2D, radius / 2.0D, 0.05D);
        }
    }

    public static int count(Mob boss, Class<? extends Mob> type, double radius) {
        return boss.level().getEntitiesOfClass(type, boss.getBoundingBox().inflate(radius)).size();
    }

    public static void summon(Mob boss, EntityType<? extends Mob> type, int n) {
        if (!(boss.level() instanceof ServerLevel sl)) return;
        RandomSource r = boss.getRandom();
        for (int i = 0; i < n; i++) {
            Mob m = type.create(sl);
            if (m == null) continue;
            m.moveTo(boss.getX() + (r.nextDouble() - 0.5D) * 6.0D, boss.getY(),
                    boss.getZ() + (r.nextDouble() - 0.5D) * 6.0D, r.nextFloat() * 360.0F, 0.0F);
            m.finalizeSpawn(sl, sl.getCurrentDifficultyAt(m.blockPosition()), MobSpawnType.MOB_SUMMONED, null, null);
            if (boss.getTarget() != null) m.setTarget(boss.getTarget());
            sl.addFreshEntity(m);
        }
    }

    /** Магический снаряд (мгновенный). */
    public static void bolt(Mob boss, LivingEntity target, float damage, ParticleOptions particle) {
        target.hurt(boss.damageSources().indirectMagic(boss, boss), damage);
        if (boss.level() instanceof ServerLevel sl) {
            Vec3 from = boss.getEyePosition();
            Vec3 to = target.position().add(0.0D, target.getBbHeight() / 2.0D, 0.0D);
            for (int i = 0; i <= 16; i++) {
                double f = i / 16.0D;
                sl.sendParticles(particle, Mth.lerp(f, from.x, to.x), Mth.lerp(f, from.y, to.y), Mth.lerp(f, from.z, to.z),
                        1, 0.0D, 0.0D, 0.0D, 0.0D);
            }
        }
    }
}
