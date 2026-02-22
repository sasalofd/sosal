package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
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

    // Тот самый метод tick, вычищенный до блеска
// ... остальной код в начале класса ...

    public static void tick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // ПРОВЕРКА ПИТАНИЯ: Используем наш новый надежный метод!
        boolean hasPower = checkPowerChain(level, pos.above());

        ItemStack input = entity.inventory.getStackInSlot(0);
        ItemStack output = entity.inventory.getStackInSlot(1);

        // Логика работы жерновов
        if (hasPower && !input.isEmpty() && input.is(ModItems.MALT.get()) && canInsertResult(output)) {
            entity.angle += 3.0F;

            // Крафт происходит только на сервере
            if (!level.isClientSide) {
                entity.progress++;
                if (entity.progress >= entity.MAX_PROGRESS) {
                    entity.craftItem();
                    entity.progress = 0;
                }
            }
        } else {
            // Если питания нет или нет солода - сбрасываем прогресс
            if (!level.isClientSide && entity.progress > 0) {
                entity.progress = 0;
            }
        }
    }

    // ДОБАВЬ ЭТОТ МЕТОД: Он проверяет, есть ли над жерновами вал или коробка передач
    private static boolean checkPowerChain(Level level, BlockPos startPos) {
        BlockEntity currentBE = level.getBlockEntity(startPos);

        // Если прямо над нами коробка передач или вал, считаем, что механизм собран
        if (currentBE instanceof WindmillShaftBlockEntity || currentBE instanceof GearboxBlockEntity) {
            return true;
        }
        return false;
    }

    // ... методы canInsertResult, craftItem и сохранения NBT остаются без изменений ...

    private static boolean canInsertResult(ItemStack output) {
        return output.isEmpty() || (output.is(ModItems.CRUSHED_MALT.get()) && output.getCount() < output.getMaxStackSize());
    }

    private void craftItem() {
        ItemStack input = inventory.getStackInSlot(0);
        ItemStack result = new ItemStack(ModItems.CRUSHED_MALT.get());

        if (!input.isEmpty()) {
            input.shrink(1);
            ItemStack output = inventory.getStackInSlot(1);

            if (output.isEmpty()) {
                inventory.setStackInSlot(1, result);
            } else {
                output.grow(1);
            }
            // Обязательно сохраняем изменения
            setChanged();
        }
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
        inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("progress");
    }
}