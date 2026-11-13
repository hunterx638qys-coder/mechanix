package com.example.mechanix.util;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

public final class BlockUtil {
    private BlockUtil() {}

    /** Поиск связной группы блоков (BFS). Исходный блок в результат не входит. */
    public static List<BlockPos> floodFind(Level level, BlockPos origin, Predicate<BlockState> match, int limit, boolean diagonal) {
        List<BlockPos> result = new ArrayList<>();
        Set<BlockPos> visited = new HashSet<>();
        Deque<BlockPos> queue = new ArrayDeque<>();
        visited.add(origin);
        queue.add(origin);

        while (!queue.isEmpty()) {
            BlockPos cur = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        if (dx == 0 && dy == 0 && dz == 0) continue;
                        if (!diagonal && Math.abs(dx) + Math.abs(dy) + Math.abs(dz) != 1) continue;
                        BlockPos next = cur.offset(dx, dy, dz);
                        if (!visited.add(next)) continue;
                        if (match.test(level.getBlockState(next))) {
                            result.add(next);
                            queue.add(next);
                            if (result.size() >= limit) return result;
                        }
                    }
                }
            }
        }
        return result;
    }

    /** Сломать блок инструментом игрока: дроп с учётом зачарований, износ, эффект разрушения. */
    public static void breakWithTool(ServerLevel level, ServerPlayer player, BlockPos pos, ItemStack tool) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (!player.isCreative()) {
            Block.dropResources(state, level, pos, be, player, tool);
        }
        level.destroyBlock(pos, false, player);
        tool.hurtAndBreak(1, player, p -> p.broadcastBreakEvent(InteractionHand.MAIN_HAND));
    }

    /** Остался ли у инструмента запас прочности (чтобы массовая добыча не ломала его). */
    public static boolean hasDurabilityLeft(ItemStack stack) {
        return !stack.isEmpty() && (!stack.isDamageableItem() || stack.getMaxDamage() - stack.getDamageValue() > 1);
    }
}
