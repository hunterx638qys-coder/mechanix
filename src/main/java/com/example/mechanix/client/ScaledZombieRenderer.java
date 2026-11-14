package com.example.mechanix.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ZombieRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.Zombie;

/** Модель зомби с собственной текстурой и масштабом. */
public class ScaledZombieRenderer extends ZombieRenderer {
    private final ResourceLocation texture;
    private final float scale;

    public ScaledZombieRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture, float scale) {
        super(ctx);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public ResourceLocation getTextureLocation(Zombie zombie) {
        return texture;
    }

    @Override
    protected void scale(Zombie zombie, PoseStack stack, float partialTick) {
        stack.scale(scale, scale, scale);
    }
}
