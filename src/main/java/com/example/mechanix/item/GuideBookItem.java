package com.example.mechanix.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Внутриигровая книга-гайд (страницы берутся из lang-файлов). */
public class GuideBookItem extends Item {
    private static final int PAGES = 8;

    public GuideBookItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            ItemStack book = new ItemStack(Items.WRITTEN_BOOK);
            CompoundTag tag = book.getOrCreateTag();
            tag.putString("title", "Mechanix");
            tag.putString("author", "Mechanix");
            ListTag pages = new ListTag();
            for (int i = 1; i <= PAGES; i++) {
                pages.add(StringTag.valueOf(Component.Serializer.toJson(Component.translatable("guide.mechanix.page" + i))));
            }
            tag.put("pages", pages);
            sp.openItemGui(book, hand);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
