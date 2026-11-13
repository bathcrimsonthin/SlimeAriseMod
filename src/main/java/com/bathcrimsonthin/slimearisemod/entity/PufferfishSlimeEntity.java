package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundGameEventPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.animal.Pufferfish;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PufferfishSlimeEntity extends Slime {

    private boolean spawnedRider = false;

    public PufferfishSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    // 搭乗中のフグに操縦権を渡さず、スライム自身のAIで移動・ジャンプさせる
    @Nullable
    @Override
    public LivingEntity getControllingPassenger() {
        return null;
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide) {
            // 初回ティック時に内部へフグを搭乗させる（スポーンエッグ・コマンド・分裂後の子スライムすべてに対応）
            if (!this.spawnedRider && this.isAlive()) {
                this.spawnedRider = true;
                Pufferfish pufferfish = EntityType.PUFFERFISH.create(this.level());
                if (pufferfish != null) {
                    pufferfish.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    this.level().addFreshEntity(pufferfish);
                    pufferfish.startRiding(this, true);
                }
            }

            // 搭乗中のフグの窒息防止および近接毒攻撃処理
            if (this.getFirstPassenger() instanceof Pufferfish pufferfish) {
                pufferfish.setAirSupply(pufferfish.getMaxAirSupply());

                int puffState = pufferfish.getPuffState();
                if (puffState > 0) {
                    this.applyPufferfishStingAround(pufferfish, puffState);
                }
            }
        }
    }

    // 近接攻撃で間合いを取れば回避できつつ、近づきすぎると毒を受ける範囲に調整
    private void applyPufferfishStingAround(Pufferfish pufferfish, int puffState) {
        double stingReach = 0.25D + 0.20D * this.getSize() + 0.10D * puffState;

        List<LivingEntity> targets = this.level().getEntitiesOfClass(
                LivingEntity.class,
                this.getBoundingBox().inflate(stingReach),
                entity -> entity != this
                        && entity != pufferfish
                        && !(entity instanceof PufferfishSlimeEntity)
                        && entity.isAlive()
                        && this.hasLineOfSight(entity)
        );

        for (LivingEntity target : targets) {
            if (target instanceof Player player) {
                if (!player.isCreative() && !player.isSpectator() && !player.isInvulnerable()) {
                    if (player.hurt(this.damageSources().mobAttack(pufferfish), (float) (1 + puffState))) {
                        if (player instanceof ServerPlayer serverPlayer && !pufferfish.isSilent()) {
                            serverPlayer.connection.send(new ClientboundGameEventPacket(ClientboundGameEventPacket.PUFFER_FISH_STING, 0.0F));
                        }
                        player.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0), pufferfish);
                    }
                }
            } else if (target instanceof Mob mob && !mob.getType().is(EntityTypeTags.AQUATIC)) {
                if (mob.hurt(this.damageSources().mobAttack(pufferfish), (float) (1 + puffState))) {
                    mob.addEffect(new MobEffectInstance(MobEffects.POISON, 60 * puffState, 0), pufferfish);
                    pufferfish.playSound(SoundEvents.PUFFER_FISH_STING, 1.0F, 1.0F);
                }
            }
        }
    }

    // 中サイズ・大サイズが倒されたときは、分裂時に親のフグが残らないよう消去する
    @Override
    public void die(DamageSource damageSource) {
        if (!this.level().isClientSide && !this.isTiny()) {
            this.discardPassengerPufferfish();
        }
        super.die(damageSource);
    }

    @Override
    public void remove(Entity.RemovalReason reason) {
        if (!this.level().isClientSide && reason == Entity.RemovalReason.KILLED && !this.isTiny()) {
            this.discardPassengerPufferfish();
        }
        super.remove(reason);
    }

    private void discardPassengerPufferfish() {
        for (Entity passenger : List.copyOf(this.getPassengers())) {
            if (passenger instanceof Pufferfish) {
                passenger.discard();
            }
        }
    }

    // 搭乗位置をスライムの内部中央に設定
    @Override
    protected Vec3 getPassengerAttachmentPoint(Entity passenger, EntityDimensions dimensions, float partialTick) {
        double yOffset = Math.max(0.05D, (dimensions.height() - passenger.getBbHeight()) * 0.5D);
        return new Vec3(0.0D, yOffset, 0.0D);
    }

    // 内部のフグからのダメージや毒を無効化
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Pufferfish) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().equals(MobEffects.POISON) && this.getFirstPassenger() instanceof Pufferfish) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putBoolean("SpawnedRider", this.spawnedRider);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.spawnedRider = compound.getBoolean("SpawnedRider");
    }
}