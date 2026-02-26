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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BeerBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<BeerBarrelBlock> CODEC = simpleCodec(BeerBarrelBlock::new);
    public static final BooleanProperty SWOLLEN = BooleanProperty.create("swollen");

    public BeerBarrelBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(SWOLLEN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(SWOLLEN);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // Если бочка вздута (swollen=true), мы делаем стандартную модель невидимой,
        // потому что её отрисовкой теперь занимается BeerBarrelRenderer.
        return state.getValue(SWOLLEN) ? RenderShape.INVISIBLE : RenderShape.MODEL;
    }


    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BeerBarrelBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (!(be instanceof BeerBarrelBlockEntity barrel)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        boolean isAnyBeer = stack.is(ModItems.BEER.get()) ||
                stack.is(ModItems.FILTERED_BEER.get()) ||
                stack.is(ModItems.LIGHT_BEER.get());

        // --- ЛОГИКА ЗАЛИВКИ ---
        if (isAnyBeer) {
            if (barrel.getMugsCount() >= 10) {
                if (!level.isClientSide) player.displayClientMessage(Component.literal("§6Бочка полная!"), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }

            if (barrel.canFillWith(stack)) {
                if (barrel.addMug()) {
                    if (!level.isClientSide) {
                        stack.shrink(1);
                        player.getInventory().add(new ItemStack(ModBlocks.WOODEN_MUG.get()));
                        level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                        // Короткий статус над хотбаром
                        player.displayClientMessage(Component.literal("§7Добавлено: §6" + barrel.getMugsCount() + "/10"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }
        }

        // --- ЛОГИКА ЗАБОРА ---
        if (stack.is(ModBlocks.WOODEN_MUG.get().asItem()) && barrel.getMugsCount() > 0) {
            if (!level.isClientSide) {
                ItemStack beerStack = barrel.takeMug();
                stack.shrink(1);
                player.getInventory().add(beerStack);
                level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                player.displayClientMessage(Component.literal("§7Забрано. Осталось: §6" + barrel.getMugsCount() + "/10"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // --- ПРОВЕРКА СОСТОЯНИЯ (Пустая рука) ---
        if (stack.isEmpty() && !level.isClientSide) {
            if (barrel.getMugsCount() > 0) {
                long timeLeft = barrel.getTargetTime() - level.getGameTime();

                // Если вздута (за 15 сек до взрыва)
                if (barrel.isFullOfEliteBeer && timeLeft > 0 && timeLeft <= barrel.getWarningPeriod()) {
                    player.displayClientMessage(Component.literal("§c⚠ БОЧКА ВЗДУТА! §e" + barrel.getStageName() + " §7[" + barrel.getMugsCount() + "/10]"), true);
                    level.playSound(null, pos, SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.5f, 0.5f);
                } else {
                    // Обычный короткий статус
                    player.displayClientMessage(Component.literal("§e" + barrel.getStageName() + " §7[" + barrel.getMugsCount() + "/10]"), true);
                }
            } else {
                player.displayClientMessage(Component.literal("§8Пусто"), true);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BEER_BARREL_BE.get(), BeerBarrelBlockEntity::tick);
    }
}