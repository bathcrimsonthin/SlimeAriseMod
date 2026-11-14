package com.bathcrimsonthin.slimearisemod.entity;

import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SlimeFieldEntity extends Entity {

    public static final int MAX_LIFETIME = 200; // 10秒
    public static final float FIELD_RADIUS = 2.4F; // 直径 4.8ブロック

    private int spawnedSlimeCount = 0;

    public SlimeFieldEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    public void tick() {
        super.tick();

        if (this.tickCount >= MAX_LIFETIME) {
            this.discard();
            return;
        }

        if (this.level().isClientSide) {
            if (this.tickCount % 5 == 0) {
                double rx = this.getX() + (this.random.nextDouble() - 0.5D) * FIELD_RADIUS * 1.6D;
                double rz = this.getZ() + (this.random.nextDouble() - 0.5D) * FIELD_RADIUS * 1.6D;
                this.level().addParticle(ParticleTypes.ITEM_SLIME, rx, this.getY() + 0.05D, rz, 0.0D, 0.05D, 0.0D);
            }
            return;
        }

        if (this.level() instanceof ServerLevel serverLevel) {
            AABB area = new AABB(
                    this.getX() - FIELD_RADIUS,
                    this.getY() - 0.25D,
                    this.getZ() - FIELD_RADIUS,
                    this.getX() + FIELD_RADIUS,
                    this.getY() + 1.5D,
                    this.getZ() + FIELD_RADIUS
            );

            List<LivingEntity> targets = serverLevel.getEntitiesOfClass(
                    LivingEntity.class,
                    area,
                    entity -> entity.isAlive() && !entity.isSpectator() && !(entity instanceof Slime)
            );

            for (LivingEntity target : targets) {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10, 2, false, false, true));
            }

            if (this.tickCount > 25 && this.tickCount % 70 == 0 && this.spawnedSlimeCount < 2) {
                double rx = this.getX() + (this.random.nextDouble() - 0.5D) * FIELD_RADIUS * 1.3D;
                double rz = this.getZ() + (this.random.nextDouble() - 0.5D) * FIELD_RADIUS * 1.3D;

                KingSlimeEntity.spawnRatioSlime(
                        serverLevel,
                        rx,
                        this.getY() + 0.1D,
                        rz,
                        2,
                        0.25F,
                        0.0D,
                        0.25D,
                        0.0D
                );
                this.spawnedSlimeCount++;
                this.playSound(SoundEvents.SLIME_SQUISH_SMALL, 0.8F, 1.0F);
                serverLevel.sendParticles(ParticleTypes.ITEM_SLIME, rx, this.getY() + 0.1D, rz, 8, 0.2D, 0.1D, 0.2D, 0.05D);
            }
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("Age");
        this.spawnedSlimeCount = tag.getInt("SpawnedSlimes");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.tickCount);
        tag.putInt("SpawnedSlimes", this.spawnedSlimeCount);
    }
}