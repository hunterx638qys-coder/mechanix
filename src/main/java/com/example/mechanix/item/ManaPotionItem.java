package com.example.mechanix.item;

import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class ManaPotionItem extends Item {
    private final int amount;

    public ManaPotionItem(int amount, Properties properties) {
        super(properties);
        this.amount = amount;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public int getUseDuration(ItemStack stack) {
        return 32;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        return ItemUtils.startUsingInstantly(level, player, hand);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!level.isClientSide && entity instanceof ServerPlayer sp) {
            RpgData d = RpgData.of(sp);
            d.setMana(d.mana() + amount);
            Rpg.sync(sp);
        }
        if (entity instanceof Player pl && !pl.getAbilities().instabuild) {
            stack.shrink(1);
            if (stack.isEmpty()) return new ItemStack(Items.GLASS_BOTTLE);
            pl.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
