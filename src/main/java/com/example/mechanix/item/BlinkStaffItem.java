package com.example.mechanix.item;

import com.example.mechanix.MechanixConfig;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** ПКМ — телепорт вперёд по взгляду до первого препятствия. */
public class BlinkStaffItem extends DescribedItem {
    public BlinkStaffItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide && player instanceof ServerPlayer sp && level instanceof ServerLevel serverLevel) {
            Vec3 look = player.getLookAngle();
            Vec3 eye = player.getEyePosition();
            double max = MechanixConfig.BLINK_DISTANCE.get();

            BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(look.scale(max)),
                    ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
            double reach = hit.getType() == HitResult.Type.MISS
                    ? max : Math.max(0.0D, eye.distanceTo(hit.getLocation()) - 0.6D);

            Vec3 dest = null;
            for (double d = reach; d >= 1.0D; d -= 0.5D) {
                Vec3 offset = look.scale(d);
                AABB moved = player.getBoundingBox().move(offset);
                if (level.noCollision(player, moved)) {
                    dest = player.position().add(offset);
                    break;
                }
            }

            if (dest == null) {
                player.displayClientMessage(Component.translatable("message.mechanix.blink_fail"), true);
            } else {
                serverLevel.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1.0D, player.getZ(),
                        30, 0.3D, 0.5D, 0.3D, 0.1D);
                level.playSound(null, player.getX(), player.getY(), player.getZ(),
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);

                sp.connection.teleport(dest.x, dest.y, dest.z, sp.getYRot(), sp.getXRot());
                sp.fallDistance = 0.0F;

                serverLevel.sendParticles(ParticleTypes.PORTAL, dest.x, dest.y + 1.0D, dest.z,
                        30, 0.3D, 0.5D, 0.3D, 0.1D);
                level.playSound(null, dest.x, dest.y, dest.z,
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.2F);

                stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                player.getCooldowns().addCooldown(this, 30);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
