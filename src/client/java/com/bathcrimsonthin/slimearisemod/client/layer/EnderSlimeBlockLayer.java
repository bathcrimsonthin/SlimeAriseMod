package com.bathcrimsonthin.slimearisemod.client.layer;

import com.bathcrimsonthin.slimearisemod.client.model.EnderSlimeModel;
import com.bathcrimsonthin.slimearisemod.entity.EnderSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.color.block.BlockColors;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;

public class EnderSlimeBlockLayer extends RenderLayer<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> {

    private final BlockRenderDispatcher blockRenderer;
    private final BlockColors blockColors;

    public EnderSlimeBlockLayer(RenderLayerParent<EnderSlimeEntity, EnderSlimeModel<EnderSlimeEntity>> context) {
        super(context);
        Minecraft minecraft = Minecraft.getInstance();
        this.blockRenderer = minecraft.getBlockRenderer();
        this.blockColors = minecraft.getBlockColors();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, EnderSlimeEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        BlockState blockState = entity.getCarriedBlock();
        if (blockState == null || blockState.getRenderShape() != RenderShape.MODEL) {
            return;
        }

        poseStack.pushPose();

        // 1. 下半分の中心へ移動 (モデル座標 Y = 20.0F -> 20.0F / 16.0F = 1.25F)
        poseStack.translate(0.0F, 1.25F, 0.0F);

        // 2. コアと同じ大きさ (6 / 16 = 0.375F) に縮小し、X・Y反転を戻す
        float scale = 0.375F;
        poseStack.scale(-scale, -scale, scale);

        // 3. ブロックモデル (0..1) の中心を原点 (0, 0, 0) に合わせる
        poseStack.translate(-0.5F, -0.5F, -0.5F);

        // 草ブロックや葉などのティントカラーを取得
        int color = this.blockColors.getColor(blockState, null, null, 0);
        float red = (float) (color >> 16 & 255) / 255.0F;
        float green = (float) (color >> 8 & 255) / 255.0F;
        float blue = (float) (color & 255) / 255.0F;

        // 外側粘膜 (entityTranslucent) より先に描画されるエンティティ用ブロックアトラスバッファを取得
        BakedModel bakedModel = this.blockRenderer.getBlockModel(blockState);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TextureAtlas.LOCATION_BLOCKS));

        this.blockRenderer.getModelRenderer().renderModel(
                poseStack.last(),
                vertexConsumer,
                blockState,
                bakedModel,
                red,
                green,
                blue,
                packedLight,
                OverlayTexture.NO_OVERLAY
        );

        poseStack.popPose();
    }
}