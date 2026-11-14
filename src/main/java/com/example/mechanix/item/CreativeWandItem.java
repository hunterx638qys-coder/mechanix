package com.example.mechanix.item;

import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgData;
import com.example.mechanix.rpg.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Тестовый жезл: работает только в креативе. ПКМ +1 уровень, Shift+ПКМ полная мана. */
public class CreativeWandItem extends Item {
    public CreativeWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            if (!sp.isCreative()) {
                SpellUtil.msg(sp, "msg.mechanix.creative_only");
            } else {
                RpgData d = RpgData.of(sp);
                if (!d.hasRace()) {
                    SpellUtil.msg(sp, "msg.mechanix.no_race");
                } else if (sp.isShiftKeyDown()) {
                    d.setMana(d.maxMana());
                    Rpg.sync(sp);
                } else {
                    Rpg.addXp(sp, RpgData.xpNeeded(d.level()));
                }
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
