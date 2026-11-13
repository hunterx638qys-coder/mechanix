package com.example.mechanix;

import com.example.mechanix.registry.ModBlocks;
import com.example.mechanix.registry.ModCreativeTabs;
import com.example.mechanix.registry.ModEffects;
import com.example.mechanix.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MechanixMod.MODID)
public class MechanixMod {
    public static final String MODID = "mechanix";

    public MechanixMod() {
        IEventBus modBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModEffects.EFFECTS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MechanixConfig.SPEC);
    }
}
