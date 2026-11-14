package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.entity.CaveStalkerEntity;
import com.example.mechanix.entity.GoblinEntity;
import com.example.mechanix.entity.GoblinKingEntity;
import com.example.mechanix.entity.LichEntity;
import com.example.mechanix.entity.OrcEntity;
import com.example.mechanix.entity.OrcWarlordEntity;
import com.example.mechanix.entity.SkeletonKnightEntity;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** События шины мода: атрибуты мобов и правила спавна. */
@Mod.EventBusSubscriber(modid = MechanixMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEvents {

    @SubscribeEvent
    public static void attributes(EntityAttributeCreationEvent e) {
        e.put(ModEntities.GOBLIN.get(), GoblinEntity.createAttrs().build());
        e.put(ModEntities.ORC.get(), OrcEntity.createAttrs().build());
        e.put(ModEntities.SKELETON_KNIGHT.get(), SkeletonKnightEntity.createAttrs().build());
        e.put(ModEntities.CAVE_STALKER.get(), CaveStalkerEntity.createAttrs().build());
        e.put(ModEntities.GOBLIN_KING.get(), GoblinKingEntity.createAttrs().build());
        e.put(ModEntities.ORC_WARLORD.get(), OrcWarlordEntity.createAttrs().build());
        e.put(ModEntities.LICH.get(), LichEntity.createAttrs().build());
    }

    @SubscribeEvent
    public static void spawns(SpawnPlacementRegisterEvent e) {
        e.register(ModEntities.GOBLIN.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        e.register(ModEntities.ORC.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        e.register(ModEntities.SKELETON_KNIGHT.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
        e.register(ModEntities.CAVE_STALKER.get(), SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                Monster::checkMonsterSpawnRules, SpawnPlacementRegisterEvent.Operation.REPLACE);
    }
}
