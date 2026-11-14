package com.example.mechanix.rpg;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/** Вспомогательные методы для заклинаний и способностей рас. */
public final class SpellUtil {
    private SpellUtil() {}

    public record Ray(LivingEntity entity, Vec3 pos) {}

    public static ServerLevel lvl(ServerPlayer p) {
        return (ServerLevel) p.level();
    }

    public static Ray ray(ServerPlayer p, double range) {
        Level level = p.level();
        Vec3 eye = p.getEyePosition();
        Vec3 look = p.getLookAngle();
        Vec3 end = eye.add(look.scale(range));
        BlockHitResult bh = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, p));
        double max = bh.getType() == HitResult.Type.MISS ? range : eye.distanceTo(bh.getLocation());
        Vec3 end2 = eye.add(look.scale(max));
        AABB box = p.getBoundingBox().expandTowards(look.scale(max)).inflate(1.0D);
        EntityHitResult eh = ProjectileUtil.getEntityHitResult(p, eye, end2, box,
                e -> e instanceof LivingEntity && e.isAlive() && !e.isSpectator() && e.isPickable() && e != p,
                max * max);
        if (eh != null && eh.getEntity() instanceof LivingEntity le) {
            return new Ray(le, eh.getLocation());
        }
        return new Ray(null, bh.getType() == HitResult.Type.MISS ? end : bh.getLocation());
    }

    public static boolean isFoe(ServerPlayer p, LivingEntity e) {
        if (e == p || !e.isAlive() || e instanceof Player) return false;
        if (e instanceof TamableAnimal t && t.isOwnedBy(p)) return false;
        return e instanceof Enemy || (e instanceof Mob m && m.getTarget() == p);
    }

    public static List<LivingEntity> foes(ServerPlayer p, Vec3 c, double r) {
        AABB box = new AABB(c.x - r, c.y - r, c.z - r, c.x + r, c.y + r, c.z + r);
        return p.level().getEntitiesOfClass(LivingEntity.class, box,
                e -> isFoe(p, e) && e.position().distanceToSqr(c) <= r * r);
    }

    public static List<Player> allies(ServerPlayer p, double r) {
        AABB box = p.getBoundingBox().inflate(r);
        return p.level().getEntitiesOfClass(Player.class, box,
                e -> e.isAlive() && !e.isSpectator() && e.distanceToSqr(p) <= r * r);
    }

    public static void hit(ServerPlayer p, LivingEntity e, float dmg) {
        e.hurt(p.damageSources().indirectMagic(p, p), dmg);
    }

    public static void fx(LivingEntity e, MobEffect effect, int ticks, int amp) {
        e.addEffect(new MobEffectInstance(effect, ticks, amp));
    }

    public static void knock(LivingEntity e, Vec3 from, double horizontal, double up) {
        Vec3 d = e.position().subtract(from);
        Vec3 flat = new Vec3(d.x, 0.0D, d.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        e.setDeltaMovement(flat.x * horizontal, up, flat.z * horizontal);
        e.hurtMarked = true;
    }

    public static void particles(ServerLevel level, ParticleOptions type, Vec3 pos, int count, double spread, double speed) {
        level.sendParticles(type, pos.x, pos.y, pos.z, count, spread, spread, spread, speed);
    }

    public static void trail(ServerLevel level, ParticleOptions type, Vec3 from, Vec3 to, int steps) {
        for (int i = 0; i <= steps; i++) {
            double f = i / (double) steps;
            level.sendParticles(type, Mth.lerp(f, from.x, to.x), Mth.lerp(f, from.y, to.y), Mth.lerp(f, from.z, to.z),
                    1, 0.0D, 0.0D, 0.0D, 0.0D);
        }
    }

    public static void sound(ServerPlayer p, SoundEvent s) {
        p.level().playSound(null, p.blockPosition(), s, SoundSource.PLAYERS, 1.0F, 1.0F);
    }

    public static void sound(ServerPlayer p, SoundEvent s, float vol, float pitch) {
        p.level().playSound(null, p.blockPosition(), s, SoundSource.PLAYERS, vol, pitch);
    }

    public static void msg(ServerPlayer p, String key) {
        p.displayClientMessage(Component.translatable(key), true);
    }

    /** Телепорт за спину цели. */
    public static boolean teleportBehind(ServerPlayer p, LivingEntity t) {
        Vec3 look = t.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0.0D, look.z);
        flat = flat.lengthSqr() < 1.0E-4D ? new Vec3(0.0D, 0.0D, 1.0D) : flat.normalize();
        Vec3 right = new Vec3(-flat.z, 0.0D, flat.x);
        Vec3[] offsets = {
                flat.scale(-1.6D), right.scale(1.6D), right.scale(-1.6D), flat.scale(1.6D), new Vec3(0.0D, 1.5D, 0.0D)
        };
        for (Vec3 off : offsets) {
            Vec3 dest = t.position().add(off);
            AABB box = p.getBoundingBox().move(dest.subtract(p.position()));
            if (p.level().noCollision(p, box)) {
                float yaw = (float) (Mth.atan2(t.getZ() - dest.z, t.getX() - dest.x) * 57.29577951308232D) - 90.0F;
                lvl(p).sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, p.getX(), p.getY() + 1.0D, p.getZ(),
                        10, 0.3D, 0.5D, 0.3D, 0.02D);
                p.connection.teleport(dest.x, dest.y, dest.z, yaw, 0.0F);
                lvl(p).sendParticles(net.minecraft.core.particles.ParticleTypes.POOF, dest.x, dest.y + 1.0D, dest.z,
                        10, 0.3D, 0.5D, 0.3D, 0.02D);
                return true;
            }
        }
        return false;
    }

    /** Ближайший враг в конусе перед игроком. */
    public static LivingEntity foeInFront(ServerPlayer p, double range) {
        Ray r = ray(p, range);
        if (r.entity() != null && isFoe(p, r.entity())) return r.entity();
        Vec3 look = p.getLookAngle();
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : foes(p, p.position(), range)) {
            Vec3 to = e.position().add(0.0D, e.getBbHeight() / 2.0D, 0.0D).subtract(p.getEyePosition());
            double dist = to.length();
            if (dist < 0.01D || to.normalize().dot(look) < 0.85D) continue;
            if (dist < bestDist) {
                bestDist = dist;
                best = e;
            }
        }
        return best;
    }

    public static boolean isEntity(Entity e) {
        return e != null;
    }
}
