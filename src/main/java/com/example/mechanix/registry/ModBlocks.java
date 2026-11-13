package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.block.FragileBlock;
import com.example.mechanix.block.HealPadBlock;
import com.example.mechanix.block.LaunchPadBlock;
import com.example.mechanix.block.SpeedPadBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlocks {
    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, MechanixMod.MODID);

    public static final RegistryObject<Block> LAUNCH_PAD = BLOCKS.register("launch_pad",
            () -> new LaunchPadBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_RED).strength(1.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> SPEED_PAD = BLOCKS.register("speed_pad",
            () -> new SpeedPadBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_LIGHT_BLUE).strength(1.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> HEAL_PAD = BLOCKS.register("heal_pad",
            () -> new HealPadBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.COLOR_GREEN).strength(1.0F).sound(SoundType.METAL).noOcclusion()));

    public static final RegistryObject<Block> FRAGILE_BLOCK = BLOCKS.register("fragile_block",
            () -> new FragileBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(0.5F).sound(SoundType.GRAVEL)));
}
