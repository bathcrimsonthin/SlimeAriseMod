package com.bathcrimsonthin.slimearisemod.client.layer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.EnderSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.EnderSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class EnderSlimeEyesLayer extends RenderLayer<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> {
    private static final RenderType EYES_TEXTURE =
            RenderType.eyes(ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/ender_slime_eyes.png"));

    public EnderSlimeEyesLayer(RenderLayerParent<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EnderSlimeEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        VertexConsumer vertexConsumer = buffer.getBuffer(EYES_TEXTURE);
        this.getParentModel().setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        // コアを描画せず、目と口パーツのみを発光描画
        this.getParentModel().renderEyesToBuffer(poseStack, vertexConsumer, 15728640, OverlayTexture.NO_OVERLAY, -1);
    }
}