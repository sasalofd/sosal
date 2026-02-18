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
    // 1. КОДЕК (Обязательно для 1.21.1)
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

            // --- ЛОГИКА ЗАЛИВКИ ---
            // Проверяем, является ли предмет в руке любым из трех видов пива
            boolean isAnyBeer = stack.is(ModItems.BEER.get()) ||
                    stack.is(ModItems.FILTERED_BEER.get()) ||
                    stack.is(ModItems.LIGHT_BEER.get());

            if (isAnyBeer) {
                // Проверяем, совпадает ли сорт с тем, что уже в бочке
                if (barrel.canFillWith(stack)) {
                    if (barrel.addMug()) { // Убедись, что метод называется addMug()
                        if (!level.isClientSide) {
                            stack.shrink(1);
                            ItemStack emptyMug = new ItemStack(ModBlocks.WOODEN_MUG.get());
                            if (stack.isEmpty()) {
                                player.setItemInHand(hand, emptyMug);
                            } else if (!player.getInventory().add(emptyMug)) {
                                player.drop(emptyMug, false);
                            }
                            level.playSound(null, pos, SoundEvents.BOTTLE_FILL, SoundSource.BLOCKS, 1.0f, 1.0f);
                            player.displayClientMessage(Component.literal("Залито: " + barrel.getMugsCount() + "/10"), true);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                } else {
                    // Если сорт не совпадает
                    if (!level.isClientSide) {
                        player.displayClientMessage(Component.literal("§cНельзя смешивать разные сорта!§r"), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // --- ЛОГИКА ЗАБОРА ---
            if (stack.is(ModBlocks.WOODEN_MUG.get().asItem())) {
                if (barrel.getMugsCount() > 0) {
                    if (!level.isClientSide) {
                        ItemStack beerStack = barrel.takeMug(); // Метод забирает 1 кружку текущего сорта
                        stack.shrink(1);
                        if (stack.isEmpty()) {
                            player.setItemInHand(hand, beerStack);
                        } else if (!player.getInventory().add(beerStack)) {
                            player.drop(beerStack, false);
                        }
                        level.playSound(null, pos, SoundEvents.BOTTLE_EMPTY, SoundSource.BLOCKS, 1.0f, 1.0f);
                        player.displayClientMessage(Component.literal("В бочке: " + barrel.getMugsCount() + "/10 | Сорт: " + barrel.getStageName()), true);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
            }

            // --- СТАТУС ПУСТОЙ РУКОЙ ---
            if (stack.isEmpty()) {
                if (!level.isClientSide) {
                    player.displayClientMessage(Component.literal("Статус: " + barrel.getStageName() + " (" + barrel.getMugsCount() + "/10)"), true);
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
