package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.effect.VampirismEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS =
            DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MechanixMod.MODID);

    public static final RegistryObject<MobEffect> VAMPIRISM = EFFECTS.register("vampirism", VampirismEffect::new);
}
