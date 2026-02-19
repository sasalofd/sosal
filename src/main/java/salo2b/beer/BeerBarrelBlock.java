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
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class BeerBarrelBlock extends BaseEntityBlock {
    public static final MapCodec<BeerBarrelBlock> CODEC = simpleCodec(BeerBarrelBlock::new);

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    public BeerBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BeerBarrelBlockEntity(pos, state);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BeerBarrelBlockEntity barrel) {

            // Проверяем все виды пива
            boolean isAnyBeer = stack.is(ModItems.BEER.get()) ||
                    stack.is(ModItems.FILTERED_BEER.get()) ||
                    stack.is(ModItems.LIGHT_BEER.get());

            // --- ЛОГИКА ЗАЛИВКИ ---
            if (isAnyBeer) {
                // Если полная
                if (barrel.getMugsCount() >= 10) {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("§6Бочка заполнена!§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }

                // Если сорт подходит
                if (barrel.canFillWith(stack)) {
                    if (barrel.addMug()) {
                        if (!level.isClientSide) {
                            stack.shrink(1);
                            ItemStack emptyMug = new ItemStack(ModBlocks.WOODEN_MUG.get());
                            if (!player.getInventory().add(emptyMug)) {
                                player.drop(emptyMug, false);
                            }
                            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);

                            // НОВОЕ УВЕДОМЛЕНИЕ
                            player.displayClientMessage(Component.literal("§7Добавлена кружка. В бочке: §6" + barrel.getMugsCount() + "/10"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("§cЭтот сорт нельзя смешивать с тем, что в бочке!§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // --- ЛОГИКА ЗАБОРА ---
            if (stack.is(ModBlocks.WOODEN_MUG.get().asItem())) {
                if (barrel.getMugsCount() > 0) {
                    if (!level.isClientSide) {
                        ItemStack beerStack = barrel.takeMug();
                        stack.shrink(1);
                        if (!player.getInventory().add(beerStack)) {
                            player.drop(beerStack, false);
                        }
                        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);

                        // НОВОЕ УВЕДОМЛЕНИЕ
                        player.displayClientMessage(Component.literal("§7Забрали кружку. Осталось: §6" + barrel.getMugsCount() + "/10 §7| Сорт: §e" + barrel.getStageName()), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                } else {
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("§8Бочка пуста§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // --- КЛИК ПУСТОЙ РУКОЙ (ПРОВЕРКА) ---
            if (stack.isEmpty()) {
                if (!level.isClientSide) {
                    if (barrel.getMugsCount() == 0) {
                        player.displayClientMessage(Component.literal("§8Бочка пуста§r"), true);
                    } else {
                        // ПОНЯТНОЕ ОПИСАНИЕ
                        player.displayClientMessage(Component.literal("§7Кружек пива в бочке: §6" + barrel.getMugsCount() + "/10 §7| Сорт: §e" + barrel.getStageName()), true);
                    }
                }
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.BEER_BARREL_BE.get(), BeerBarrelBlockEntity::tick);
    }
}
