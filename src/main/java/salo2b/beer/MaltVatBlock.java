package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class MaltVatBlock extends BaseEntityBlock {
    public static final MapCodec<MaltVatBlock> CODEC = simpleCodec(MaltVatBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    public MaltVatBlock(Properties properties) { super(properties); }

    @Override
    protected RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new MaltVatBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof MaltVatBlockEntity vat) {

            // 1. ЗАЛИВАЕМ ВОДУ
            if (stack.is(Items.WATER_BUCKET)) {
                if (vat.addWater()) {
                    if (!level.isClientSide) {
                        player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.literal("§bВода залита§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // 2. ДОБАВЛЯЕМ ЯЧМЕНЬ
            if (stack.is(ModItems.BARLEY.get())) {
                if (vat.addBarley()) {
                    if (!level.isClientSide) {
                        stack.shrink(1);
                        level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.literal("§eЯчмень добавлен. Начинается варка солода...§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // 3. ЗАБИРАЕМ ГОТОВЫЙ СОЛОД (Пустой рукой или чем угодно, когда готово)
            if (vat.isFinished()) {
                if (!level.isClientSide) {
                    ItemStack malt = new ItemStack(ModItems.MALT.get());
                    if (!player.getInventory().add(malt)) {
                        player.drop(malt, false);
                    }
                    vat.reset(); // Сбрасываем чан для новой варки
                    level.playSound(null, pos, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.BLOCKS, 1.0f, 1.0f);
                    player.displayClientMessage(Component.literal("§aСолод готов!§r"), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            // 4. ПРОВЕРКА СТАТУСА (Пустой рукой)
            if (stack.isEmpty()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal(vat.getStatusMessage()), true);
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.MALT_VAT_BE.get(), MaltVatBlockEntity::tick);
    }
}
