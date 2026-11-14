package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.KingSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.KingSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class KingSlimeRenderer extends MobRenderer<KingSlimeEntity, KingSlimeModel> {

    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/king_slime.png");

    public KingSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new KingSlimeModel(context.bakeLayer(KingSlimeModel.INNER_LAYER)), 0.25F);
        this.addLayer(new KingSlimeOuterLayer(this, new KingSlimeModel(context.bakeLayer(KingSlimeModel.OUTER_LAYER))));
    }

    @Override
    protected void scale(KingSlimeEntity slime, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) slime.getSize();
        float squish = Mth.lerp(partialTick, slime.oSquish, slime.squish) / (size * 0.5F + 1.0F);
        float inv = 1.0F / (squish + 1.0F);
        poseStack.scale(inv * size, 1.0F / inv * size, inv * size);
    }

    @Override
    public ResourceLocation getTextureLocation(KingSlimeEntity entity) {
        return TEXTURE;
    }

    private static class KingSlimeOuterLayer extends RenderLayer<KingSlimeEntity, KingSlimeModel> {

        private final KingSlimeModel outerModel;

        public KingSlimeOuterLayer(RenderLayerParent<KingSlimeEntity, KingSlimeModel> parent, KingSlimeModel outerModel) {
            super(parent);
            this.outerModel = outerModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           KingSlimeEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) {
                return;
            }

            this.getParentModel().copyPropertiesTo(this.outerModel);
            this.outerModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
            this.outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(this.getTextureLocation(entity)));
            this.outerModel.renderToBuffer(
                    poseStack,
                    consumer,
                    packedLight,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                    -1
            );
        }
    }
}