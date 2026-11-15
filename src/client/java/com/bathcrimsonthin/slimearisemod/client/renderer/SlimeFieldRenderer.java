package com.bathcrimsonthin.slimearisemod.client.renderer;

import com.bathcrimsonthin.slimearisemod.entity.SlimeFieldEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;

public class SlimeFieldRenderer extends EntityRenderer<SlimeFieldEntity> {

    private static final ResourceLocation SLIME_BLOCK_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/block/slime_block.png");
    private static final int ROWS = 10;
    private static final float PIXEL_STEP = 0.25F; // 1ブロックの1/4（4テクセル）単位でカクカクさせる

    public SlimeFieldRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SlimeFieldEntity entity, float entityYaw, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        float age = entity.tickCount + partialTick;
        float remaining = entity.getMaxLifetime() - age;
        if (remaining <= 0.0F) {
            return;
        }

        int seed = entity.getId();
        float sizeVariation = 0.92F + (Math.abs(seed * 31) % 17) * 0.01F;
        float scaleFactor = Mth.clamp(age / 5.0F, 0.2F, 1.0F) * sizeVariation;
        float alphaRatio = Mth.clamp(remaining / 30.0F, 0.0F, 1.0F);

        int innerColor = FastColor.ARGB32.color((int) (195 * alphaRatio), 255, 255, 255);
        int rimColor = FastColor.ARGB32.color((int) (150 * alphaRatio), 230, 255, 230);

        float baseRadius = SlimeFieldEntity.FIELD_RADIUS * scaleFactor;
        float uvScale = 0.5F;

        poseStack.pushPose();
        float yOffset = 0.015F + (Math.abs(seed) % 10) * 0.0015F;
        poseStack.translate(0.0D, yOffset, 0.0D);
        PoseStack.Pose pose = poseStack.last();

        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityTranslucent(SLIME_BLOCK_TEXTURE));

        float rowHeight = (baseRadius * 2.0F) / ROWS;

        for (int row = 0; row < ROWS; row++) {
            float z0 = snapToGrid(-baseRadius + row * rowHeight);
            float z1 = snapToGrid(-baseRadius + (row + 1) * rowHeight);
            if (z1 <= z0) {
                z1 = z0 + PIXEL_STEP;
            }

            // 行の中心位置から基本の横幅を計算し、個体ごとの擬似ランダム凹凸を加える
            float normZ = ((row + 0.5F) / ROWS) * 2.0F - 1.0F;
            float baseHalfWidth = baseRadius * Mth.sqrt(Math.max(0.05F, 1.0F - normZ * normZ * 0.88F));

            int leftJitterSteps = ((Math.abs(seed * 17 + row * 31) % 3) - 1);   // -1, 0, +1 ステップ
            int rightJitterSteps = ((Math.abs(seed * 23 + row * 47) % 3) - 1);  // -1, 0, +1 ステップ

            float minX = snapToGrid(-baseHalfWidth + leftJitterSteps * PIXEL_STEP);
            float maxX = snapToGrid(baseHalfWidth + rightJitterSteps * PIXEL_STEP);

            if (maxX <= minX + PIXEL_STEP * 2.0F) {
                minX = -PIXEL_STEP;
                maxX = PIXEL_STEP;
            }

            // 上下の端行はフチ色、中間行は中央と左右のフチを塗り分けて立体感を出す
            if (row == 0 || row == ROWS - 1) {
                addQuad(consumer, pose, minX, maxX, z0, z1, rimColor, uvScale, packedLight);
            } else {
                float innerMinX = minX + PIXEL_STEP;
                float innerMaxX = maxX - PIXEL_STEP;
                if (innerMaxX > innerMinX) {
                    addQuad(consumer, pose, minX, innerMinX, z0, z1, rimColor, uvScale, packedLight);
                    addQuad(consumer, pose, innerMinX, innerMaxX, z0, z1, innerColor, uvScale, packedLight);
                    addQuad(consumer, pose, innerMaxX, maxX, z0, z1, rimColor, uvScale, packedLight);
                } else {
                    addQuad(consumer, pose, minX, maxX, z0, z1, rimColor, uvScale, packedLight);
                }
            }
        }

        poseStack.popPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
    }

    private static float snapToGrid(float value) {
        return Math.round(value / PIXEL_STEP) * PIXEL_STEP;
    }

    private static void addQuad(VertexConsumer consumer, PoseStack.Pose pose,
                                float minX, float maxX, float minZ, float maxZ,
                                int color, float uvScale, int packedLight) {
        consumer.addVertex(pose, minX, 0.0F, minZ).setColor(color).setUv(minX * uvScale, minZ * uvScale).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, minX, 0.0F, maxZ).setColor(color).setUv(minX * uvScale, maxZ * uvScale).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, maxX, 0.0F, maxZ).setColor(color).setUv(maxX * uvScale, maxZ * uvScale).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
        consumer.addVertex(pose, maxX, 0.0F, minZ).setColor(color).setUv(maxX * uvScale, minZ * uvScale).setOverlay(OverlayTexture.NO_OVERLAY).setLight(packedLight).setNormal(pose, 0.0F, 1.0F, 0.0F);
    }

    @Override
    public ResourceLocation getTextureLocation(SlimeFieldEntity entity) {
        return SLIME_BLOCK_TEXTURE;
    }
}