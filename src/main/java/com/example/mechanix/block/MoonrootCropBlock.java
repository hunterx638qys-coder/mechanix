package com.example.mechanix.block;

import com.example.mechanix.registry.ModItems;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;

/** Лунный корень: культура на грядке, 8 стадий роста. */
public class MoonrootCropBlock extends CropBlock {
    public MoonrootCropBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.MOONROOT_SEEDS.get();
    }
}
