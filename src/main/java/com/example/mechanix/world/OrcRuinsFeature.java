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

/** Руины орков: обвалившиеся стены, колонны, алтарь, сундуки, спавнеры и Варлорд. */
public class OrcRuinsFeature extends Feature<NoneFeatureConfiguration> {
    public OrcRuinsFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static BlockState ruinBlock(RandomSource r) {
        int i = r.nextInt(10);
        return (i < 4 ? Blocks.COBBLESTONE : (i < 8 ? Blocks.MOSSY_COBBLESTONE : Blocks.STONE_BRICKS)).defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!MechanixConfig.STRUCTURES.get()) return false;
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        RandomSource rnd = ctx.random();
        if (!StructureUtil.surfaceOk(level, o)) return false;

        final int r = 5;
        BlockState floor = Blocks.MOSSY_STONE_BRICKS.defaultBlockState();

        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                BlockPos base = o.offset(dx, 0, dz);
                StructureUtil.foundation(level, base.below(), floor);
                StructureUtil.clearColumn(level, base, 8);

                boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                boolean gap = dz == r && Math.abs(dx) <= 1;
                boolean corner = Math.abs(dx) == r && Math.abs(dz) == r;
                if (corner) {
                    for (int y = 0; y < 6; y++) StructureUtil.set(level, base.above(y), Blocks.STONE_BRICKS.defaultBlockState());
                } else if (edge && !gap) {
                    int height = rnd.nextInt(5);
                    for (int y = 0; y < height; y++) StructureUtil.set(level, base.above(y), ruinBlock(rnd));
                }
            }
        }

        // алтарь
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                StructureUtil.set(level, o.offset(dx, 0, dz), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState());
            }
        }
        StructureUtil.set(level, o.offset(0, 1, 0), Blocks.CRYING_OBSIDIAN.defaultBlockState());
        StructureUtil.set(level, o.offset(-1, 1, -1), Blocks.SOUL_LANTERN.defaultBlockState());
        StructureUtil.set(level, o.offset(1, 1, 1), Blocks.SOUL_LANTERN.defaultBlockState());

        StructureUtil.chest(level, o.offset(3, 0, 3), Direction.NORTH, "chests/orc_ruins", rnd);
        StructureUtil.chest(level, o.offset(-3, 0, -3), Direction.SOUTH, "chests/orc_ruins", rnd);
        StructureUtil.spawner(level, o.offset(3, 0, -3), ModEntities.ORC.get(), rnd);
        StructureUtil.spawner(level, o.offset(-3, 0, 3), ModEntities.ORC.get(), rnd);
        StructureUtil.boss(level, ModEntities.ORC_WARLORD.get(), o.offset(0, 2, 2));
        return true;
    }
}
