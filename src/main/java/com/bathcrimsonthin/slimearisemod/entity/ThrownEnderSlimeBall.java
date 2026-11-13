package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

public class ThrownEnderSlimeBall extends ThrowableItemProjectile {

    private static final int MAX_BOUNCES = 3;
    private int bounceCount = 0;

    public ThrownEnderSlimeBall(EntityType<? extends ThrownEnderSlimeBall> entityType, Level level) {
        super(entityType, level);
    }

    public ThrownEnderSlimeBall(Level level, LivingEntity shooter) {
        super(ModEntities.ENDER_SLIME_BALL, shooter, level);
    }

    public ThrownEnderSlimeBall(Level level, double x, double y, double z) {
        super(ModEntities.ENDER_SLIME_BALL, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.ENDER_SLIME_BALL;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide) {
            this.level().addParticle(
                    ParticleTypes.PORTAL,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    (this.random.nextDouble() - 0.5D) * 0.2D,
                    (this.random.nextDouble() - 0.5D) * 0.2D,
                    (this.random.nextDouble() - 0.5D) * 0.2D
            );
        } else if (this.bounceCount >= MAX_BOUNCES && this.getDeltaMovement().lengthSqr() < 0.0025D) {
            // 3回バウンド後に勢いが完全に止まった場合の安全発火
            this.squishAndTeleport(this.position());
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);

        if (this.level().isClientSide) {
            return;
        }

        if (this.bounceCount < MAX_BOUNCES) {
            this.bounceCount++;

            Direction direction = result.getDirection();
            Vec3 hitPos = result.getLocation();
            Vec3 normal = Vec3.atLowerCornerOf(direction.getNormal());

            // 同一面への連続判定を防ぐため法線方向へわずかにオフセット
            this.setPos(hitPos.x + normal.x * 0.02D, hitPos.y + normal.y * 0.02D, hitPos.z + normal.z * 0.02D);

            Vec3 motion = this.getDeltaMovement();
            double restitution = 0.7D;
            double friction = 0.85D;

            double vx = motion.x;
            double vy = motion.y;
            double vz = motion.z;

            switch (direction.getAxis()) {
                case X -> vx = -vx * restitution;
                case Y -> {
                    vy = -vy * restitution;
                    vx *= friction;
                    vz *= friction;
                }
                case Z -> vz = -vz * restitution;
            }

            this.setDeltaMovement(vx, vy, vz);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.8F, 1.1F + this.random.nextFloat() * 0.2F);
        } else {
            this.squishAndTeleport(result.getLocation());
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);

        Entity target = result.getEntity();
        target.hurt(this.damageSources().thrown(this, this.getOwner()), 0.0F);

        if (this.level().isClientSide) {
            return;
        }

        if (this.bounceCount < MAX_BOUNCES) {
            this.bounceCount++;

            Vec3 motion = this.getDeltaMovement();
            this.setDeltaMovement(-motion.x * 0.6D, Math.abs(motion.y) * 0.5D + 0.15D, -motion.z * 0.6D);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(),
                    SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.8F, 1.1F);
        } else {
            this.squishAndTeleport(result.getLocation());
        }
    }

    private void squishAndTeleport(Vec3 destination) {
        if (this.isRemoved()) {
            return;
        }

        // クライアント側へ破損（潰れ）エフェクトを通知
        this.level().broadcastEntityEvent(this, EntityEvent.DEATH);
        this.level().playSound(null, destination.x, destination.y, destination.z,
                SoundEvents.SLIME_SQUISH, SoundSource.NEUTRAL, 1.0F, 0.9F);

        Entity owner = this.getOwner();
        if (owner instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.connection.isAcceptingMessages() && serverPlayer.level() == this.level() && !serverPlayer.isSleeping()) {
                if (owner.isPassenger()) {
                    serverPlayer.dismountTo(destination.x, destination.y, destination.z);
                } else {
                    owner.teleportTo(destination.x, destination.y, destination.z);
                }
                owner.resetFallDistance();
                this.level().playSound(null, destination.x, destination.y, destination.z,
                        SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0F, 1.0F);
            }
        } else if (owner != null) {
            owner.teleportTo(destination.x, destination.y, destination.z);
            owner.resetFallDistance();
        }

        this.discard();
    }

    @Override
    public void handleEntityEvent(byte id) {
        if (id == EntityEvent.DEATH) {
            ItemStack itemStack = this.getItem();
            ParticleOptions itemParticle = new ItemParticleOption(ParticleTypes.ITEM, itemStack.isEmpty() ? new ItemStack(ModItems.ENDER_SLIME_BALL) : itemStack);

            for (int i = 0; i < 16; ++i) {
                this.level().addParticle(
                        itemParticle,
                        this.getX(),
                        this.getY(),
                        this.getZ(),
                        (this.random.nextDouble() - 0.5D) * 0.35D,
                        this.random.nextDouble() * 0.25D,
                        (this.random.nextDouble() - 0.5D) * 0.35D
                );
            }

            for (int i = 0; i < 24; ++i) {
                this.level().addParticle(
                        ParticleTypes.PORTAL,
                        this.getX(),
                        this.getY() + this.random.nextDouble() * 1.5D,
                        this.getZ(),
                        this.random.nextGaussian() * 0.5D,
                        -this.random.nextDouble(),
                        this.random.nextGaussian() * 0.5D
                );
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("BounceCount", this.bounceCount);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        this.bounceCount = compound.getInt("BounceCount");
    }
}