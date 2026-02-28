package salo2b.beer.block.entity;

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

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

public class MillstoneBlockEntity extends BlockEntity implements MenuProvider {

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    protected final ContainerData data;
    public int progress = 0;
    public final int MAX_PROGRESS = 100;

    public float angle = 0;
    public float prevAngle = 0;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
        this.data = new ContainerData() {
            @Override
            public int get(int index) {
                return index == 0 ? MillstoneBlockEntity.this.progress : MillstoneBlockEntity.this.MAX_PROGRESS;
            }
            @Override
            public void set(int index, int value) {
                if (index == 0) MillstoneBlockEntity.this.progress = value;
            }
            @Override
            public int getCount() { return 2; }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("container.beer.millstone");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MillstoneMenu(id, inventory, this, this.data);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // Проверяем питание
        boolean hasPower = isPoweredByRotor(level, pos, state);

        // ВЫВОД ДЛЯ ТЕСТА (можно оставить для финальной проверки, потом удали)
        if (level.getGameTime() % 40 == 0 && !level.isClientSide) {
            System.out.println("Millstone Power: " + hasPower);
        }

        ItemStack input = entity.inventory.getStackInSlot(0);
        ItemStack output = entity.inventory.getStackInSlot(1);

        if (hasPower && !input.isEmpty() && input.is(ModItems.MALT.get()) && canInsertResult(output)) {
            entity.angle += 3.0F;
            if (entity.angle >= 360f) {
                entity.angle -= 360f;
                entity.prevAngle -= 360f;
            }

            if (!level.isClientSide) {
                entity.progress++;
                if (entity.progress >= entity.MAX_PROGRESS) {
                    entity.craftItem();
                    entity.progress = 0;
                }
            }
        } else if (!level.isClientSide && entity.progress > 0) {
            entity.progress = 0;
            // Шлем обновление на клиент, чтобы шкала в GUI сбросилась
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static boolean isPoweredByRotor(Level level, BlockPos pos, BlockState state) {
        // Жернова проверяют блоки вокруг себя (сверху и со всех сторон, кроме низа)
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;

            // Запускаем поиск от соседа.
            // Мы передаем 'visited', чтобы поиск не зациклился между двумя валами.
            if (checkPowerRecursive(level, pos.relative(dir), 0, new java.util.HashSet<>())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPowerRecursive(Level level, BlockPos currentPos, int distance, java.util.Set<BlockPos> visited) {
        // Ограничение: не дальше 16 блоков и не проверять один и тот же блок дважды
        if (distance > 16 || visited.contains(currentPos)) return false;
        visited.add(currentPos);

        BlockState state = level.getBlockState(currentPos);

        // 1. Нашли РОТОР — Победа!
        if (state.is(ModBlocks.WINDMILL_ROTOR.get())) {
            return true;
        }

        // 2. Если это ВАЛ
        if (state.is(ModBlocks.WINDMILL_SHAFT.get())) {
            // Вал обычно передает энергию дальше по своей оси
            // Но для простоты проверим всех соседей вала
            for (Direction dir : Direction.values()) {
                if (checkPowerRecursive(level, currentPos.relative(dir), distance + 1, visited)) {
                    return true;
                }
            }
        }

        // 3. Если это КОРОВКА ПЕРЕДАЧ (Gearbox)
        if (state.is(ModBlocks.GEARBOX.get())) {
            // Коробка передач передает энергию всем соседям!
            for (Direction dir : Direction.values()) {
                if (checkPowerRecursive(level, currentPos.relative(dir), distance + 1, visited)) {
                    return true;
                }
            }
        }

        return false;
    }

    private static boolean checkChain(Level level, BlockPos startPos, Direction direction) {
        BlockPos currentPos = startPos;
        for (int i = 0; i < 16; i++) {
            BlockState state = level.getBlockState(currentPos);

            // 1. Проверка на ротор через ModBlocks (самый надежный способ)
            if (state.is(ModBlocks.WINDMILL_ROTOR.get())) return true;

            // 2. Проверка на вал или коробку передач
            // Мы используем .is(), чтобы точно знать, что это наши блоки из реестра
            if (state.is(ModBlocks.WINDMILL_SHAFT.get()) || state.is(ModBlocks.GEARBOX.get())) {
                currentPos = currentPos.relative(direction);
                continue;
            }
            break;
        }
        return false;
    }

    private static boolean canInsertResult(ItemStack output) {
        return output.isEmpty() || (output.is(ModItems.CRUSHED_MALT.get()) && output.getCount() < output.getMaxStackSize());
    }

    private void craftItem() {
        ItemStack input = inventory.getStackInSlot(0);
        if (input.is(ModItems.MALT.get())) {
            input.shrink(1);
            ItemStack result = new ItemStack(ModItems.CRUSHED_MALT.get());
            inventory.insertItem(1, result, false);
            setChanged();
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
    }
}
