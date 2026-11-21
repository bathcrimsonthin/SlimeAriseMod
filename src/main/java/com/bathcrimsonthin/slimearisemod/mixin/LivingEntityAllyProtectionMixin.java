package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.AlliedSlimeHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityAllyProtectionMixin {

    @Inject(method = "canAttack", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventAlliedCanAttack(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (AlliedSlimeHelper.isAlliedSlime(self)) {
            if (target instanceof Player || AlliedSlimeHelper.shouldProtectTarget(self, target)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventAlliedSlimeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity) (Object) this;
        Entity attacker = source.getEntity();
        Entity direct = source.getDirectEntity();

        if (attacker != null && (AlliedSlimeHelper.shouldProtectTarget(attacker, victim)
                || AlliedSlimeHelper.shouldProtectTarget(victim, attacker))) {
            cir.setReturnValue(false);
            return;
        }
        if (direct != null && (AlliedSlimeHelper.shouldProtectTarget(direct, victim)
                || AlliedSlimeHelper.shouldProtectTarget(victim, direct))) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "canBeAffected", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventAlliedHarmfulEffects(MobEffectInstance effect, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity victim = (LivingEntity) (Object) this;
        if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL
                && AlliedSlimeHelper.isAlliedSlime(victim)) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "doPush", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$preventAlliedPush(Entity other, CallbackInfo ci) {
        LivingEntity self = (LivingEntity) (Object) this;
        if (AlliedSlimeHelper.shouldProtectTarget(other, self) || AlliedSlimeHelper.shouldProtectTarget(self, other)) {
            ci.cancel();
        }
    }
}