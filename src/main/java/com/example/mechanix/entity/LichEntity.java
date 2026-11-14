package com.example.mechanix.entity;

import com.example.mechanix.registry.ModEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.BossEvent;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/**
 * Лич. Фаза 1: тёмные стрелы. Фаза 2 (&lt;60% HP): призыв рыцарей и телепорт.
 * Фаза 3 (&lt;30% HP): ночная волна смерти и высасывание жизни.
 */
public class LichEntity extends Skeleton implements RpgBoss {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.NOTCHED_6);
    private int phase = 1;
    private int timer = 0;

    public LichEntity(EntityType<? extends LichEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttrs() {
        return Skeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 300.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 8.0D)
                .add(Attributes.ARMOR, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.8D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossEvent.removePlayer(player);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        float f = this.getHealth() / this.getMaxHealth();
        bossEvent.setProgress(f);

        int np = f > 0.6F ? 1 : (f > 0.3F ? 2 : 3);
        if (np != phase) {
            phase = np;
            BossUtil.broadcast(this, "msg.mechanix.lich.phase" + np);
        }

        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (++timer < (phase == 3 ? 70 : 90)) return;
        timer = 0;

        if (this.distanceToSqr(target) < 784.0D && this.hasLineOfSight(target)) {
            BossUtil.bolt(this, target, 7.0F, ParticleTypes.SOUL_FIRE_FLAME);
        }
        if (phase >= 2) {
            if (BossUtil.count(this, SkeletonKnightEntity.class, 24.0D) < 4) {
                BossUtil.summon(this, ModEntities.SKELETON_KNIGHT.get(), 2);
            }
            double ang = this.random.nextDouble() * Math.PI * 2.0D;
            this.moveTo(target.getX() + Math.cos(ang) * 6.0D, target.getY(), target.getZ() + Math.sin(ang) * 6.0D);
            this.getNavigation().stop();
        }
        if (phase == 3) {
            for (Player pl : this.level().getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(10.0D),
                    e -> !e.isSpectator() && !e.isCreative())) {
                pl.hurt(this.damageSources().indirectMagic(this, this), 10.0F);
                pl.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 0));
                BossUtil.knock(pl, this.position(), 0.9D, 0.4D);
                this.heal(4.0F);
            }
            if (this.level() instanceof net.minecraft.server.level.ServerLevel sl) {
                sl.sendParticles(ParticleTypes.SOUL, this.getX(), this.getY() + 1.0D, this.getZ(), 60, 4.0D, 0.5D, 4.0D, 0.05D);
            }
        }
    }
}
