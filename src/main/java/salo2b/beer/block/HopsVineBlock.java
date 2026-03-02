package salo2b.beer.block;

import salo2b.beer.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HopsVineBlock extends Block implements BonemealableBlock {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_7;
    protected static final VoxelShape SHAPE = Block.box(7.0D, 0.0D, 7.0D, 9.0D, 16.0D, 9.0D);

    public HopsVineBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    protected boolean isRandomlyTicking(BlockState state) {
        return true; 
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        
        // 1. Пытаемся созревать на текущем блоке (СИЛЬНО ЗАМЕДЛЕНО: 1 к 30)
        if (age < 7 && random.nextInt(30) == 0) {
            level.setBlock(pos, state.setValue(AGE, age + 1), 2);
        }

        // 2. Если достигли 2 стадии, пытаемся лезть ВВЕРХ (БЫСТРО: 1 к 4)
        if (age >= 2 && random.nextInt(4) == 0) {
            growUp(level, pos);
        }
    }

    private void growUp(Level level, BlockPos pos) {
        BlockPos abovePos = pos.above();
        BlockState aboveState = level.getBlockState(abovePos);
        
        int height = 1;
        BlockPos checkPos = pos.below();
        while (level.getBlockState(checkPos).is(this)) {
            height++;
            checkPos = checkPos.below();
        }

        if (height < 4 && aboveState.is(ModBlocks.LATTICE.get())) {
            level.setBlock(abovePos, this.defaultBlockState().setValue(AGE, 0), 3);
        }
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = level.getBlockState(belowPos);
        return belowState.is(this) || belowState.is(ModBlocks.LATTICE.get()) || belowState.isFaceSturdy(level, belowPos, Direction.UP) || belowState.is(Blocks.FARMLAND);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos currentPos, BlockPos facingPos) {
        if (!state.canSurvive(level, currentPos)) {
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, facing, facingState, level, currentPos, facingPos);
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(AGE) < 7 || canGrowUp(level, pos);
    }

    private boolean canGrowUp(LevelReader level, BlockPos pos) {
        int height = 1;
        BlockPos checkPos = pos.below();
        while (level.getBlockState(checkPos).is(this)) {
            height++;
            checkPos = checkPos.below();
        }
        return height < 4 && level.getBlockState(pos.above()).is(ModBlocks.LATTICE.get());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return true;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        int age = state.getValue(AGE);
        if (age < 2) {
            level.setBlock(pos, state.setValue(AGE, age + 2 > 7 ? 7 : age + 2), 2);
        } else if (canGrowUp(level, pos)) {
            growUp(level, pos);
        } else if (age < 7) {
            level.setBlock(pos, state.setValue(AGE, 7), 2);
        }
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (state.getValue(AGE) == 7) {
            if (!level.isClientSide) {
                Block.dropResources(state, level, pos);
                level.setBlock(pos, state.setValue(AGE, 4), 2);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
}
