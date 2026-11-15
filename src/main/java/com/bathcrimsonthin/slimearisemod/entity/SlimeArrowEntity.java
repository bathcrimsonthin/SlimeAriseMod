package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class SlimeArrowEntity extends AbstractArrow {

    private static final int MAX_BOUNCES = 2;
    private static final double BOUNCE_RESTITUTION = 0.84D;

    private int bounceCount = 0;

    public SlimeArrowEntity(EntityType<? extends SlimeArrowEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SlimeArrowEntity(Level level, double x, double y, double z, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.SLIME_ARROW, x, y, z, level, pickupItemStack, firedFromWeapon);
    }

    public SlimeArrowEntity(Level level, LivingEntity owner, ItemStack pickupItemStack, @Nullable ItemStack firedFromWeapon) {
        super(ModEntities.SLIME_ARROW, owner, level, pickupItemStack, firedFromWeapon);
    }

    @Override
    protected ItemStack getDefaultPickupItem() {
        return new ItemStack(ModItems.SLIME_ARROW);
    }

    @Override
    public void tick() {
        super.tick();

        // 飛翔中の小さな粘液軌跡パーティクル
        if (this.level().isClientSide && !this.inGround && this.tickCount % 2 == 0) {
            Vec3 vel = this.getDeltaMovement();
            this.level().addParticle(
                    ParticleTypes.ITEM_SLIME,
                    this.getX() - vel.x * 0.25D,
                    this.getY() - vel.y * 0.25D,
                    this.getZ() - vel.z * 0.25D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        Vec3 motion = this.getDeltaMovement();

        if (this.bounceCount < MAX_BOUNCES && motion.lengthSqr() >= 0.04D) {
            Direction face = result.getDirection();
            Vec3 normal = Vec3.atLowerCornerOf(face.getNormal());

            // 入射ベクトルをヒット面の法線で反射し、反発係数を掛ける
            double dot = motion.dot(normal);
            Vec3 reflected = motion.subtract(normal.scale(2.0D * dot)).scale(BOUNCE_RESTITUTION);

            // 同じブロックへ即座に再衝突しないよう表面から少し押し出す
            Vec3 hitPos = result.getLocation().add(normal.scale(0.06D));
            this.setPos(hitPos.x, hitPos.y, hitPos.z);
            this.setDeltaMovement(reflected);

            // 矢の向きを反射後の進行方向に更新
            double horizDist = reflected.horizontalDistance();
            this.setYRot((float) (Mth.atan2(reflected.x, reflected.z) * (180.0D / Math.PI)));
            this.setXRot((float) (Mth.atan2(reflected.y, horizDist) * (180.0D / Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();

            this.inGround = false;
            this.bounceCount++;

            if (!this.level().isClientSide) {
                float pitch = 1.15F + this.bounceCount * 0.15F;
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.SLIME_JUMP, SoundSource.NEUTRAL, 0.85F, pitch);

                if (this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(
                            ParticleTypes.ITEM_SLIME,
                            hitPos.x,
                            hitPos.y,
                            hitPos.z,
                            8,
                            0.12D,
                            0.12D,
                            0.12D,
                            0.06D
                    );
                }
            }
            return;
        }

        super.onHitBlock(result);
    }

    @Override
    protected void doPostHurtEffects(LivingEntity target) {
        super.doPostHurtEffects(target);

        // 命中した相手に移動速度低下 II（4秒）を付与
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));

        if (this.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    ParticleTypes.ITEM_SLIME,
                    target.getX(),
                    target.getY(0.5D),
                    target.getZ(),
                    12,
                    0.25D,
                    0.25D,
                    0.25D,
                    0.08D
            );
            serverLevel.playSound(null, target.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.9F, 1.1F);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("BounceCount", this.bounceCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("BounceCount")) {
            this.bounceCount = tag.getInt("BounceCount");
        }
    }
}