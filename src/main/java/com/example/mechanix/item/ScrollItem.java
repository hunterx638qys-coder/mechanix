package com.example.mechanix.item;

import com.example.mechanix.rpg.Rpg;
import com.example.mechanix.rpg.RpgData;
import com.example.mechanix.rpg.SpellUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/** Свитки: перерождение (смена расы), сброс характеристик, том опыта. */
public class ScrollItem extends Item {
    public enum Kind { REBIRTH, RESET, TOME }

    private final Kind kind;

    public ScrollItem(Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && player instanceof ServerPlayer sp) {
            RpgData d = RpgData.of(sp);
            boolean used = false;
            if (!d.hasRace()) {
                SpellUtil.msg(sp, "msg.mechanix.no_race");
            } else {
                switch (kind) {
                    case REBIRTH -> used = Rpg.rebirth(sp);
                    case RESET -> used = Rpg.resetStats(sp);
                    case TOME -> {
                        Rpg.addXp(sp, (int) (RpgData.xpNeeded(d.level()) * 0.4D));
                        used = true;
                    }
                }
            }
            if (used) {
                level.playSound(null, sp.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                if (!sp.getAbilities().instabuild) stack.shrink(1);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
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
