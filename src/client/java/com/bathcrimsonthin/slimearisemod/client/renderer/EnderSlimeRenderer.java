package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.EnderSlimeModel;
import com.bathcrimsonthin.slimearisemod.client.layer.EnderSlimeBlockLayer;
import com.bathcrimsonthin.slimearisemod.client.layer.EnderSlimeEyesLayer;
import com.bathcrimsonthin.slimearisemod.client.layer.EnderSlimeOuterLayer;
import com.bathcrimsonthin.slimearisemod.entity.EnderSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class EnderSlimeRenderer extends MobRenderer<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/ender_slime.png");

    public EnderSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new EnderSlimeModel<>(context.bakeLayer(EnderSlimeModel.LAYER_LOCATION)), 0.25F);
        this.addLayer(new EnderSlimeEyesLayer(this));
        this.addLayer(new EnderSlimeBlockLayer(this)); // 先に内部ブロックを描画
        this.addLayer(new EnderSlimeOuterLayer(this)); // 最後に半透明の外側粘膜を描画
    }

    @Override
    protected void scale(EnderSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);

        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float stretch = 1.0F / (squish + 1.0F);

        poseStack.scale(stretch * size, 1.0F / stretch * size, stretch * size);
    }

    @Override
    public ResourceLocation getTextureLocation(EnderSlimeEntity entity) {
        return TEXTURE;
    }
}