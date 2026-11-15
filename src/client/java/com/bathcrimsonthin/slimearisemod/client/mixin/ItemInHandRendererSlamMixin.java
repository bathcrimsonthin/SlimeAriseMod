package com.bathcrimsonthin.slimearisemod.client.mixin;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import com.bathcrimsonthin.slimearisemod.item.SlimySwordItem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererSlamMixin {

    @Inject(method = "renderArmWithItem", at = @At("HEAD"))
    private void slimearisemod$animateFirstPersonSlam(AbstractClientPlayer player, float partialTicks, float pitch,
                                                      InteractionHand hand, float swingProgress, ItemStack stack,
                                                      float equippedProgress, PoseStack poseStack,
                                                      MultiBufferSource buffer, int combinedLight, CallbackInfo ci) {
        if (!(stack.getItem() instanceof SlimySwordItem) || !SlamAnimationHelper.isSlamming(player)) {
            return;
        }
        if (hand != SlamAnimationHelper.getSlamHand(player)) {
            return;
        }

        float progress = SlamAnimationHelper.getSlamProgress(player, partialTicks);
        float reverseGrip = SlamAnimationHelper.getReverseGripFactor(progress);
        float windup = SlamAnimationHelper.getWindupFactor(progress);
        float planted = SlamAnimationHelper.getPlantedFactor(progress);

        HumanoidArm arm = (hand == InteractionHand.MAIN_HAND) ? player.getMainArm() : player.getMainArm().getOpposite();
        float sideSign = (arm == HumanoidArm.RIGHT) ? 1.0F : -1.0F;

        float impactVibe = (progress >= 0.44F && progress <= 0.62F)
                ? Mth.sin((progress - 0.44F) * 150.0F) * 0.015F
                : 0.0F;

        // 1. 両手で構えるため画面中央へ寄せつつ、前方（奥側）へ腕を突き出して地面へ突き立てる
        float xOffset = -0.44F * sideSign * reverseGrip;
        float yOffset = (0.12F * reverseGrip) + (0.48F * windup) - (0.30F * planted) + impactVibe;
        float zOffset = (-0.12F * reverseGrip) - (0.08F * windup) - (0.24F * planted);

        poseStack.translate(xOffset, yOffset, zOffset);

        // 2. 剣先が手前（自分側）に向かないよう、奥側〜真下の地面へ突き刺さる角度に設定
        float pitchAngle = (-132.0F * reverseGrip) + (22.0F * windup) - (4.0F * planted);
        float yawAngle = 12.0F * sideSign * reverseGrip;
        float rollAngle = -16.0F * sideSign * reverseGrip;

        poseStack.mulPose(Axis.XP.rotationDegrees(pitchAngle));
        poseStack.mulPose(Axis.YP.rotationDegrees(yawAngle));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rollAngle));
    }
}