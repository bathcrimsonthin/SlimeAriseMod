package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.StonecutterSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.StonecutterSlimeEntity;
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

public class StonecutterSlimeRenderer extends MobRenderer<StonecutterSlimeEntity, StonecutterSlimeModel> {

    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/stonecutter_slime_0.png"),
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/stonecutter_slime_1.png"),
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/stonecutter_slime_2.png"),
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/stonecutter_slime_3.png")
    };

    public StonecutterSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new StonecutterSlimeModel(context.bakeLayer(StonecutterSlimeModel.INNER_LAYER)), 0.25F);
        this.addLayer(new StonecutterSlimeOuterLayer(this, new StonecutterSlimeModel(context.bakeLayer(StonecutterSlimeModel.OUTER_LAYER))));
    }

    @Override
    protected void scale(StonecutterSlimeEntity slime, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) slime.getSize();
        float squish = Mth.lerp(partialTick, slime.oSquish, slime.squish) / (size * 0.5F + 1.0F);
        float inv = 1.0F / (squish + 1.0F);
        poseStack.scale(inv * size, 1.0F / inv * size, inv * size);
    }

    @Override
    public ResourceLocation getTextureLocation(StonecutterSlimeEntity entity) {
        // バニラの石切り台(.mcmeta frametime: 1)と同じ1tickごとの切り替え
        return TEXTURES[Math.floorMod(entity.tickCount, TEXTURES.length)];
    }

    private static class StonecutterSlimeOuterLayer extends RenderLayer<StonecutterSlimeEntity, StonecutterSlimeModel> {

        private final StonecutterSlimeModel outerModel;

        public StonecutterSlimeOuterLayer(RenderLayerParent<StonecutterSlimeEntity, StonecutterSlimeModel> parent,
                                          StonecutterSlimeModel outerModel) {
            super(parent);
            this.outerModel = outerModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           StonecutterSlimeEntity entity, float limbSwing, float limbSwingAmount,
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