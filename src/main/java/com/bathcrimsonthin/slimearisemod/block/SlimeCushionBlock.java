package com.bathcrimsonthin.slimearisemod.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class SlimeCushionBlock extends HalfTransparentBlock {

    private static final VoxelShape SHAPE = Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D);
    private static final int DURATION_TICKS = 80; // 4秒間（80 ticks）で消滅

    public SlimeCushionBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        if (!level.isClientSide && !oldState.is(this)) {
            level.scheduleTick(pos, this, DURATION_TICKS);
        }
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.sendParticles(
                ParticleTypes.ITEM_SLIME,
                pos.getX() + 0.5D,
                pos.getY() + 0.25D,
                pos.getZ() + 0.5D,
                6,
                0.3D,
                0.1D,
                0.3D,
                0.02D
        );
        level.playSound(null, pos, SoundEvents.SLIME_BLOCK_BREAK, SoundSource.BLOCKS, 0.4F, 1.2F);
        level.removeBlock(pos, false);
    }

    @Override
    public void fallOn(Level level, BlockState state, BlockPos pos, Entity entity, float fallDistance) {
        // スニークの有無に関わらず落下ダメージを完全に無効化
        entity.causeFallDamage(fallDistance, 0.0F, level.damageSources().fall());

        if (!level.isClientSide && fallDistance > 1.5F) {
            level.playSound(null, pos, SoundEvents.SLIME_SQUISH, SoundSource.BLOCKS, 0.8F, 1.0F);
            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(
                        ParticleTypes.ITEM_SLIME,
                        entity.getX(),
                        pos.getY() + 0.3D,
                        entity.getZ(),
                        10,
                        0.3D,
                        0.1D,
                        0.3D,
                        0.05D
                );
            }
        }
    }

    @Override
    public void updateEntityAfterFallOn(BlockGetter level, Entity entity) {
        if (entity.isSuppressingBounce()) {
            // スニーク着地時はバウンドを抑制してピタッと止まる
            super.updateEntityAfterFallOn(level, entity);
            return;
        }

        Vec3 motion = entity.getDeltaMovement();
        // 通常の歩行時は跳ねず、ジャンプや落下で着地したときのみバウンドする
        if (motion.y < -0.15D) {
            double bounceFactor = entity instanceof LivingEntity ? 1.05D : 0.8D;
            double bounceY = Math.max(-motion.y * bounceFactor, 0.85D);
            bounceY = Math.min(bounceY, 2.2D);
            entity.setDeltaMovement(motion.x, bounceY, motion.z);
        } else {
            super.updateEntityAfterFallOn(level, entity);
        }
    }
}