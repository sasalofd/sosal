package salo2b.beer.block.salt;

import salo2b.beer.registration.ModBlocks;
import salo2b.beer.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Collections;
import java.util.List;

public class SaltSpikeBlock extends Block implements SimpleWaterloggedBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    protected static final VoxelShape UP_SHAPE = Block.box(3.0D, 0.0D, 3.0D, 13.0D, 10.0D, 13.0D);
    protected static final VoxelShape DOWN_SHAPE = Block.box(3.0D, 6.0D, 3.0D, 13.0D, 16.0D, 13.0D);
    protected static final VoxelShape NORTH_SHAPE = Block.box(3.0D, 3.0D, 6.0D, 13.0D, 13.0D, 16.0D);
    protected static final VoxelShape SOUTH_SHAPE = Block.box(3.0D, 3.0D, 0.0D, 13.0D, 13.0D, 10.0D);
    protected static final VoxelShape WEST_SHAPE = Block.box(6.0D, 3.0D, 3.0D, 16.0D, 13.0D, 13.0D);
    protected static final VoxelShape EAST_SHAPE = Block.box(0.0D, 3.0D, 3.0D, 10.0D, 13.0D, 13.0D);

    public SaltSpikeBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(AGE, 0)
                .setValue(FACING, Direction.UP)
                .setValue(WATERLOGGED, false));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction direction = state.getValue(FACING);
        return switch (direction) {
            case NORTH -> NORTH_SHAPE;
            case SOUTH -> SOUTH_SHAPE;
            case EAST -> EAST_SHAPE;
            case WEST -> WEST_SHAPE;
            case DOWN -> DOWN_SHAPE;
            default -> UP_SHAPE;
        };
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos attachedPos = pos.relative(direction.getOpposite());
        return level.getBlockState(attachedPos).isFaceSturdy(level, attachedPos, direction);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(currentPos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        if (facing.getOpposite() == state.getValue(FACING) && !state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 3) {
            // Быстрый рост 100% для теста
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        } else if (state.getValue(FACING) == Direction.DOWN) {
            if (random.nextFloat() < 0.17578125f) {
                maybeFillCauldron(level, pos);
            }
        }
    }

    private void maybeFillCauldron(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos);
        for (int i = 0; i < 11; i++) {
            mutable.move(Direction.DOWN);
            BlockState stateBelow = level.getBlockState(mutable);
            if (stateBelow.is(Blocks.CAULDRON)) {
                level.setBlock(mutable, Blocks.WATER_CAULDRON.defaultBlockState(), 3);
                break;
            } else if (stateBelow.is(Blocks.WATER_CAULDRON)) {
                int waterLevel = stateBelow.getValue(LayeredCauldronBlock.LEVEL);
                if (waterLevel < 3) {
                    level.setBlock(mutable, stateBelow.setValue(LayeredCauldronBlock.LEVEL, waterLevel + 1), 3);
                }
                break;
            } else if (!stateBelow.isAir() && !stateBelow.is(Blocks.WATER)) {
                break;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == 3 && state.getValue(FACING) == Direction.DOWN) {
            if (random.nextInt(10) == 0) {
                double x = pos.getX() + 0.5D;
                double y = pos.getY() + 0.3D;
                double z = pos.getZ() + 0.5D;
                level.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(AGE) == 3) {
            return List.of(new ItemStack(ModItems.SALT_SHARD.get(), 1));
        }
        return Collections.emptyList();
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE, FACING, WATERLOGGED);
    }
}
