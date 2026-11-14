package com.example.mechanix.registry;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.entity.CaveStalkerEntity;
import com.example.mechanix.entity.GoblinEntity;
import com.example.mechanix.entity.GoblinKingEntity;
import com.example.mechanix.entity.LichEntity;
import com.example.mechanix.entity.OrcEntity;
import com.example.mechanix.entity.OrcWarlordEntity;
import com.example.mechanix.entity.SkeletonKnightEntity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITIES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MechanixMod.MODID);

    public static final RegistryObject<EntityType<GoblinEntity>> GOBLIN = ENTITIES.register("goblin",
            () -> EntityType.Builder.of(GoblinEntity::new, MobCategory.MONSTER)
                    .sized(0.45F, 1.46F).clientTrackingRange(8).build("goblin"));

    public static final RegistryObject<EntityType<OrcEntity>> ORC = ENTITIES.register("orc",
            () -> EntityType.Builder.of(OrcEntity::new, MobCategory.MONSTER)
                    .sized(0.7F, 2.25F).clientTrackingRange(8).build("orc"));

    public static final RegistryObject<EntityType<SkeletonKnightEntity>> SKELETON_KNIGHT = ENTITIES.register("skeleton_knight",
            () -> EntityType.Builder.of(SkeletonKnightEntity::new, MobCategory.MONSTER)
                    .sized(0.6F, 1.99F).clientTrackingRange(8).build("skeleton_knight"));

    public static final RegistryObject<EntityType<CaveStalkerEntity>> CAVE_STALKER = ENTITIES.register("cave_stalker",
            () -> EntityType.Builder.of(CaveStalkerEntity::new, MobCategory.MONSTER)
                    .sized(1.4F, 0.9F).clientTrackingRange(8).build("cave_stalker"));

    public static final RegistryObject<EntityType<GoblinKingEntity>> GOBLIN_KING = ENTITIES.register("goblin_king",
            () -> EntityType.Builder.of(GoblinKingEntity::new, MobCategory.MONSTER)
                    .sized(0.8F, 2.5F).clientTrackingRange(10).build("goblin_king"));

    public static final RegistryObject<EntityType<OrcWarlordEntity>> ORC_WARLORD = ENTITIES.register("orc_warlord",
            () -> EntityType.Builder.of(OrcWarlordEntity::new, MobCategory.MONSTER)
                    .sized(0.9F, 2.9F).clientTrackingRange(10).build("orc_warlord"));

    public static final RegistryObject<EntityType<LichEntity>> LICH = ENTITIES.register("lich",
            () -> EntityType.Builder.of(LichEntity::new, MobCategory.MONSTER)
                    .sized(0.75F, 2.5F).clientTrackingRange(10).build("lich"));
}
