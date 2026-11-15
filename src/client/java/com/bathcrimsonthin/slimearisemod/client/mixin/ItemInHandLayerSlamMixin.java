package com.bathcrimsonthin.slimearisemod.client.mixin;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import com.bathcrimsonthin.slimearisemod.item.SlimySwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandLayer.class)
public abstract class ItemInHandLayerSlamMixin {

    @Inject(
            method = "renderArmWithItem",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/client/renderer/ItemInHandRenderer;renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V"
            )
    )
    private void slimearisemod$reverseGripSwordInThirdPerson(LivingEntity livingEntity, ItemStack itemStack,
                                                             ItemDisplayContext displayContext, HumanoidArm arm,
                                                             PoseStack poseStack, MultiBufferSource buffer,
                                                             int packedLight, CallbackInfo ci) {
        if (!(livingEntity instanceof Player player) || !SlamAnimationHelper.isSlamming(player)) {
            return;
        }
        if (!(itemStack.getItem() instanceof SlimySwordItem) || arm != SlamAnimationHelper.getSlamArm(player)) {
            return;
        }

        int ticks = SlamAnimationHelper.getSlamTicks(player);
        float approxProgress = 1.0F - ((float) ticks / SlamAnimationHelper.SLAM_TOTAL_TICKS);
        float reverseGrip = SlamAnimationHelper.getReverseGripFactor(approxProgress);
        float planted = SlamAnimationHelper.getPlantedFactor(approxProgress);

        float sideSign = (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;

        if (reverseGrip > 0.0F) {
            // 両手の中央に柄を配置し、腕に対して刃が約85〜90度で下を向く逆手持ちに回転
            poseStack.translate(-0.09D * sideSign * reverseGrip, 0.08D * reverseGrip, 0.06D * reverseGrip);
            poseStack.mulPose(Axis.XP.rotationDegrees(208.0F * reverseGrip));
            poseStack.mulPose(Axis.ZP.rotationDegrees(-14.0F * sideSign * reverseGrip));
        }

        if (planted > 0.0F) {
            // 突き立て時は前方の地面へ垂直に刺さる角度（腕とほぼ90度）へ微調整
            poseStack.translate(0.0D, 0.05D * planted, 0.04D * planted);
            poseStack.mulPose(Axis.XP.rotationDegrees(-6.0F * planted));
        }
    }
}