package com.example.mechanix.item;

import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

/** Шуточная бита: почти без урона, но отправляет врага в полёт. */
public class BonkBatItem extends SwordItem {
    public BonkBatItem(Tier tier, int damage, float speed, Properties properties) {
        super(tier, damage, speed, properties);
    }

    @Override
    public boolean hurtEnemy(ItemStack stack, LivingEntity target, LivingEntity attacker) {
        Vec3 dir = target.position().subtract(attacker.position());
        Vec3 flat = new Vec3(dir.x, 0.0D, dir.z);
        flat = flat.lengthSqr() < 1.0E-4D ? attacker.getLookAngle() : flat.normalize();
        target.setDeltaMovement(flat.x * 3.0D, 1.2D, flat.z * 3.0D);
        target.hurtMarked = true;
        target.level().playSound(null, target.blockPosition(), SoundEvents.ANVIL_LAND, SoundSource.PLAYERS, 0.6F, 1.8F);
        return super.hurtEnemy(stack, target, attacker);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
