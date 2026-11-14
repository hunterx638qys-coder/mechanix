package com.example.mechanix.item;

import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Клинок Бездны: иссушает врага и возвращает ману владельцу. */
public class AbyssBladeItem extends SwordItem {
    public AbyssBladeItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        target.addEffect(new MobEffectInstance(MobEffects.WITHER, 100, 1));
        if (!attacker.level().isClientSide && attacker instanceof ServerPlayer sp) {
            RpgData d = RpgData.of(sp);
            if (d.hasRace()) {
                d.setMana(d.mana() + 4.0D);
                Rpg.sync(sp);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
