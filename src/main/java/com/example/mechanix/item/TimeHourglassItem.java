package com.example.mechanix.item;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** ПКМ — переключает время суток (день -> ночь, ночь -> утро). */
public class TimeHourglassItem extends DescribedItem {
    public TimeHourglassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);

        if (level instanceof ServerLevel serverLevel) {
            long t = serverLevel.getDayTime();
            long dayStart = t - (t % 24000L);
            boolean isDay = (t % 24000L) < 12500L;

            serverLevel.setDayTime(isDay ? dayStart + 13000L : dayStart + 24000L);
            player.displayClientMessage(Component.translatable(
                    isDay ? "message.mechanix.time_night" : "message.mechanix.time_day"), true);
            level.playSound(null, player.blockPosition(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.0F, 0.8F);

            stack.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            player.getCooldowns().addCooldown(this, 200);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }
}
