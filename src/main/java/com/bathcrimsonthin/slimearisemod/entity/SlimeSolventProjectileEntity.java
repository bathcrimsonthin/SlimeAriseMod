package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;

import java.util.List;

public class SlimeSolventProjectileEntity extends ThrowableItemProjectile {

    private static final double SPLASH_RADIUS = 3.5D;
    private static final float SOLVENT_DAMAGE = 4.0F;

    public SlimeSolventProjectileEntity(EntityType<? extends SlimeSolventProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public SlimeSolventProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SLIME_SOLVENT_PROJECTILE, shooter, level);
    }

    public SlimeSolventProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntities.SLIME_SOLVENT_PROJECTILE, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SLIME_SOLVENT;
    }

    @Override
    protected double getDefaultGravity() {
        return 0.05D;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);

        if (!this.level().isClientSide && this.level() instanceof ServerLevel serverLevel) {
            this.applySolventSplash(serverLevel);

            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.SPLASH_POTION_BREAK,
                    SoundSource.NEUTRAL,
                    1.0F,
                    0.9F + this.random.nextFloat() * 0.2F
            );
            serverLevel.playSound(
                    null,
                    this.getX(),
                    this.getY(),
                    this.getZ(),
                    SoundEvents.FIRE_EXTINGUISH,
                    SoundSource.NEUTRAL,
                    0.55F,
                    1.45F
            );

            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, this.getItem()),
                    this.getX(),
                    this.getY() + 0.1D,
                    this.getZ(),
                    20,
                    0.35D,
                    0.25D,
                    0.35D,
                    0.12D
            );
            serverLevel.sendParticles(
                    ParticleTypes.CLOUD,
                    this.getX(),
                    this.getY() + 0.15D,
                    this.getZ(),
                    16,
                    SPLASH_RADIUS * 0.45D,
                    0.2D,
                    SPLASH_RADIUS * 0.45D,
                    0.04D
            );

            this.discard();
        }
    }

    private void applySolventSplash(ServerLevel serverLevel) {
        AABB area = this.getBoundingBox().inflate(SPLASH_RADIUS, 2.0D, SPLASH_RADIUS);
        List<Slime> slimes = serverLevel.getEntitiesOfClass(Slime.class, area, Slime::isAlive);
        DamageSource damageSource = this.damageSources().thrown(this, this.getOwner());

        for (Slime slime : slimes) {
            if (this.distanceToSqr(slime) > SPLASH_RADIUS * SPLASH_RADIUS) {
                continue;
            }

            if (slime.hurt(damageSource, SOLVENT_DAMAGE)) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        slime.getX(),
                        slime.getY(0.5D),
                        slime.getZ(),
                        10,
                        slime.getBbWidth() * 0.3D,
                        slime.getBbHeight() * 0.3D,
                        slime.getBbWidth() * 0.3D,
                        0.08D
                );
                serverLevel.sendParticles(
                        ParticleTypes.BUBBLE_POP,
                        slime.getX(),
                        slime.getY(0.5D),
                        slime.getZ(),
                        8,
                        slime.getBbWidth() * 0.25D,
                        slime.getBbHeight() * 0.25D,
                        slime.getBbWidth() * 0.25D,
                        0.05D
                );

                // 溶解液で倒された通常・Modスライムは分裂させない
                if (slime.isDeadOrDying() && !(slime instanceof KingSlimeEntity)) {
                    slime.setSize(1, false);
                }
            }
        }
    }
}