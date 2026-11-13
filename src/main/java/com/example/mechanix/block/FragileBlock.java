package com.example.mechanix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Рассыпается через секунду после того, как на него наступили. */
public class FragileBlock extends Block {
    private static final int DELAY_TICKS = 20;

    public FragileBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (!level.isClientSide && entity instanceof LivingEntity && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.scheduleTick(pos, this, DELAY_TICKS);
            level.playSound(null, pos, SoundEvents.GRAVEL_HIT, SoundSource.BLOCKS, 1.0F, 0.6F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(ParticleTypes.POOF, pos.getX() + 0.5D, pos.getY() + 1.0D, pos.getZ() + 0.5D,
                8, 0.3D, 0.1D, 0.3D, 0.02D);
        level.destroyBlock(pos, false);
    }
}
