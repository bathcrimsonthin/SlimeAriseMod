package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.ModEntitySpawns;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Slime;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

public final class SlimeStackBehaviorMixin {

    private SlimeStackBehaviorMixin() {
    }

    @Mixin(Mob.class)
    public static abstract class MobStackMixin {

        /**
         * スライムの上に別のスライムが乗っている場合、上のスライムを「操縦者」として扱わないようにする。
         * これにより最下段のスライムが通常通りジャンプ・移動してスタック全体を運べるようになる。
         */
        @Inject(method = "getControllingPassenger", at = @At("HEAD"), cancellable = true)
        private void slimearisemod$allowBottomSlimeToMove(CallbackInfoReturnable<LivingEntity> cir) {
            if ((Object) this instanceof Slime slime && slime.isVehicle()) {
                for (Entity passenger : slime.getPassengers()) {
                    if (passenger instanceof Slime) {
                        cir.setReturnValue(null);
                        return;
                    }
                }
            }
        }

        /**
         * 同じ騎乗スタック内のスライム（乗っている相手・乗られている相手）をターゲットにセットさせない
         */
        @Inject(method = "setTarget", at = @At("HEAD"), cancellable = true)
        private void slimearisemod$ignoreStackedSlimeTarget(@Nullable LivingEntity target, CallbackInfo ci) {
            if ((Object) this instanceof Slime slime && ModEntitySpawns.isSameSlimeStack(slime, target)) {
                ci.cancel();
            }
        }
    }

    @Mixin(LivingEntity.class)
    public static abstract class LivingEntityStackMixin {

        /**
         * NearestAttackableTargetGoal や HurtByTargetGoal などのターゲット探索候補から除外する
         */
        @Inject(method = "canAttack(Lnet/minecraft/world/entity/LivingEntity;)Z", at = @At("HEAD"), cancellable = true)
        private void slimearisemod$cannotAttackStackedSlime(LivingEntity target, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this instanceof Slime slime && ModEntitySpawns.isSameSlimeStack(slime, target)) {
                cir.setReturnValue(false);
            }
        }

        /**
         * 接触攻撃・トゲ反撃・範囲攻撃・搭乗フグの接触ダメージを同一スタック内では無効化する
         */
        @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
        private void slimearisemod$ignoreStackedSlimeDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
            if ((Object) this instanceof Slime slime) {
                Entity attacker = source.getEntity();
                Entity directAttacker = source.getDirectEntity();
                if (ModEntitySpawns.isSameSlimeStack(slime, attacker)
                        || ModEntitySpawns.isSameSlimeStack(slime, directAttacker)) {
                    cir.setReturnValue(false);
                }
            }
        }
    }
}