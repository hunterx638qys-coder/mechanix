package com.example.mechanix.item;

import com.example.mechanix.MechanixConfig;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/** ПКМ включает/выключает. Пока включён и лежит в инвентаре — притягивает предметы вокруг. */
public class MagnetAmuletItem extends DescribedItem {
    private static final String TAG_ACTIVE = "Active";

    public MagnetAmuletItem(Properties properties) {
        super(properties);
    }

    private static boolean isActive(ItemStack stack) {
        return stack.hasTag() && stack.getTag().getBoolean(TAG_ACTIVE);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide) {
            boolean active = !isActive(stack);
            stack.getOrCreateTag().putBoolean(TAG_ACTIVE, active);
            player.displayClientMessage(Component.translatable(
                    active ? "message.mechanix.magnet_on" : "message.mechanix.magnet_off"), true);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slot, boolean selected) {
        if (level.isClientSide || !(entity instanceof Player player) || !isActive(stack)) return;

        double radius = MechanixConfig.MAGNET_RADIUS.get();
        Vec3 target = player.position().add(0.0D, player.getBbHeight() / 2.0D, 0.0D);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, player.getBoundingBox().inflate(radius))) {
            Vec3 pull = target.subtract(item.position());
            if (pull.lengthSqr() < 0.25D) continue;
            item.setDeltaMovement(pull.normalize().scale(0.35D));
            item.hurtMarked = true;
        }
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return isActive(stack);
    }
}
