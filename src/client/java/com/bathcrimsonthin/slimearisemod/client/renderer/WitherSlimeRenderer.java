package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.model.WitherSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.WitherSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.SkullModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WitherSlimeRenderer extends MobRenderer<WitherSlimeEntity, WitherSlimeModel> {

    private static final ResourceLocation SLIME_LOCATION =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/wither_slime.png");
    private static final ResourceLocation WITHER_SKELETON_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/entity/skeleton/wither_skeleton.png");
    private static final ResourceLocation WITHER_ARMOR_LOCATION =
            ResourceLocation.withDefaultNamespace("textures/entity/wither/wither_armor.png");

    public WitherSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new WitherSlimeModel(context.bakeLayer(WitherSlimeModel.INNER_LAYER)), 0.25F);
        // 1. コアサイズのウィザースケルトン頭を描画
        this.addLayer(new WitherSkullCoreLayer(this, new SkullModel(context.bakeLayer(ModelLayers.WITHER_SKELETON_SKULL))));
        // 2. 半透明の外皮を描画
        this.addLayer(new WitherSlimeOuterLayer(this, new WitherSlimeModel(context.bakeLayer(WitherSlimeModel.OUTER_LAYER))));
        // 3. HP半分以下でウィザーアーマーの青いオーラを描画
        this.addLayer(new WitherSlimeArmorLayer(this, new WitherSlimeModel(context.bakeLayer(WitherSlimeModel.ARMOR_LAYER))));
    }

    @Override
    protected void scale(WitherSlimeEntity slime, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) slime.getSize();
        float squish = Mth.lerp(partialTick, slime.oSquish, slime.squish) / (size * 0.5F + 1.0F);
        float inv = 1.0F / (squish + 1.0F);
        poseStack.scale(inv * size, 1.0F / inv * size, inv * size);
    }

    @Override
    public ResourceLocation getTextureLocation(WitherSlimeEntity entity) {
        return SLIME_LOCATION;
    }

    // 8x8x8 のウィザースケルトン頭を 0.75倍 (6x6x6) にしてコアの座標に描画するレイヤー
    private static class WitherSkullCoreLayer extends RenderLayer<WitherSlimeEntity, WitherSlimeModel> {

        private final SkullModel skullModel;

        public WitherSkullCoreLayer(RenderLayerParent<WitherSlimeEntity, WitherSlimeModel> parent, SkullModel skullModel) {
            super(parent);
            this.skullModel = skullModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           WitherSlimeEntity entity, float limbSwing, float limbSwingAmount,
                           float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (entity.isInvisible()) {
                return;
            }

            poseStack.pushPose();
            poseStack.translate(0.0F, 1.4375F, 0.0F);
            poseStack.scale(0.75F, 0.75F, 0.75F);

            this.skullModel.setupAnim(0.0F, 0.0F, 0.0F);
            VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(WITHER_SKELETON_LOCATION));
            this.skullModel.renderToBuffer(
                    poseStack,
                    consumer,
                    packedLight,
                    LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                    -1
            );
            poseStack.popPose();
        }
    }

    // 半透明の外皮レイヤー
    private static class WitherSlimeOuterLayer extends RenderLayer<WitherSlimeEntity, WitherSlimeModel> {

        private final WitherSlimeModel outerModel;

        public WitherSlimeOuterLayer(RenderLayerParent<WitherSlimeEntity, WitherSlimeModel> parent, WitherSlimeModel outerModel) {
            super(parent);
            this.outerModel = outerModel;
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight,
                           WitherSlimeEntity entity, float limbSwing, float limbSwingAmount,
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

    // HP半分以下で発動するウィザーアーマーオーラ
    private static class WitherSlimeArmorLayer extends EnergySwirlLayer<WitherSlimeEntity, WitherSlimeModel> {

        private final WitherSlimeModel armorModel;

        public WitherSlimeArmorLayer(RenderLayerParent<WitherSlimeEntity, WitherSlimeModel> parent, WitherSlimeModel armorModel) {
            super(parent);
            this.armorModel = armorModel;
        }

        @Override
        protected float xOffset(float tickCount) {
            return Mth.cos(tickCount * 0.02F) * 3.0F;
        }

        @Override
        protected ResourceLocation getTextureLocation() {
            return WITHER_ARMOR_LOCATION;
        }

        @Override
        protected EntityModel<WitherSlimeEntity> model() {
            return this.armorModel;
        }
    }
}