package com.bathcrimsonthin.slimearisemod.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class StickyTorchBlock extends Block {

    public static final MapCodec<StickyTorchBlock> CODEC = simpleCodec(StickyTorchBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.FACING;

    private static final VoxelShape FLOOR_SHAPE = Block.box(6.0D, 0.0D, 6.0D, 10.0D, 10.0D, 10.0D);
    private static final VoxelShape CEILING_SHAPE = Block.box(6.0D, 6.0D, 6.0D, 10.0D, 16.0D, 10.0D);
    private static final Map<Direction, VoxelShape> WALL_SHAPES = Map.of(
            Direction.NORTH, Block.box(5.5D, 3.0D, 11.0D, 10.5D, 13.0D, 16.0D),
            Direction.SOUTH, Block.box(5.5D, 3.0D, 0.0D, 10.5D, 13.0D, 5.0D),
            Direction.WEST, Block.box(11.0D, 3.0D, 5.5D, 16.0D, 13.0D, 10.5D),
            Direction.EAST, Block.box(0.0D, 3.0D, 5.5D, 5.0D, 13.0D, 10.5D)
    );

    public StickyTorchBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.UP));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        if (facing == Direction.UP) {
            return FLOOR_SHAPE;
        }
        if (facing == Direction.DOWN) {
            return CEILING_SHAPE;
        }
        return WALL_SHAPES.getOrDefault(facing, FLOOR_SHAPE);
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction facing = state.getValue(FACING);
        BlockPos supportPos = pos.relative(facing.getOpposite());
        return canSupportCenter(level, supportPos, facing);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Direction clickedFace = context.getClickedFace();

        BlockState preferred = this.defaultBlockState().setValue(FACING, clickedFace);
        if (preferred.canSurvive(level, pos)) {
            return preferred;
        }

        for (Direction dir : context.getNearestLookingDirections()) {
            BlockState candidate = this.defaultBlockState().setValue(FACING, dir.getOpposite());
            if (candidate.canSurvive(level, pos)) {
                return candidate;
            }
        }
        return null;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (direction.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        Direction facing = state.getValue(FACING);
        double x = pos.getX() + 0.5D;
        double y = pos.getY() + 0.7D;
        double z = pos.getZ() + 0.5D;

        if (facing == Direction.DOWN) {
            // 天井設置時は下向き先端に炎を表示
            y = pos.getY() + 0.3D;
        } else if (facing.getAxis().isHorizontal()) {
            Direction opposite = facing.getOpposite();
            x += 0.27D * opposite.getStepX();
            y += 0.22D;
            z += 0.27D * opposite.getStepZ();
        }

        level.addParticle(ParticleTypes.SMOKE, x, y, z, 0.0D, 0.0D, 0.0D);
        level.addParticle(ParticleTypes.FLAME, x, y, z, 0.0D, 0.0D, 0.0D);

        // 時々根元のスライムから粘液粒が光る
        if (random.nextInt(6) == 0) {
            double slimeY = (facing == Direction.DOWN) ? (pos.getY() + 0.85D) : (pos.getY() + 0.25D);
            level.addParticle(
                    ParticleTypes.ITEM_SLIME,
                    pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D,
                    slimeY,
                    pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.2D,
                    0.0D,
                    0.0D,
                    0.0D
            );
        }
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }
}