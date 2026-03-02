package salo2b.beer.block;

import salo2b.beer.*;
import salo2b.beer.block.*;
import salo2b.beer.block.entity.*;
import salo2b.beer.item.*;
import salo2b.beer.menu.*;
import salo2b.beer.registration.*;
import salo2b.beer.villager.*;
import salo2b.beer.worldgen.*;
import salo2b.beer.client.renderer.*;
import salo2b.beer.client.screen.*;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlock extends BaseEntityBlock {
    public static final MapCodec<MillstoneBlock> CODEC = simpleCodec(MillstoneBlock::new);

    // ДОБАВЛЯЕМ FACING: Чтобы MillstoneBlockEntity видел эту переменную при проверке вала
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public MillstoneBlock(Properties properties) {
        super(properties);
        // Устанавливаем направление по умолчанию
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    // Регистрация свойств блока
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Установка направления при постановке блока
    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MillstoneBlockEntity millstone) {
                ItemStack output = millstone.inventory.getStackInSlot(1);
                ItemStack input = millstone.inventory.getStackInSlot(0);

                if (!output.isEmpty()) {
                    // Забираем результат
                    player.getInventory().add(output.copy());
                    millstone.inventory.setStackInSlot(1, ItemStack.EMPTY);
                    millstone.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.SUCCESS;
                } else if (!input.isEmpty()) {
                    // Забираем входной предмет и сбрасываем прогресс
                    player.getInventory().add(input.copy());
                    millstone.inventory.setStackInSlot(0, ItemStack.EMPTY);
                    millstone.progress = 0;
                    millstone.setChanged();
                    level.sendBlockUpdated(pos, state, state, 3);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MillstoneBlockEntity millstone) {
                // ПРОВЕРКА: принимаем только солод или ячмень
                if (!stack.isEmpty() && (stack.is(ModItems.MALT.get()) || stack.is(ModItems.BARLEY.get()))) {
                    ItemStack remainder = millstone.inventory.insertItem(0, stack.copy(), false);
                    if (remainder.getCount() < stack.getCount()) {
                        player.setItemInHand(hand, remainder);
                        millstone.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (state.getBlock() != newState.getBlock()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MillstoneBlockEntity millstone) {
                // Оптимизированный сброс содержимого ItemStackHandler
                for (int i = 0; i < millstone.inventory.getSlots(); i++) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), millstone.inventory.getStackInSlot(i));
                }
                level.updateNeighbourForOutputSignal(pos, this);
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MillstoneBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, (BlockEntityType<MillstoneBlockEntity>)(Object)ModBlockEntities.MILLSTONE.get(), MillstoneBlockEntity::tick);
    }
}
