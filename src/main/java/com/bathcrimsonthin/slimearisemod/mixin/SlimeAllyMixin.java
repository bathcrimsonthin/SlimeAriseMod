package com.bathcrimsonthin.slimearisemod.mixin;

import com.bathcrimsonthin.slimearisemod.entity.AlliedSlimeHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;
import java.util.UUID;

@Mixin(Slime.class)
public abstract class SlimeAllyMixin implements AlliedSlimeHelper.AlliedSlimeAccess {

    @Shadow
    protected abstract boolean isDealsDamage();

    @Shadow
    protected abstract void dealDamage(LivingEntity target);

    @Shadow
    protected abstract ParticleOptions getParticleType();

    @Unique
    @Nullable
    private UUID slimearisemod$allyOwnerUuid = null;

    @Unique
    private int slimearisemod$allyLifeTicks = 0;

    @Override
    @Nullable
    public UUID slimearisemod$getAllyOwnerUuid() {
        return this.slimearisemod$allyOwnerUuid;
    }

    @Override
    public void slimearisemod$setAllyOwnerUuid(@Nullable UUID ownerUuid) {
        this.slimearisemod$allyOwnerUuid = ownerUuid;
    }

    @Override
    public int slimearisemod$getAllyLifeTicks() {
        return this.slimearisemod$allyLifeTicks;
    }

    @Override
    public void slimearisemod$setAllyLifeTicks(int ticks) {
        this.slimearisemod$allyLifeTicks = ticks;
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void slimearisemod$onTick(CallbackInfo ci) {
        Slime self = (Slime) (Object) this;
        if (self.level().isClientSide || this.slimearisemod$allyOwnerUuid == null) {
            return;
        }

        LivingEntity currentTarget = self.getTarget();
        if (currentTarget != null && (currentTarget instanceof Player || AlliedSlimeHelper.shouldProtectTarget(self, currentTarget))) {
            self.setTarget(null);
        }

        this.slimearisemod$allyLifeTicks++;
        if (this.slimearisemod$allyLifeTicks >= AlliedSlimeHelper.ALLY_MAX_LIFETIME_TICKS) {
            if (self.level() instanceof ServerLevel serverLevel) {
                ParticleOptions landingParticle = this.getParticleType();
                if (landingParticle == null) {
                    landingParticle = ParticleTypes.ITEM_SLIME;
                }
                int size = Math.max(1, self.getSize());
                int particleCount = 6 + size * 3;
                double spread = 0.15D * size;

                serverLevel.sendParticles(
                        landingParticle,
                        self.getX(),
                        self.getY(0.5D),
                        self.getZ(),
                        particleCount,
                        spread,
                        spread,
                        spread,
                        0.08D
                );
            }
            self.playSound(SoundEvents.SLIME_SQUISH, 0.8F, 1.3F);
            self.discard();
            return;
        }

        if (self.tickCount <= 1 || self.tickCount % 5 == 0) {
            AlliedSlimeHelper.updateAlliedTarget(self, this.slimearisemod$allyOwnerUuid);
        }

        LivingEntity target = self.getTarget();
        if (target != null && target.isAlive() && this.isDealsDamage()
                && !AlliedSlimeHelper.shouldProtectTarget(self, target)) {
            this.dealDamage(target);
        }
    }

    // 味方スライムが死亡して分裂する際、生成された子スライムへ味方情報と残り寿命を引き継ぐ
    @Inject(method = "remove", at = @At("RETURN"))
    private void slimearisemod$onRemoveSplitTransfer(Entity.RemovalReason reason, CallbackInfo ci) {
        Slime self = (Slime) (Object) this;
        if (self.level().isClientSide || this.slimearisemod$allyOwnerUuid == null) {
            return;
        }
        if (self.getSize() > 1 && self.isDeadOrDying()) {
            List<Slime> children = self.level().getEntitiesOfClass(
                    Slime.class,
                    self.getBoundingBox().inflate(2.5D),
                    s -> s != self
                            && s.isAlive()
                            && s.tickCount == 0
                            && s.getType() == self.getType()
                            && !AlliedSlimeHelper.isAlliedSlime(s)
            );
            for (Slime child : children) {
                if (child instanceof AlliedSlimeHelper.AlliedSlimeAccess childAccess) {
                    childAccess.slimearisemod$setAllyOwnerUuid(this.slimearisemod$allyOwnerUuid);
                    childAccess.slimearisemod$setAllyLifeTicks(this.slimearisemod$allyLifeTicks);
                }
            }
        }
    }

    @Inject(method = "push", at = @At("TAIL"))
    private void slimearisemod$onPushEnemy(Entity entity, CallbackInfo ci) {
        Slime self = (Slime) (Object) this;
        if (self.level().isClientSide || this.slimearisemod$allyOwnerUuid == null) {
            return;
        }
        if (!(entity instanceof LivingEntity target) || entity instanceof IronGolem || entity instanceof Player) {
            return;
        }
        if (!this.isDealsDamage() || AlliedSlimeHelper.shouldProtectTarget(self, target)) {
            return;
        }
        if (target == self.getTarget() || target instanceof Enemy) {
            this.dealDamage(target);
        }
    }

    @Inject(method = "playerTouch", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$onPlayerTouch(Player player, CallbackInfo ci) {
        Slime self = (Slime) (Object) this;
        if (AlliedSlimeHelper.shouldProtectTarget(self, player)) {
            ci.cancel();
        }
    }

    @Inject(method = "dealDamage", at = @At("HEAD"), cancellable = true)
    private void slimearisemod$onDealDamage(LivingEntity target, CallbackInfo ci) {
        Slime self = (Slime) (Object) this;
        if (AlliedSlimeHelper.shouldProtectTarget(self, target)) {
            ci.cancel();
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void slimearisemod$onSaveNbt(CompoundTag tag, CallbackInfo ci) {
        if (this.slimearisemod$allyOwnerUuid != null) {
            tag.putUUID("AllyOwnerUUID", this.slimearisemod$allyOwnerUuid);
            tag.putInt("AllyLifeTicks", this.slimearisemod$allyLifeTicks);
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void slimearisemod$onLoadNbt(CompoundTag tag, CallbackInfo ci) {
        if (tag.hasUUID("AllyOwnerUUID")) {
            this.slimearisemod$allyOwnerUuid = tag.getUUID("AllyOwnerUUID");
        }
        if (tag.contains("AllyLifeTicks")) {
            this.slimearisemod$allyLifeTicks = tag.getInt("AllyLifeTicks");
        }
    }
}