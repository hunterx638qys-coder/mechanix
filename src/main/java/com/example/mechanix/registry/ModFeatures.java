package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.world.GoblinCampFeature;
import com.example.mechanix.world.MageTowerFeature;
import com.example.mechanix.world.OrcRuinsFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, MechanixMod.MODID);

    public static final RegistryObject<Feature<?>> MAGE_TOWER = FEATURES.register("mage_tower", MageTowerFeature::new);
    public static final RegistryObject<Feature<?>> GOBLIN_CAMP = FEATURES.register("goblin_camp", GoblinCampFeature::new);
    public static final RegistryObject<Feature<?>> ORC_RUINS = FEATURES.register("orc_ruins", OrcRuinsFeature::new);
}
