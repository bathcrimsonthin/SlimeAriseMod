package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.SlamAnimationHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntitySlamLockMixin {

    @ModifyVariable(method = "travel", at = @At("HEAD"), argsOnly = true)
    private Vec3 slimearisemod$lockMovementDuringSlam(Vec3 travelVector) {
        if ((Object) this instanceof Player player && SlamAnimationHelper.isSlamming(player)) {
            Vec3 current = player.getDeltaMovement();
            player.setDeltaMovement(0.0D, Math.min(current.y, 0.0D), 0.0D);
            return Vec3.ZERO;
        }
        return travelVector;
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventJumpDuringSlam(CallbackInfo ci) {
        if ((Object) this instanceof Player player && SlamAnimationHelper.isSlamming(player)) {
            ci.cancel();
        }
    }
}