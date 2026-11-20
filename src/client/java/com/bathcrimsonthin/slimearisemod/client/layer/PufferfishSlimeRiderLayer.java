package com.bathcrimsonthin.slimearisemod.client.layer;

import com.bathcrimsonthin.slimearisemod.entity.PufferfishSlimeEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.model.SlimeModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.animal.Pufferfish;

public class PufferfishSlimeRiderLayer extends RenderLayer<PufferfishSlimeEntity, SlimeModel<PufferfishSlimeEntity>> {

    private static final float PUFFER_CORE_SCALE = 0.56F;
    private final EntityRenderDispatcher entityRenderDispatcher;

    public PufferfishSlimeRiderLayer(RenderLayerParent<PufferfishSlimeEntity, SlimeModel<PufferfishSlimeEntity>> context,
                                     EntityRenderDispatcher entityRenderDispatcher) {
        super(context);
        this.entityRenderDispatcher = entityRenderDispatcher;
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, PufferfishSlimeEntity entity,
                       float limbSwing, float limbSwingAmount, float partialTick, float ageInTicks, float netHeadYaw, float headPitch) {
        Pufferfish pufferfish = entity.getPassengerPufferfish();
        if (pufferfish == null) {
            return;
        }

        // 膨張段階ごとのモデル中心高さ（足元からの距離）を取得し、常にスライムのコア中心 (Y = 1.25F) に合わせる
        float pufferCenterY = switch (pufferfish.getPuffState()) {
            case 0 -> 0.125F;    // 通常時（高さ 2px の中心）
            case 1 -> 0.28125F;  // 半膨張（高さ 5px の中心）
            default -> 0.375F;   // 最大膨張（高さ 8px の中心）
        };

        poseStack.pushPose();

        // 1. コア中心にフグの中心が重なる位置へ移動
        poseStack.translate(0.0F, 1.25F + pufferCenterY * PUFFER_CORE_SCALE, 0.0F);

        // 2. LivingEntityRenderer の反転・回転を戻す
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        float slimeBodyRot = Mth.rotLerp(partialTick, entity.yBodyRotO, entity.yBodyRot);
        poseStack.mulPose(Axis.YP.rotationDegrees(-(180.0F - slimeBodyRot)));

        // 3. コアサイズ（6px四方）に収まるよう縮小（同時に浮遊上下動の振幅も約半分に抑えられる）
        poseStack.scale(PUFFER_CORE_SCALE, PUFFER_CORE_SCALE, PUFFER_CORE_SCALE);

        // フグの向きをスライムの正面と同期
        float origBodyRot = pufferfish.yBodyRot;
        float origBodyRotO = pufferfish.yBodyRotO;
        float origHeadRot = pufferfish.yHeadRot;
        float origHeadRotO = pufferfish.yHeadRotO;

        pufferfish.yBodyRot = entity.yBodyRot;
        pufferfish.yBodyRotO = entity.yBodyRotO;
        pufferfish.yHeadRot = entity.yHeadRot;
        pufferfish.yHeadRotO = entity.yHeadRotO;

        this.entityRenderDispatcher.setRenderShadow(false);
        try {
            this.entityRenderDispatcher.render(
                    pufferfish,
                    0.0D,
                    0.0D,
                    0.0D,
                    slimeBodyRot,
                    partialTick,
                    poseStack,
                    bufferSource,
                    packedLight
            );
        } finally {
            this.entityRenderDispatcher.setRenderShadow(true);
            pufferfish.yBodyRot = origBodyRot;
            pufferfish.yBodyRotO = origBodyRotO;
            pufferfish.yHeadRot = origHeadRot;
            pufferfish.yHeadRotO = origHeadRotO;
        }

        poseStack.popPose();
    }
}