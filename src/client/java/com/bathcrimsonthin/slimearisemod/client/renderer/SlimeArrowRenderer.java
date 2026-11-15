package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.entity.SlimeArrowEntity;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class SlimeArrowRenderer extends ArrowRenderer<SlimeArrowEntity> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath("slimearisemod", "textures/entity/slime_arrow.png");

    public SlimeArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeArrowEntity entity) {
        return TEXTURE;
    }
}