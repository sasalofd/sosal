package salo2b.beer.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.block.entity.SaltingBarrelBlockEntity;
import salo2b.beer.registration.ModBlockEntities;

public class SaltingBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<SaltingBarrelBlock> CODEC = simpleCodec(SaltingBarrelBlock::new);
    public static final net.minecraft.world.level.block.state.properties.BooleanProperty OPEN = BlockStateProperties.OPEN;
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;

    private static final VoxelShape INSIDE = box(2.0D, 2.0D, 2.0D, 14.0D, 16.0D, 14.0D);
    private static final VoxelShape BARREL_SHAPE = Shapes.join(Shapes.block(), INSIDE, net.minecraft.world.phys.shapes.BooleanOp.ONLY_FIRST);

    public SaltingBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OPEN, false).setValue(FACING, Direction.NORTH).setValue(HALF, DoubleBlockHalf.LOWER));
    }

    @Override
    protected void createBlockStateDefinition(net.minecraft.world.level.block.state.StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OPEN, FACING, HALF);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        if (pos.getY() < level.getMaxBuildHeight() - 1 && level.getBlockState(pos.above()).canBeReplaced(context)) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()).setValue(HALF, DoubleBlockHalf.LOWER).setValue(OPEN, false);
        }
        return null;
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        level.setBlock(pos.above(), state.setValue(HALF, DoubleBlockHalf.UPPER), 3);
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction facing, BlockState facingState, net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos facingPos) {
        DoubleBlockHalf half = state.getValue(HALF);
        if (facing.getAxis() == Direction.Axis.Y && (half == DoubleBlockHalf.LOWER == (facing == Direction.UP))) {
            return facingState.is(this) && facingState.getValue(HALF) != half ? state.setValue(FACING, facingState.getValue(FACING)).setValue(OPEN, facingState.getValue(OPEN)) : net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
        } else {
            return super.updateShape(state, facing, facingState, level, pos, facingPos);
        }
    }

    private static VoxelShape makeOpenLid(Direction facing) {
        // Возвращаем тонкий хитбокс (1 пиксель)
        // Оставляем только 1 пиксель "зацепа" внутри блока, остальное - снаружи
        switch (facing) {
            case NORTH: return box(-16.0D, 0.0D, 0.0D, 1.0D, 1.0D, 16.0D);
            case SOUTH: return box(15.0D, 0.0D, 0.0D, 32.0D, 1.0D, 16.0D);
            case WEST:  return box(0.0D, 0.0D, 15.0D, 16.0D, 1.0D, 32.0D);
            case EAST:  return box(0.0D, 0.0D, -16.0D, 1.0D, 1.0D, 0.0D);
            default:    return Shapes.empty();
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            if (state.getValue(OPEN)) {
                // Только выносная крышка с микро-зацепом на краю
                return makeOpenLid(state.getValue(FACING));
            }
            return box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
        }
        return BARREL_SHAPE;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(HALF) == DoubleBlockHalf.UPPER) {
            if (state.getValue(OPEN)) {
                // Для коллизии (чтобы стоять) зацеп не нужен, используем чистый офсет
                Direction facing = state.getValue(FACING);
                switch (facing) {
                    case NORTH: return box(-16.0D, 0.0D, 0.0D, 0.0D, 1.0D, 16.0D);
                    case SOUTH: return box(16.0D, 0.0D, 0.0D, 32.0D, 1.0D, 16.0D);
                    case WEST:  return box(0.0D, 0.0D, 16.0D, 16.0D, 1.0D, 32.0D);
                    case EAST:  return box(0.0D, 0.0D, -16.0D, 16.0D, 1.0D, 0.0D);
                    default:    return Shapes.empty();
                }
            }
            return box(0.0D, 0.0D, 0.0D, 16.0D, 1.0D, 16.0D);
        }
        return state.getValue(OPEN) ? BARREL_SHAPE : Shapes.block();
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return new SaltingBarrelBlockEntity(pos, state);
        }
        return null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        boolean isUpper = state.getValue(HALF) == DoubleBlockHalf.UPPER;
        BlockPos barrelPos = isUpper ? pos.below() : pos;
        BlockState barrelState = level.getBlockState(barrelPos);
        if (!barrelState.is(this)) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        BlockEntity be = level.getBlockEntity(barrelPos);
        if (be instanceof SaltingBarrelBlockEntity saltingBarrel) {
            boolean isOpen = barrelState.getValue(OPEN);
            
            // Если бочка ЗАКРЫТА - открываем по клику в любую часть (пустой рукой)
            if (!isOpen) {
                if (stack.isEmpty() && !player.isShiftKeyDown()) {
                    level.setBlock(barrelPos, barrelState.setValue(OPEN, true), 3);
                    level.playSound(null, barrelPos, net.minecraft.sounds.SoundEvents.BARREL_OPEN, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 1.2f);
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            } else {
                // Если бочка ОТКРЫТА
                if (isUpper) {
                    // Клик по КРЫШКЕ (верхний блок) -> Всегда ЗАКРЫТЬ
                    if (stack.isEmpty() && !player.isShiftKeyDown()) {
                        level.setBlock(barrelPos, barrelState.setValue(OPEN, false), 3);
                        level.playSound(null, barrelPos, net.minecraft.sounds.SoundEvents.BARREL_CLOSE, net.minecraft.sounds.SoundSource.BLOCKS, 1.0f, 0.8f);
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else {
                    // Клик по БОЧКЕ (нижний блок) -> Взаимодействие с рыбой
                    return saltingBarrel.interact(player, hand, stack);
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (state.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return createTickerHelper(type, ModBlockEntities.SALTING_BARREL_BE.get(), SaltingBarrelBlockEntity::tick);
        }
        return null;
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }
}
