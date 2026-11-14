package com.example.mechanix.item;

import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgData;
import com.example.mechanix.rpg.Spell;
import com.example.mechanix.rpg.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Посох: ПКМ колдует заклинание за ману, независимо от класса игрока. */
public class StaffItem extends Item {
    private final Spell spell;

    public StaffItem(Spell spell, Properties properties) {
        super(properties);
        this.spell = spell;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            RpgData d = RpgData.of(sp);
            if (!d.hasRace()) {
                SpellUtil.msg(sp, "msg.mechanix.no_race");
            } else if (d.mana() < spell.mana) {
                SpellUtil.msg(sp, "msg.mechanix.no_mana");
            } else if (spell.cast.cast(sp, d)) {
                d.setMana(d.mana() - spell.mana);
                stack.hurtAndBreak(1, sp, x -> x.broadcastBreakEvent(hand));
                sp.getCooldowns().addCooldown(this, Math.max(10, spell.cooldown / 4));
                Rpg.sync(sp);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        ItemTooltips.add(stack, tooltip);
    }
}
