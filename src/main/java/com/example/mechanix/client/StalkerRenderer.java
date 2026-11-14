package com.example.mechanix.client;

import com.example.mechanix.entity.CaveStalkerEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.SpiderRenderer;
import net.minecraft.resources.ResourceLocation;

/** Модель паука с собственной текстурой. */
public class StalkerRenderer extends SpiderRenderer<CaveStalkerEntity> {
    private final ResourceLocation texture;

    public StalkerRenderer(EntityRendererProvider.Context ctx, ResourceLocation texture) {
        super(ctx);
        this.texture = texture;
    }

    @Override
    public ResourceLocation getTextureLocation(CaveStalkerEntity entity) {
        return texture;
    }
}
