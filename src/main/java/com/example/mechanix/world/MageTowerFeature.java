package com.example.mechanix.world;

import com.example.mechanix.MechanixConfig;
import com.example.mechanix.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/** Башня мага: 3 этажа, лестница, спавнеры скелетов-рыцарей, наверху Лич и сундуки. */
public class MageTowerFeature extends Feature<NoneFeatureConfiguration> {
    public MageTowerFeature() {
        super(NoneFeatureConfiguration.CODEC);
    }

    private static BlockState wall(RandomSource r) {
        int i = r.nextInt(10);
        return (i < 6 ? Blocks.STONE_BRICKS : (i < 9 ? Blocks.MOSSY_STONE_BRICKS : Blocks.CRACKED_STONE_BRICKS)).defaultBlockState();
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> ctx) {
        if (!MechanixConfig.STRUCTURES.get()) return false;
        WorldGenLevel level = ctx.level();
        BlockPos o = ctx.origin();
        RandomSource rnd = ctx.random();
        if (!StructureUtil.surfaceOk(level, o)) return false;

        final int r = 3;
        final int h = 15;
        BlockState air = StructureUtil.AIR;
        BlockState bricks = Blocks.STONE_BRICKS.defaultBlockState();
        BlockState planks = Blocks.DARK_OAK_PLANKS.defaultBlockState();

        // фундамент, очистка, пол первого этажа
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                StructureUtil.foundation(level, o.offset(dx, -1, dz), bricks);
                for (int y = 0; y <= h + 2; y++) StructureUtil.set(level, o.offset(dx, y, dz), air);
            }
        }

        // стены, окна, дверь
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                if (!edge) continue;
                for (int y = 0; y < h; y++) {
                    BlockState b = wall(rnd);
                    boolean midSide = (dx == 0 && Math.abs(dz) == r) || (dz == 0 && Math.abs(dx) == r);
                    if (midSide && (y == 2 || y == 7 || y == 12)) b = Blocks.IRON_BARS.defaultBlockState();
                    if (dz == r && dx == 0 && (y == 0 || y == 1)) b = air;
                    StructureUtil.set(level, o.offset(dx, y, dz), b);
                }
            }
        }

        // перекрытия: этажи на y=5, 10 и крыша на y=15
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                StructureUtil.set(level, o.offset(dx, 5, dz), planks);
                StructureUtil.set(level, o.offset(dx, 10, dz), planks);
                StructureUtil.set(level, o.offset(dx, h, dz), bricks);
                boolean edge = Math.abs(dx) == r || Math.abs(dz) == r;
                if (edge && ((dx + dz) & 1) == 0) StructureUtil.set(level, o.offset(dx, h + 1, dz), bricks);
            }
        }

        // лестница вдоль северной стены
        BlockState ladder = Blocks.LADDER.defaultBlockState().setValue(LadderBlock.FACING, Direction.SOUTH);
        for (int y = 0; y < h - 1; y++) {
            StructureUtil.set(level, o.offset(0, y, -(r - 1)), ladder);
        }

        // освещение
        BlockState lantern = Blocks.SOUL_LANTERN.defaultBlockState();
        StructureUtil.set(level, o.offset(-2, 0, 2), lantern);
        StructureUtil.set(level, o.offset(2, 6, 2), lantern);
        StructureUtil.set(level, o.offset(-2, 11, -2), lantern);

        // спавнеры, сундуки, босс
        StructureUtil.spawner(level, o.offset(1, 0, 1), ModEntities.SKELETON_KNIGHT.get(), rnd);
        StructureUtil.spawner(level, o.offset(1, 6, 1), ModEntities.SKELETON_KNIGHT.get(), rnd);
        StructureUtil.chest(level, o.offset(2, 11, 2), Direction.NORTH, "chests/mage_tower", rnd);
        StructureUtil.chest(level, o.offset(-2, 11, 2), Direction.NORTH, "chests/mage_tower", rnd);
        StructureUtil.boss(level, ModEntities.LICH.get(), o.offset(0, 11, 0));
        return true;
    }
}
