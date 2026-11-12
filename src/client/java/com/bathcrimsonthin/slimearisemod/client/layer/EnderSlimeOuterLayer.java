package com.bathcrimsonthin.slimearisemod.client.layer;

import com.bathcrimsonthin.slimearisemod.client.model.EnderSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.EnderSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;

public class EnderSlimeOuterLayer extends RenderLayer<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> {

    public EnderSlimeOuterLayer(RenderLayerParent<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, EnderSlimeEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean glowing = minecraft.shouldEntityAppearGlowing(entity) && entity.isInvisible();

        if (!entity.isInvisible() || glowing) {
            VertexConsumer vertexConsumer;
            if (glowing) {
                vertexConsumer = buffer.getBuffer(RenderType.outline(this.getTextureLocation(entity)));
            } else {
                vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
            }

            this.getParentModel().setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.getParentModel().renderOuterToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F), -1);
        }
    }
}