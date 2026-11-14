package com.example.mechanix.rpg;

import com.example.mechanix.registry.ModArmorMaterial;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;

/** Бонусы за полный комплект брони. */
public final class ArmorSets {
    private ArmorSets() {}

    public static boolean has(Player p, ModArmorMaterial material) {
        for (ItemStack s : p.getArmorSlots()) {
            if (!(s.getItem() instanceof ArmorItem armor) || armor.getMaterial() != material) return false;
        }
        return true;
    }

    /** Раз в секунду. */
    public static void tick(ServerPlayer p, RpgData d) {
        if (has(p, ModArmorMaterial.MITHRIL)) {
            p.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 60, 0, true, false));
            d.setMana(d.mana() + 2.0D);
        }
        if (has(p, ModArmorMaterial.STAR_IRON)) {
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 60, 0, true, false));
            p.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 60, 0, true, false));
        }
        if (has(p, ModArmorMaterial.BLOODSTONE)) {
            p.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 60, 0, true, false));
        }
    }
}
