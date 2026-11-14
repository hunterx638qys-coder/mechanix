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

/**
 * Король гоблинов. Фаза 1: призывает гоблинов. Фаза 2 (&lt;60% HP): ярость и удары по земле.
 * Фаза 3 (&lt;30% HP): безумие, быстрый призыв, регенерация.
 */
public class GoblinKingEntity extends Zombie implements RpgBoss {
    private final ServerBossEvent bossEvent = new ServerBossEvent(this.getDisplayName(),
            BossEvent.BossBarColor.GREEN, BossEvent.BossBarOverlay.NOTCHED_6);
    private int phase = 1;
    private int timer = 0;

    public GoblinKingEntity(EntityType<? extends GoblinKingEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttrs() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 200.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.28D)
                .add(Attributes.ATTACK_DAMAGE, 9.0D)
                .add(Attributes.ARMOR, 6.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.7D)
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
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.GOLDEN_SWORD));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.GOLDEN_HELMET));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
        this.setDropChance(EquipmentSlot.HEAD, 0.0F);
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
            BossUtil.broadcast(this, "msg.mechanix.goblin_king.phase" + np);
            if (np == 2) {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.33D);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 0));
            } else {
                this.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.38D);
                this.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 1200, 1));
            }
        }

        LivingEntity target = this.getTarget();
        if (target == null) return;
        if (++timer >= (phase == 3 ? 60 : 100)) {
            timer = 0;
            if (BossUtil.count(this, GoblinEntity.class, 24.0D) < 6) {
                BossUtil.summon(this, ModEntities.GOBLIN.get(), phase == 3 ? 3 : 2);
            }
            if (phase >= 2) {
                BossUtil.slam(this, 6.0D, 8.0F, ParticleTypes.EXPLOSION);
            }
        }
        if (phase == 3 && this.tickCount % 20 == 0) {
            this.heal(2.0F);
        }
    }
}
