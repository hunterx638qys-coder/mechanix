package com.example.mechanix.world;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Лагерь гоблинов: частокол, две хижины, костёр, сундуки, спавнеры и Король гоблинов. */
public class GoblinCampFeature extends Feature<NoneFeatureConfiguration> {
    public GoblinCampFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!MechanixConfig.STRUCTURES.get()) return false;
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        RandomSource rnd = ctx.random();
        if (!StructureUtil.surfaceOk(level, o)) return false;

        final int r = 6;
        BlockState dirt = Blocks.COARSE_DIRT.defaultBlockState();
        BlockState log = Blocks.SPRUCE_LOG.defaultBlockState();
        BlockState planks = Blocks.SPRUCE_PLANKS.defaultBlockState();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int d2 = dx * dx + dz * dz;
                if (d2 > r * r) continue;
                BlockPos base = o.offset(dx, 0, dz);
                StructureUtil.foundation(level, base.below(), dirt);
                StructureUtil.clearColumn(level, base, 6);
                if (d2 >= 25 && ((dx + dz) & 1) == 0 && !(dz > 4 && Math.abs(dx) <= 1)) {
                    for (int y = 0; y < 3; y++) StructureUtil.set(level, base.above(y), log);
                }
            }
        }

        // костёр
        StructureUtil.set(level, o, Blocks.CAMPFIRE.defaultBlockState());

        // две хижины
        for (int side = -1; side <= 1; side += 2) {
            BlockPos c = o.offset(side * 3, 0, -3);
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    boolean edge = Math.abs(dx) == 1 || Math.abs(dz) == 1;
                    for (int y = 0; y < 2; y++) {
                        boolean door = dz == 1 && dx == 0;
                        StructureUtil.set(level, c.offset(dx, y, dz), edge && !door ? planks : StructureUtil.AIR);
                    }
                    StructureUtil.set(level, c.offset(dx, 2, dz), planks);
                }
            }
        }
        StructureUtil.spawner(level, o.offset(-3, 0, -3), ModEntities.GOBLIN.get(), rnd);
        StructureUtil.chest(level, o.offset(3, 0, -3), Direction.SOUTH, "chests/goblin_camp", rnd);
        StructureUtil.chest(level, o.offset(0, 0, -5), Direction.SOUTH, "chests/goblin_camp", rnd);
        StructureUtil.spawner(level, o.offset(2, 0, 3), ModEntities.GOBLIN.get(), rnd);

        // трон и босс
        StructureUtil.set(level, o.offset(0, 0, 2), Blocks.HAY_BLOCK.defaultBlockState());
        StructureUtil.boss(level, ModEntities.GOBLIN_KING.get(), o.offset(0, 1, 2));
        return true;
    }
}
