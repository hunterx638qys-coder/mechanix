package com.example.mechanix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class HealPadBlock extends PadBlock {
    public HealPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void activate(Level level, BlockPos pos, Entity entity) {
        if (level.isClientSide || !(entity instanceof LivingEntity living)) return;
        MobEffectInstance cur = living.getEffect(MobEffects.REGENERATION);
        if (cur == null || cur.getDuration() < 20) {
            living.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 1, true, true));
        }
    }
}
