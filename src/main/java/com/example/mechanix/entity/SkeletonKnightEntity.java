package com.example.mechanix.entity;

import net.minecraft.util.RandomSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;

/** Скелет-рыцарь: ближний бой, не горит на солнце. */
public class SkeletonKnightEntity extends Skeleton {
    public SkeletonKnightEntity(EntityType<? extends SkeletonKnightEntity> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttrs() {
        return Skeleton.createAttributes()
                .add(Attributes.MAX_HEALTH, 32.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.27D)
                .add(Attributes.ATTACK_DAMAGE, 5.0D)
                .add(Attributes.ARMOR, 4.0D);
    }

    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected void populateDefaultEquipmentSlots(RandomSource random, DifficultyInstance difficulty) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.02F);
        this.setDropChance(EquipmentSlot.HEAD, 0.02F);
    }
}
