package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.PufferfishSlimeEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.Pufferfish;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Pufferfish.class)
public abstract class PufferfishMixin {

    @Inject(method = "touch", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventStingingHostSlime(Mob mob, CallbackInfo ci) {
        Pufferfish self = (Pufferfish) (Object) this;
        if (mob instanceof PufferfishSlimeEntity && (self.getVehicle() == mob || self.distanceToSqr(mob) < 4.0D)) {
            ci.cancel();
        }
    }
}