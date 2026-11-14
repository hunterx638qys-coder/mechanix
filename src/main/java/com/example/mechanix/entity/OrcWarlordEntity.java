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
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Варлорд орков. Фаза 1: рывки. Фаза 2 (&lt;60% HP): ударная волна. Фаза 3 (&lt;30% HP): ярость, призыв орков.
 */
public class OrcWarlordEntity extends Zombie implements RpgBoss {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.RED, BossEvent.BossBarOverlay.NOTCHED_6);
    private int phase = 1;
    private int timer = 0;

    public OrcWarlordEntity(EntityType<? extends OrcWarlordEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttrs() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 400.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ATTACK_DAMAGE, 14.0D)
                .add(Attributes.ARMOR, 10.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.9D)
                .add(Attributes.FOLLOW_RANGE, 40.0D);
    }

    protected boolean isSunSensitive() {
        return false;
    }

    protected boolean convertsInWater() {
        return false;
    }

    @Override
    public void setBaby(boolean baby) {
        super.setBaby(false);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.NETHERITE_AXE));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setItemSlot(EquipmentSlot.CHEST, new ItemStack(Items.IRON_CHESTPLATE));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
        this.setDropChance(EquipmentSlot.CHEST, 0.0F);
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
            BossUtil.broadcast(this, "msg.mechanix.orc_warlord.phase" + np);
            if (np == 3) {
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 2400, 1));
                this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 2400, 1));
                BossUtil.summon(this, ModEntities.ORC.get(), 2);
            }
        }

        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (++timer >= (phase == 3 ? 70 : 110)) {
            timer = 0;
            if (phase >= 2) {
                BossUtil.slam(this, 8.0D, 10.0F, ParticleTypes.CAMPFIRE_COSY_SMOKE);
            }
            if (this.distanceToSqr(target) > 16.0D) {
                Vec3 dir = target.position().subtract(this.position()).normalize();
                this.setDeltaMovement(dir.x * 1.5D, 0.25D, dir.z * 1.5D);
                this.hurtMarked = true;
            }
            if (phase == 3 && BossUtil.count(this, OrcEntity.class, 24.0D) < 4) {
                BossUtil.summon(this, ModEntities.ORC.get(), 1);
            }
        }
    }
}
