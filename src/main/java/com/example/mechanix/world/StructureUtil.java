package com.example.mechanix.world;

import com.example.mechanix.MechanixConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Вспомогательные методы для генерации структур. */
final class StructureUtil {
    static final BlockState AIR = Blocks.AIR.defaultBlockState();

    private StructureUtil() {}

    static void set(WorldGenLevel level, BlockPos pos, BlockState state) {
        level.setBlock(pos, state, 2);
    }

    /** Ровная поверхность без воды под точкой. */
    static boolean surfaceOk(WorldGenLevel level, BlockPos origin) {
        BlockState below = level.getBlockState(origin.below());
        return below.getFluidState().isEmpty() && !below.isAir();
    }

    /** Достраивает фундамент вниз до твёрдой земли (не больше 12 блоков). */
    static void foundation(WorldGenLevel level, BlockPos top, BlockState state) {
        for (int i = 0; i < 12; i++) {
            BlockPos p = top.below(i);
            BlockState st = level.getBlockState(p);
            if (i > 0 && st.canOcclude() && st.getFluidState().isEmpty()) break;
            level.setBlock(p, state, 2);
        }
    }

    static void clearColumn(WorldGenLevel level, BlockPos base, int height) {
        for (int y = 0; y <= height; y++) {
            BlockPos p = base.above(y);
            if (!level.getBlockState(p).isAir()) level.setBlock(p, AIR, 2);
        }
    }

    static void spawner(WorldGenLevel level, BlockPos pos, EntityType<?> type, RandomSource random) {
        level.setBlock(pos, Blocks.SPAWNER.defaultBlockState(), 2);
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof SpawnerBlockEntity spawner) {
            spawner.setEntityId(type, random);
        }
    }

    static void chest(WorldGenLevel level, BlockPos pos, Direction facing, String lootPath, RandomSource random) {
        level.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), 2);
        RandomizableContainerBlockEntity.setLootTable(level, random, pos, new ResourceLocation("mechanix", lootPath));
    }

    static void boss(WorldGenLevel level, EntityType<? extends Mob> type, BlockPos pos) {
        if (!MechanixConfig.BOSS_SPAWNS.get()) return;
        Mob mob = type.create(level.getLevel());
        if (mob == null) return;
        mob.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, 0.0F, 0.0F);
        mob.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
    }
}
