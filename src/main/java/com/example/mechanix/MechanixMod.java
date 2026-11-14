package com.example.mechanix;

import com.example.mechanix.network.ModNetwork;
import com.example.mechanix.registry.ModBlocks;
import com.example.mechanix.registry.ModCreativeTabs;
import com.example.mechanix.registry.ModEntities;
import com.example.mechanix.registry.ModFeatures;
import com.example.mechanix.registry.ModItems;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(MechanixMod.MODID)
public class MechanixMod {
    public static final String MODID = "mechanix";

    public MechanixMod() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(bus);
        ModItems.ITEMS.register(bus);
        ModEntities.ENTITIES.register(bus);
        ModFeatures.FEATURES.register(bus);
        ModCreativeTabs.TABS.register(bus);

        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, MechanixConfig.SPEC);
        bus.addListener(this::commonSetup);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(ModNetwork::register);
    }
}
