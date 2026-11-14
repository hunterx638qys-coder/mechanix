package com.example.mechanix.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Секира варлорда: удар задевает врагов рядом с целью. */
public class OrcGreataxeItem extends AxeItem {
    public OrcGreataxeItem(Tier tier, float damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        if (!attacker.level().isClientSide && attacker instanceof Player player) {
            for (LivingEntity e : attacker.level().getEntitiesOfClass(LivingEntity.class, target.getBoundingBox().inflate(2.5D),
                    x -> x != target && x != attacker && x.isAlive() && !(x instanceof Player))) {
                e.hurt(player.damageSources().playerAttack(player), 6.0F);
            }
        }
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
