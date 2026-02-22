package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlock extends BaseEntityBlock {
    public static final MapCodec<MillstoneBlock> CODEC = simpleCodec(MillstoneBlock::new);

    public MillstoneBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof MillstoneBlockEntity millstone) {
            ItemStack itemInHand = player.getItemInHand(hand);
            ItemStack outputSlot = millstone.inventory.getStackInSlot(1);

            // 1. Если в выходном слоте что-то есть — отдаем игроку СРАЗУ (даже если в руке что-то есть)
            if (!outputSlot.isEmpty()) {
                ItemStack extracted = millstone.inventory.extractItem(1, 64, false);
                if (!player.addItem(extracted)) {
                    player.drop(extracted, false);
                }
                return ItemInteractionResult.SUCCESS;
            }

            // 2. Если выход пуст и в руке СОЛОД — кладем его во вход
            if (itemInHand.is(ModItems.MALT.get())) {
                ItemStack remaining = millstone.inventory.insertItem(0, itemInHand.copy(), false);
                player.setItemInHand(hand, remaining);
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MillstoneBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Меняем на MODEL, чтобы блок перестал быть прозрачным в мире
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MILLSTONE.get(), MillstoneBlockEntity::tick);
    }
}