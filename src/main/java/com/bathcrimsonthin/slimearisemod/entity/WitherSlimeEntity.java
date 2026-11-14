package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PowerableMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.WitherSkull;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class WitherSlimeEntity extends Slime implements PowerableMob {

    private int skullCooldown = 60;
    private boolean wasOnGroundLastTick = true;

    public WitherSlimeEntity(EntityType<? extends Slime> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes();
    }

    // 着地時のスライムパーティクルを WitherSlimeBall に差し替える
    @Override
    protected ParticleOptions getParticleType() {
        return new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.WITHER_SLIME_BALL));
    }

    // HPが最大値の半分以下になるとウィザーアーマー状態になる
    @Override
    public boolean isPowered() {
        return this.getHealth() <= this.getMaxHealth() * 0.5F;
    }

    // 攻撃力は通常のスライムの2倍
    @Override
    protected float getAttackDamage() {
        return super.getAttackDamage() * 2.0F;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.spawnSmokeParticles();
        } else if (this.isAlive() && !this.isTiny()) {
            // 一番小さいサイズ（isTiny）以外のときのみウィザースカルを発射する
            if (this.skullCooldown > 0) {
                this.skullCooldown--;
            }

            LivingEntity target = this.getTarget();
            if (target != null && target.isAlive() && this.skullCooldown <= 0) {
                double distSqr = this.distanceToSqr(target);
                int size = this.getSize();
                double minRange = 1.5D * size;

                if (distSqr >= minRange * minRange && distSqr <= 256.0D && this.hasLineOfSight(target)) {
                    this.shootWitherSkull(target);
                    this.skullCooldown = 70 + this.random.nextInt(50);
                }
            }
        }
    }

    // クライアント側で黒い煙パーティクルを発生させる（跳躍・着地時は多めに放出）
    private void spawnSmokeParticles() {
        if (!this.isAlive() || this.isInvisible()) {
            return;
        }

        int size = this.getSize();
        boolean justLandedOrJumped = this.onGround() != this.wasOnGroundLastTick;
        this.wasOnGroundLastTick = this.onGround();

        int count = justLandedOrJumped ? 4 * size : (this.tickCount % 3 == 0 ? 1 : 0);
        for (int i = 0; i < count; i++) {
            double offsetX = (this.random.nextDouble() - 0.5D) * 0.5D * size;
            double offsetY = this.random.nextDouble() * 0.45D * size;
            double offsetZ = (this.random.nextDouble() - 0.5D) * 0.5D * size;
            this.level().addParticle(
                    ParticleTypes.SMOKE,
                    this.getX() + offsetX,
                    this.getY() + offsetY,
                    this.getZ() + offsetZ,
                    0.0D,
                    0.02D,
                    0.0D
            );
        }
    }

    // 口の位置からターゲットへ向けてウィザースカルを発射
    private void shootWitherSkull(LivingEntity target) {
        int size = this.getSize();
        Vec3 forward = Vec3.directionFromRotation(0.0F, this.yBodyRot);

        double spawnX = this.getX() + forward.x * (0.25D * size);
        double spawnY = this.getY() + 0.20D * size;
        double spawnZ = this.getZ() + forward.z * (0.25D * size);

        double targetX = target.getX() - spawnX;
        double targetY = (target.getY() + target.getEyeHeight() * 0.5D) - spawnY;
        double targetZ = target.getZ() - spawnZ;

        WitherSkull skull = new WitherSkull(this.level(), this, new Vec3(targetX, targetY, targetZ));
        skull.setOwner(this);
        skull.setPosRaw(spawnX, spawnY, spawnZ);

        this.level().addFreshEntity(skull);
        this.playSound(SoundEvents.WITHER_SHOOT, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SMOKE, spawnX, spawnY, spawnZ, 6, 0.1D, 0.1D, 0.1D, 0.05D);
        }
    }

    // 接触攻撃時にウィザー状態（10秒）を付与
    @Override
    protected void dealDamage(LivingEntity livingEntity) {
        if (this.isAlive() && this.isDealsDamage()) {
            int size = this.getSize();
            if (this.distanceToSqr(livingEntity) < 0.6D * (double) size * 0.6D * (double) size
                    && this.hasLineOfSight(livingEntity)) {
                DamageSource damageSource = this.damageSources().mobAttack(this);
                if (livingEntity.hurt(damageSource, this.getAttackDamage())) {
                    livingEntity.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 0), this);
                    this.playSound(SoundEvents.SLIME_ATTACK, 1.0F, (this.random.nextFloat() - this.random.nextFloat()) * 0.2F + 1.0F);
                    if (this.level() instanceof ServerLevel serverLevel) {
                        EnchantmentHelper.doPostAttackEffects(serverLevel, livingEntity, damageSource);
                    }
                }
            }
        }
    }

    // ウィザーアーマー中の飛び道具無効化 ＋ 自身のスカル・爆発による自滅防止
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (this.isPowered() && source.is(DamageTypeTags.IS_PROJECTILE)) {
            return false;
        }
        if (source.getEntity() instanceof WitherSlimeEntity) {
            return false;
        }
        if (source.is(DamageTypeTags.IS_EXPLOSION) && source.getEntity() == this) {
            return false;
        }
        return super.hurt(source, amount);
    }

    @Override
    public boolean canBeAffected(MobEffectInstance effect) {
        if (effect.getEffect().equals(MobEffects.WITHER)) {
            return false;
        }
        return super.canBeAffected(effect);
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource damageSource) {
        return SoundEvents.WITHER_SKELETON_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WITHER_SKELETON_DEATH;
    }
}