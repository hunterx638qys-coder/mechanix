package com.example.mechanix.item;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/** ПКМ — вызывает молнию в блок, на который смотрит игрок (до 48 блоков). */
public class StormRodItem extends DescribedItem {
    private static final double RANGE = 48.0D;

    public StormRodItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (!level.isClientSide) {
            Vec3 eye = player.getEyePosition();
            BlockHitResult hit = level.clip(new ClipContext(eye, eye.add(player.getLookAngle().scale(RANGE)),
                    ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));

            if (hit.getType() == HitResult.Type.MISS) {
                player.displayClientMessage(Component.translatable("message.mechanix.storm_fail"), true);
            } else {
                BlockPos target = hit.getBlockPos().above();
                LightningBolt bolt = EntityType.LIGHTNING_BOLT.create(level);
                if (bolt != null) {
                    bolt.moveTo(Vec3.atBottomCenterOf(target));
                    if (player instanceof ServerPlayer sp) bolt.setCause(sp);
                    level.addFreshEntity(bolt);
                    stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
                    player.getCooldowns().addCooldown(this, 100);
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
