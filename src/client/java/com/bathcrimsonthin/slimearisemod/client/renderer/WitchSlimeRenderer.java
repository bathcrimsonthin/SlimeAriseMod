package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.SlimeAriseModClient;
import com.bathcrimsonthin.slimearisemod.client.model.WitchSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.WitchSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WitchSlimeRenderer extends MobRenderer<WitchSlimeEntity, WitchSlimeModel> {
    public static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/witch_slime.png");

    public WitchSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.WITCH_SLIME_INNER)), 0.25F);

        // 外殻（ゼリー部分）レイヤーの追加（体色カラーを適用）
        this.addLayer(new RenderLayer<WitchSlimeEntity, WitchSlimeModel>(this) {
            private final WitchSlimeModel outerModel = new WitchSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.WITCH_SLIME_OUTER));

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WitchSlimeEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (!entity.isInvisible()) {
                    this.getParentModel().copyPropertiesTo(this.outerModel);
                    this.outerModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
                    this.outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

                    int color = entity.getTintedColor();
                    this.outerModel.renderToBuffer(
                            poseStack,
                            vertexConsumer,
                            packedLight,
                            LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                            color
                    );
                }
            }
        });
    }

    @Override
    public void render(WitchSlimeEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int tintColor = entity.getTintedColor();

        // ポーション醸造中（カラー指定あり）の場合はバッファーをラップして着色
        if (tintColor != -1) {
            float r = ((tintColor >> 16) & 0xFF) / 255.0F;
            float g = ((tintColor >> 8) & 0xFF) / 255.0F;
            float b = (tintColor & 0xFF) / 255.0F;

            MultiBufferSource tintedBuffer = renderType -> new TintedVertexConsumer(buffer.getBuffer(renderType), r, g, b);
            super.render(entity, entityYaw, partialTick, poseStack, tintedBuffer, packedLight);
        } else {
            super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
        }
    }

    @Override
    protected void scale(WitchSlimeEntity entity, PoseStack poseStack, float partialTick) {
        poseStack.scale(0.999F, 0.999F, 0.999F);
        poseStack.translate(0.0F, 0.001F, 0.0F);
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float scaleFactor = 1.0F / (squish + 1.0F);
        poseStack.scale(scaleFactor * size, 1.0F / scaleFactor * size, scaleFactor * size);
    }

    @Override
    public ResourceLocation getTextureLocation(WitchSlimeEntity entity) {
        return TEXTURE;
    }

    // 描画色をポーションカラーで乗算補正する VertexConsumer ラッパー
    private static class TintedVertexConsumer implements VertexConsumer {
        private final VertexConsumer parent;
        private final float tr, tg, tb;

        public TintedVertexConsumer(VertexConsumer parent, float tr, float tg, float tb) {
            this.parent = parent;
            this.tr = tr;
            this.tg = tg;
            this.tb = tb;
        }

        @Override
        public VertexConsumer addVertex(float x, float y, float z) {
            this.parent.addVertex(x, y, z);
            return this;
        }

        @Override
        public VertexConsumer setColor(int r, int g, int b, int a) {
            this.parent.setColor((int) (r * this.tr), (int) (g * this.tg), (int) (b * this.tb), a);
            return this;
        }

        @Override
        public VertexConsumer setUv(float u, float v) {
            this.parent.setUv(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv1(int u, int v) {
            this.parent.setUv1(u, v);
            return this;
        }

        @Override
        public VertexConsumer setUv2(int u, int v) {
            this.parent.setUv2(u, v);
            return this;
        }

        @Override
        public VertexConsumer setNormal(float x, float y, float z) {
            this.parent.setNormal(x, y, z);
            return this;
        }
    }
}