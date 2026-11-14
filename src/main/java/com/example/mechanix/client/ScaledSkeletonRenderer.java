package com.example.mechanix.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SkeletonRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.monster.AbstractSkeleton;

/** Модель скелета с собственной текстурой и масштабом. */
public class ScaledSkeletonRenderer extends SkeletonRenderer {
    private final ResourceLocation texture;
    private final float scale;

    public ScaledSkeletonRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture, float scale) {
        super(ctx);
        this.texture = texture;
        this.scale = scale;
    }

    @Override
    public ResourceLocation getTextureLocation(AbstractSkeleton skeleton) {
        return texture;
    }

    @Override
    protected void scale(AbstractSkeleton skeleton, PoseStack stack, float partialTick) {
        stack.scale(scale, scale, scale);
    }
}
