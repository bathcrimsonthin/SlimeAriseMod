package com.bathcrimsonthin.slimearisemod.entity;

import com.bathcrimsonthin.slimearisemod.block.ModBlocks;
import com.bathcrimsonthin.slimearisemod.block.StickyTorchBlock;
import com.bathcrimsonthin.slimearisemod.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

public class StickyTorchProjectileEntity extends ThrowableItemProjectile {

    public StickyTorchProjectileEntity(EntityType<? extends StickyTorchProjectileEntity> entityType, Level level) {
        super(entityType, level);
    }

    public StickyTorchProjectileEntity(Level level, LivingEntity shooter) {
        super(ModEntities.STICKY_TORCH_PROJECTILE, shooter, level);
    }

    public StickyTorchProjectileEntity(Level level, double x, double y, double z) {
        super(ModEntities.STICKY_TORCH_PROJECTILE, x, y, z, level);
    }

    @Override
    protected Item getDefaultItem() {
        return ModItems.STICKY_TORCH;
    }

    @Override
    public void tick() {
        super.tick();

        if (this.level().isClientSide && this.tickCount % 2 == 0) {
            this.level().addParticle(ParticleTypes.FLAME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.ITEM_SLIME, this.getX(), this.getY(), this.getZ(), 0.0D, 0.0D, 0.0D);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level().isClientSide) {
            Entity target = result.getEntity();
            target.hurt(this.damageSources().thrown(this, this.getOwner()), 1.0F);
            target.igniteForSeconds(2.0F);
            this.spawnAtLocation(new ItemStack(ModItems.STICKY_TORCH));
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (this.level().isClientSide) {
            return;
        }

        BlockPos hitPos = result.getBlockPos();
        Direction face = result.getDirection();

        // 草や雪など置換可能なブロックに当たった場合はその位置、そうでなければ隣接マスに設置を試みる
        BlockPos placePos = this.level().getBlockState(hitPos).canBeReplaced() ? hitPos : hitPos.relative(face);
        BlockState placeState = ModBlocks.STICKY_TORCH.defaultBlockState().setValue(StickyTorchBlock.FACING, face);

        if (this.level().getBlockState(placePos).canBeReplaced()
                && this.level().getFluidState(placePos).isEmpty()
                && placeState.canSurvive(this.level(), placePos)) {

            this.level().setBlock(placePos, placeState, Block.UPDATE_ALL);
            this.level().playSound(null, placePos, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
            this.level().playSound(null, placePos, SoundEvents.SLIME_SQUISH_SMALL, SoundSource.BLOCKS, 0.85F, 1.2F);

            if (this.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        placePos.getX() + 0.5D,
                        placePos.getY() + 0.5D,
                        placePos.getZ() + 0.5D,
                        8,
                        0.2D,
                        0.2D,
                        0.2D,
                        0.05D
                );
                serverLevel.sendParticles(
                        ParticleTypes.FLAME,
                        placePos.getX() + 0.5D,
                        placePos.getY() + 0.5D,
                        placePos.getZ() + 0.5D,
                        4,
                        0.1D,
                        0.1D,
                        0.1D,
                        0.02D
                );
            }
        } else {
            // 設置できない面（水没・ハーフブロック等）の場合はアイテムとしてドロップして回収可能にする
            this.spawnAtLocation(new ItemStack(ModItems.STICKY_TORCH));
            this.level().playSound(null, this.blockPosition(), SoundEvents.SLIME_SQUISH_SMALL, SoundSource.NEUTRAL, 0.7F, 1.4F);
        }

        this.discard();
    }
}