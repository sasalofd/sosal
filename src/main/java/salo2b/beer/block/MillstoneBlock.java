package salo2b.beer.block;

import salo2b.beer.block.entity.MillstoneBlockEntity;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModItems;
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
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public MillstoneBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            BlockEntity entity = level.getBlockEntity(pos);
            if (entity instanceof MillstoneBlockEntity millstone) {
                ItemStack output = millstone.inventory.getStackInSlot(1);
                ItemStack input = millstone.inventory.getStackInSlot(0);
                if (!output.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(), output.copy());
                    millstone.inventory.setStackInSlot(1, ItemStack.EMPTY);
                    return InteractionResult.SUCCESS;
                } else if (!input.isEmpty()) {
                    Containers.dropItemStack(level, pos.getX(), pos.getY() + 0.5, pos.getZ(), input.copy());
                    millstone.inventory.setStackInSlot(0, ItemStack.EMPTY);
                    millstone.progress = 0;
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
                boolean canInput = stack.getItem() == ModItems.MALT.get() || 
                                 stack.getItem() == ModItems.BARLEY.get() || 
                                 stack.getItem() == ModItems.SALT_CRYSTAL.get();
                if (canInput) {
                    ItemStack remainder = millstone.inventory.insertItem(0, stack.copy(), false);
                    if (remainder.getCount() < stack.getCount()) {
                        player.setItemInHand(hand, remainder);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new MillstoneBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, (BlockEntityType<MillstoneBlockEntity>)(Object)ModBlockEntities.MILLSTONE.get(), MillstoneBlockEntity::tick);
    }
}
