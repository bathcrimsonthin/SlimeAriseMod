package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.SlimeAriseMod;
import com.bathcrimsonthin.slimearisemod.client.SlimeAriseModClient;
import com.bathcrimsonthin.slimearisemod.client.model.RabbitSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.RabbitSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import org.jetbrains.annotations.NotNull;

public class RabbitSlimeRenderer extends MobRenderer<RabbitSlimeEntity, RabbitSlimeModel> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(SlimeAriseMod.MOD_ID, "textures/entity/rabbit_slime.png");

    public RabbitSlimeRenderer(EntityRendererProvider.Context context) {
        super(context, new RabbitSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.RABBIT_SLIME)), 0.25F);
        // 外側の半透明ゼリーレイヤーの追加
        this.addLayer(new RabbitSlimeOuterLayer(this, context));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(RabbitSlimeEntity entity) {
        return TEXTURE;
    }

    @Override
    protected void scale(RabbitSlimeEntity entity, PoseStack poseStack, float partialTick) {
        float size = (float) entity.getSize();
        float squish = Mth.lerp(partialTick, entity.oSquish, entity.squish) / (size * 0.5F + 1.0F);
        float scale = 1.0F / (squish + 1.0F);
        poseStack.scale(scale * size, 1.0F / scale * size, scale * size);
    }

    // --- 外殻レイヤー内部クラス ---
    static class RabbitSlimeOuterLayer extends RenderLayer<RabbitSlimeEntity, RabbitSlimeModel> {
        private final RabbitSlimeModel outerModel;

        public RabbitSlimeOuterLayer(MobRenderer<RabbitSlimeEntity, RabbitSlimeModel> parent, EntityRendererProvider.Context context) {
            super(parent);
            this.outerModel = new RabbitSlimeModel(context.bakeLayer(SlimeAriseModClient.ModModelLayers.RABBIT_SLIME_OUTER));
        }

        @Override
        public void render(PoseStack poseStack, MultiBufferSource buffer, int packedLight, RabbitSlimeEntity entity, float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
            if (!entity.isInvisible()) {
                this.getParentModel().copyPropertiesTo(this.outerModel);
                this.outerModel.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
                var vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
                this.outerModel.renderToBuffer(poseStack, vertexConsumer, packedLight, LivingEntityRenderer.getOverlayCoords(entity, 0.0F));
            }
        }
    }
}