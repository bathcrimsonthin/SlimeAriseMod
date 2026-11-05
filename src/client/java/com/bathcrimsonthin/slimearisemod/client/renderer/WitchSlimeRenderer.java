package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.SlimeAriseModClient;
import com.bathcrimsonthin.slimearisemod.client.model.WitchSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.WitchSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class WitchSlimeRenderer extends MobRenderer<WitchSlimeEntity, WitchSlimeModel> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/witch_slime.png");

    public WitchSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new WitchSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.WITCH_SLIME_INNER)), 0.25F);

        // 2層目（外殻ゼリー）の描画レイヤー追加
        this.addLayer(new RenderLayer<WitchSlimeEntity, WitchSlimeModel>(this) {
            private final WitchSlimeModel outerModel = new WitchSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.WITCH_SLIME_OUTER));

            @Override
            public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, WitchSlimeEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
                if (!entity.isInvisible()) {
                    this.getParentModel().copyPropertiesTo(this.outerModel);
                    this.outerModel.prepareMobModel(entity, limbSwing, limbSwingAmount, partialTick);
                    this.outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                    var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));

                    // 1.21+: 引数の float 4つを int カラー (-1 = 0xFFFFFFFF) に変更
                    this.outerModel.renderToBuffer(
                            poseStack,
                            vertexConsumer,
                            packedLight,
                            LivingEntityRenderer.getOverlayCoords(entity, 0.0F),
                            -1
                    );
                }
            }
        });
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
}