package com.example.mechanix.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

final class ItemTooltips {
    private ItemTooltips() {}

    static void add(ItemStack stack, List<Component> tooltip) {
        tooltip.add(Component.translatable(stack.getItem().getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
