package salo2b.beer.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import salo2b.beer.registration.ModBlocks;

public class SaltingBarrelLidBlock extends Block {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SaltingBarrelLidBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.INVISIBLE;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockPos barrelPos = getBarrelPos(pos, state.getValue(FACING));
        BlockState barrelState = level.getBlockState(barrelPos);
        
        if (barrelState.is(ModBlocks.SALTING_BARREL.get())) {
            // ЗАКРЫВАЕМ БЕЗ ЭФФЕКТОВ
            if (stack.isEmpty() && !player.isShiftKeyDown() && barrelState.getValue(SaltingBarrelBlock.OPEN)) {
                if (!level.isClientSide) {
                    // Используем метод самой бочки для синхронного закрытия обеих половин
                    ((SaltingBarrelBlock)barrelState.getBlock()).setOpen(level, barrelPos, barrelState, false);
                    level.playSound(null, barrelPos, net.minecraft.sounds.SoundEvents.BARREL_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return barrelState.useItemOn(stack, level, player, hand, hitResult);
        }
        
        if (!level.isClientSide) level.setBlock(pos, Blocks.AIR.defaultBlockState(), 35);
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        BlockPos barrelPos = getBarrelPos(pos, state.getValue(FACING));
        if (facingPos.equals(barrelPos)) {
            if (!facingState.is(ModBlocks.SALTING_BARREL.get()) || !facingState.getValue(SaltingBarrelBlock.OPEN)) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        return super.updateShape(state, facing, facingState, level, pos, facingPos);
    }

    private BlockPos getBarrelPos(BlockPos pos, Direction facing) {
        // Бочка находится НИЖЕ и ЛЕВЕЕ (CounterClockWise) относительно хитбокса крышки
        return pos.below().relative(facing.getClockWise());
    }
}
