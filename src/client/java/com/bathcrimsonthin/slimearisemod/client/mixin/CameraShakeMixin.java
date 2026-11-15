package com.bathcrimsonthin.slimearisemod.client.mixin;

import com.bathcrimsonthin.slimearisemod.client.SlamCameraShake;
import net.minecraft.client.Camera;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.BlockGetter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraShakeMixin {

    @Shadow private float yRot;
    @Shadow private float xRot;

    @Shadow protected abstract void setRotation(float yRot, float xRot);
    @Shadow protected abstract void move(float zoom, float dy, float dx);

    @Inject(method = "setup", at = @At("TAIL"))
    private void slimearisemod$applyCameraShake(BlockGetter level, Entity entity, boolean detached,
                                                boolean thirdPersonReverse, float partialTick, CallbackInfo ci) {
        float shakeStrength = SlamCameraShake.getCurrentStrength();
        float crouchDip = detached ? 0.0F : SlamCameraShake.getCrouchDipOffset(entity, partialTick);

        if (shakeStrength > 0.0F || crouchDip != 0.0F) {
            if (shakeStrength > 0.0F) {
                this.setRotation(
                        this.yRot + SlamCameraShake.getYawOffset(),
                        this.xRot + SlamCameraShake.getPitchOffset()
                );
            }
            this.move(0.0F, SlamCameraShake.getVerticalOffset() + crouchDip, 0.0F);
        }
    }
}