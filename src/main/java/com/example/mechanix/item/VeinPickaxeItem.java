package com.example.mechanix.item;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.util.BlockUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.List;

/** Кирка: при добыче руды с зажатым приседом ломает всю жилу целиком. */
public class VeinPickaxeItem extends PickaxeItem {
    public VeinPickaxeItem(Tier tier, int attackDamage, float attackSpeed, Properties properties) {
        super(tier, attackDamage, attackSpeed, properties);
    }

    @Override
    public boolean mineBlock(ItemStack stack, Level level, BlockState state, BlockPos pos, LivingEntity miner) {
        boolean result = super.mineBlock(stack, level, state, pos, miner);

        if (!level.isClientSide && miner instanceof ServerPlayer player && player.isShiftKeyDown()
                && state.is(Tags.Blocks.ORES)) {
            ServerLevel serverLevel = (ServerLevel) level;
            List<BlockPos> vein = BlockUtil.floodFind(level, pos, s -> s.is(state.getBlock()),
                    MechanixConfig.VEIN_LIMIT.get(), true);
            for (BlockPos p : vein) {
                if (!BlockUtil.hasDurabilityLeft(stack)) break;
                BlockUtil.breakWithTool(serverLevel, player, p, stack);
            }
        }
        return result;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(getDescriptionId() + ".desc").withStyle(ChatFormatting.GRAY));
    }
}
