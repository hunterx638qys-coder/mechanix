package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.block.MoonrootCropBlock;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MechanixMod.MODID);

    private static BlockBehaviour.Properties ore(float hardness) {
        return BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(hardness, 3.0F)
                .requiresCorrectToolForDrops().sound(SoundType.STONE);
    }

    public static final RegistryObject<Block> MITHRIL_ORE = BLOCKS.register("mithril_ore",
            () -> new DropExperienceBlock(ore(3.5F), UniformInt.of(2, 5)));
    public static final RegistryObject<Block> STAR_IRON_ORE = BLOCKS.register("star_iron_ore",
            () -> new DropExperienceBlock(ore(5.0F), UniformInt.of(4, 8)));
    public static final RegistryObject<Block> BLOODSTONE_ORE = BLOCKS.register("bloodstone_ore",
            () -> new DropExperienceBlock(ore(4.0F), UniformInt.of(3, 6)));
    public static final RegistryObject<Block> MANA_CRYSTAL_ORE = BLOCKS.register("mana_crystal_ore",
            () -> new DropExperienceBlock(ore(3.0F), UniformInt.of(1, 4)));

    public static final RegistryObject<Block> MOONROOT_CROP = BLOCKS.register("moonroot_crop",
            () -> new MoonrootCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT)));
}
