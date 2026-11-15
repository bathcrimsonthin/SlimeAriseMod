package com.bathcrimsonthin.slimearisemod.client.mixin;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HumanoidModel.class)
public abstract class HumanoidModelSlamMixin<T extends LivingEntity> {

    @Shadow @Final public ModelPart head;
    @Shadow @Final public ModelPart body;
    @Shadow @Final public ModelPart rightArm;
    @Shadow @Final public ModelPart leftArm;
    @Shadow @Final public ModelPart rightLeg;
    @Shadow @Final public ModelPart leftLeg;

    @Inject(method = "setupAnim(Lnet/minecraft/world/entity/LivingEntity;FFFFF)V", at = @At("TAIL"))
    private void slimearisemod$animateThirdPersonSlam(T entity, float limbSwing, float limbSwingAmount,
                                                      float ageInTicks, float netHeadYaw, float headPitch,
                                                      CallbackInfo ci) {
        if (!(entity instanceof Player player) || !SlamAnimationHelper.isSlamming(player)) {
            return;
        }

        float partialTick = ageInTicks - entity.tickCount;
        float progress = SlamAnimationHelper.getSlamProgress(player, partialTick);
        float reverseGrip = SlamAnimationHelper.getReverseGripFactor(progress);
        float windup = SlamAnimationHelper.getWindupFactor(progress);
        float planted = SlamAnimationHelper.getPlantedFactor(progress);

        // 1. 体をかがめる（腰を落とし、上体を前傾させる）
        if (planted > 0.0F) {
            this.head.y = Mth.lerp(planted, this.head.y, 4.4F);
            this.body.y = Mth.lerp(planted, this.body.y, 3.3F);
            this.rightArm.y = Mth.lerp(planted, this.rightArm.y, 5.3F);
            this.leftArm.y = Mth.lerp(planted, this.leftArm.y, 5.3F);

            this.rightLeg.z = Mth.lerp(planted, this.rightLeg.z, 4.0F);
            this.leftLeg.z = Mth.lerp(planted, this.leftLeg.z, 4.0F);
            this.rightLeg.y = Mth.lerp(planted, this.rightLeg.y, 11.6F);
            this.leftLeg.y = Mth.lerp(planted, this.leftLeg.y, 11.6F);

            this.rightLeg.xRot = Mth.lerp(planted, this.rightLeg.xRot, -0.32F);
            this.leftLeg.xRot = Mth.lerp(planted, this.leftLeg.xRot, 0.22F);
        }

        // 上体と頭の角度（振りかぶり時は軽く反らし、突き立て時は前傾）
        float targetBodyX = (-0.18F * windup) + (0.48F * planted);
        this.body.xRot = Mth.lerp(Math.max(windup, planted), this.body.xRot, targetBodyX);
        this.head.xRot += (-0.32F * windup) + (0.30F * planted);

        // 2. 両手を中央へ寄せて同じ柄を握り、頭上へ振りかぶってから前方の地面へ突き立てる
        float twoHandAlpha = Math.max(reverseGrip, Math.max(windup, planted));

        // 腕を前方へしっかり突き出す(-1.12rad)ことで、直角に構えた剣先が地面に垂直に刺さる
        float targetArmX = Mth.lerp(windup, -1.25F, -2.85F);
        targetArmX = Mth.lerp(planted, targetArmX, -1.12F);

        float targetInwardY = Mth.lerp(planted, 0.42F, 0.46F);
        float targetRollZ = Mth.lerp(windup, 0.08F, 0.14F);

        this.rightArm.xRot = Mth.lerp(twoHandAlpha, this.rightArm.xRot, targetArmX);
        this.leftArm.xRot = Mth.lerp(twoHandAlpha, this.leftArm.xRot, targetArmX);

        this.rightArm.yRot = Mth.lerp(twoHandAlpha, this.rightArm.yRot, -targetInwardY);
        this.leftArm.yRot = Mth.lerp(twoHandAlpha, this.leftArm.yRot, targetInwardY);

        this.rightArm.zRot = Mth.lerp(twoHandAlpha, this.rightArm.zRot, targetRollZ);
        this.leftArm.zRot = Mth.lerp(twoHandAlpha, this.leftArm.zRot, -targetRollZ);
    }
}