package com.example.mechanix.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/** Сама логика (лечение при ударе) реализована в ModEvents#onLivingHurt. */
public class VampirismEffect extends MobEffect {
    public VampirismEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x8B0000);
    }
}
