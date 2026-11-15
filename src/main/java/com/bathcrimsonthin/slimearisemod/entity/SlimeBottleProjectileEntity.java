package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.block.ModBlocks;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

public class SlimeBottleProjectileEntity extends ThrowableItemProjectile {

    public SlimeBottleProjectileEntity(EntityType<? extends ThrowableItemProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public SlimeBottleProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.SLIME_BOTTLE_PROJECTILE, shooter, level);
    }

    public SlimeBottleProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntities.SLIME_BOTTLE_PROJECTILE, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.SLIME_BOTTLE;
    }

    @Override
    protected double getDefaultGravity() {
        // 落下中のプレイヤーを追い越して着弾できるよう、バニラの瓶(0.05)よりやや強めの重力に設定
        return 0.06D;
    }

    @Override
    protected void onHit(HitResult result) {
        super.onHit(result);
        if (!this.level().isClientSide) {
            BlockPos centerPos = this.resolveDeployCenter(result);
            this.shatterAndDeploy(centerPos);
            this.discard();
        }
    }

    private BlockPos resolveDeployCenter(HitResult result) {
        if (result instanceof BlockHitResult blockHit) {
            return blockHit.getBlockPos().relative(blockHit.getDirection());
        } else if (result instanceof EntityHitResult entityHit) {
            return entityHit.getEntity().blockPosition();
        }
        return this.blockPosition();
    }

    private void shatterAndDeploy(BlockPos centerPos) {
        Level level = this.level();

        // ガラス瓶が割れる音と破片パーティクル
        level.playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.GLASS_BREAK, SoundSource.NEUTRAL, 0.9F, 1.1F);
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(
                    new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.GLASS_BOTTLE)),
                    this.getX(),
                    this.getY() + 0.1D,
                    this.getZ(),
                    12,
                    0.25D,
                    0.15D,
                    0.25D,
                    0.08D
            );
        }

        // 中身のスライムが3×3のクッション膜として広がる
        BlockState cushionState = ModBlocks.SLIME_CUSHION.defaultBlockState();
        boolean placedAny = false;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                BlockPos targetPos = centerPos.offset(dx, 0, dz);
                if (level.getBlockState(targetPos).canBeReplaced()) {
                    level.setBlock(targetPos, cushionState, 3);
                    placedAny = true;
                }
            }
        }

        if (placedAny) {
            level.playSound(null, centerPos, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        centerPos.getX() + 0.5D,
                        centerPos.getY() + 0.3D,
                        centerPos.getZ() + 0.5D,
                        24,
                        1.0D,
                        0.15D,
                        1.0D,
                        0.05D
                );
            }
        }
    }
}