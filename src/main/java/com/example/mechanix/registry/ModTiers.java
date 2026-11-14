package com.example.mechanix.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.ForgeTier;
import net.minecraftforge.common.Tags;

public final class ModTiers {
    private ModTiers() {}

    public static final ForgeTier MITHRIL = new ForgeTier(3, 1800, 8.0F, 3.0F, 18,
            BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.MITHRIL_INGOT.get()));

    public static final ForgeTier STAR_IRON = new ForgeTier(4, 2800, 9.5F, 4.0F, 12,
            Tags.Blocks.NEEDS_NETHERITE_TOOL, () -> Ingredient.of(ModItems.STAR_IRON_INGOT.get()));

    public static final ForgeTier BLOODSTONE = new ForgeTier(3, 1200, 7.0F, 3.0F, 22,
            BlockTags.NEEDS_DIAMOND_TOOL, () -> Ingredient.of(ModItems.BLOODSTONE.get()));
}
