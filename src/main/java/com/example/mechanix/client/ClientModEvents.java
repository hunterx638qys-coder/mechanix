package com.example.mechanix.client;

import com.example.mechanix.MechanixMod;
import com.example.mechanix.registry.ModEntities;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterGuiOverlaysEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/** События шины мода (только клиент). */
@Mod.EventBusSubscriber(modid = MechanixMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvents {

    private static ResourceLocation tex(String name) {
        return new ResourceLocation(MechanixMod.MODID, "textures/entity/" + name + ".png");
    }

    @SubscribeEvent
    public static void keys(RegisterKeyMappingsEvent e) {
        for (var k : KeyBindings.SPELLS) e.register(k);
        e.register(KeyBindings.ABILITY);
        e.register(KeyBindings.MENU);
    }

    @SubscribeEvent
    public static void overlays(RegisterGuiOverlaysEvent e) {
        e.registerAboveAll("rpg_hud", RpgHud::render);
    }

    @SubscribeEvent
    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.<Zombie>registerEntityRenderer(ModEntities.GOBLIN.get(),
                ctx -> new ScaledZombieRenderer(ctx, tex("goblin"), 0.75F));
        e.<Zombie>registerEntityRenderer(ModEntities.ORC.get(),
                ctx -> new ScaledZombieRenderer(ctx, tex("orc"), 1.15F));
        e.<Zombie>registerEntityRenderer(ModEntities.GOBLIN_KING.get(),
                ctx -> new ScaledZombieRenderer(ctx, tex("goblin_king"), 1.3F));
        e.<Zombie>registerEntityRenderer(ModEntities.ORC_WARLORD.get(),
                ctx -> new ScaledZombieRenderer(ctx, tex("orc_warlord"), 1.45F));
        e.<AbstractSkeleton>registerEntityRenderer(ModEntities.SKELETON_KNIGHT.get(),
                ctx -> new ScaledSkeletonRenderer(ctx, tex("skeleton_knight"), 1.0F));
        e.<AbstractSkeleton>registerEntityRenderer(ModEntities.LICH.get(),
                ctx -> new ScaledSkeletonRenderer(ctx, tex("lich"), 1.25F));
        e.registerEntityRenderer(ModEntities.CAVE_STALKER.get(),
                (EntityRendererProvider.Context ctx) -> new StalkerRenderer(ctx, tex("cave_stalker")));
    }
}
