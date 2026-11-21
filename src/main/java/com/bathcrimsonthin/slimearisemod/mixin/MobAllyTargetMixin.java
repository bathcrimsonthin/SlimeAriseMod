package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.AlliedSlimeHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mob.class)
public abstract class MobAllyTargetMixin {

    @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventAlliedInvalidTarget(@Nullable LivingEntity target, CallbackInfo ci) {
        Mob self = (Mob) (Object) this;
        if (target != null && AlliedSlimeHelper.isAlliedSlime(self)) {
            if (target instanceof Player || AlliedSlimeHelper.shouldProtectTarget(self, target)) {
                ci.cancel();
            }
        }
    }
}