package com.example.mechanix.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class LaunchPadBlock extends PadBlock {
    /** Ключ в persistent data: до какого игрового тика отменяется урон от падения. */
    public static final String NO_FALL_UNTIL = "mechanix_nofall_until";

    public LaunchPadBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void activate(Level level, BlockPos pos, Entity entity) {
        if (entity.isShiftKeyDown() || !entity.onGround()) return; // присед = пропустить плиту

        Vec3 look = entity.getLookAngle();
        entity.setDeltaMovement(look.x * 1.1D, 1.0D, look.z * 1.1D);
        entity.hurtMarked = true;
        entity.fallDistance = 0.0F;
        entity.getPersistentData().putLong(NO_FALL_UNTIL, level.getGameTime() + 200L);

        if (level instanceof ServerLevel serverLevel) {
            level.playSound(null, pos, SoundEvents.FIREWORK_ROCKET_LAUNCH, SoundSource.BLOCKS, 0.8F, 1.2F);
            serverLevel.sendParticles(ParticleTypes.CLOUD, pos.getX() + 0.5D, pos.getY() + 0.2D, pos.getZ() + 0.5D,
                    12, 0.25D, 0.05D, 0.25D, 0.05D);
        }
    }
}
