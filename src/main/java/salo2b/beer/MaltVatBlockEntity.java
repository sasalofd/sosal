package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class MaltVatBlockEntity extends BlockEntity implements MenuProvider {

    // СЛОТЫ:
    // 0: Дробленый солод (Вход ингредиентов)
    // 1: Ведро с водой (Чтобы пополнить бак)
    // 2: Пустое ведро (Чтобы забрать сусло)
    // 3: Ведро сусла (Выход)
    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            MaltVatBlockEntity.this.setChanged();
        }
    };

    // Данные для GUI (стрелочка и уровень воды)
    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MaltVatBlockEntity.this.progress;
                case 1 -> MaltVatBlockEntity.this.maxProgress;
                case 2 -> MaltVatBlockEntity.this.waterLevel;
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> MaltVatBlockEntity.this.progress = value;
                case 1 -> MaltVatBlockEntity.this.maxProgress = value;
                case 2 -> MaltVatBlockEntity.this.waterLevel = value;
            }
        }
        @Override
        public int getCount() { return 3; }
    };

    public int waterLevel = 0;      // Текущая вода (0-10)
    public int progress = 0;
    public int maxProgress = 6000;  // 5 минут = 300 сек * 20 тиков = 6000

    public MaltVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MALT_VAT_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaltVatBlockEntity entity) {
        if (level.isClientSide) return;

        // 1. ПОПОЛНЕНИЕ ВОДЫ (Слот 1)
        ItemStack waterBucketInput = entity.inventory.getItem(1);
        if (waterBucketInput.is(Items.WATER_BUCKET) && entity.waterLevel < 10) {
            // Если в баке есть место -> забираем воду, возвращаем ведро
            entity.inventory.setItem(1, new ItemStack(Items.BUCKET));
            entity.waterLevel++;
            entity.setChanged();
        }

        // 2. ВАРКА СУСЛА
        ItemStack inputIngredient = entity.inventory.getItem(0); // Слот с солодом
        ItemStack emptyBucketSlot = entity.inventory.getItem(2); // Слот для пустого ведра
        ItemStack outputSlot = entity.inventory.getItem(3);      // Слот выхода

        // Проверяем рецепт:
        // - Нужен CRUSHED_MALT (3 штуки)
        // - Нужна вода в баке (хотя бы 1 ведро)
        // - Нужно пустое ведро в слоте 2 (куда залить результат)
        // - Слот 3 должен быть пуст
        boolean hasIngredients = inputIngredient.is(ModItems.CRUSHED_MALT.get()) && inputIngredient.getCount() >= 3;
        boolean hasWater = entity.waterLevel >= 1;
        boolean hasBucket = emptyBucketSlot.is(Items.BUCKET);
        boolean outputEmpty = outputSlot.isEmpty();

        if (hasIngredients && hasWater && hasBucket && outputEmpty) {
            entity.progress++;

            // Если прошло 5 минут
            if (entity.progress >= entity.maxProgress) {
                // ПОТРАЧЕНО:
                inputIngredient.shrink(3);  // -3 Солода
                entity.waterLevel--;        // -1 Ведро воды из бака
                emptyBucketSlot.shrink(1);  // -1 Пустое ведро из слота

                // ПОЛУЧЕНО:
                entity.inventory.setItem(3, new ItemStack(ModItems.WORT_BUCKET.get())); // Ведро сусла

                entity.progress = 0;
            }
        } else {
            // Если условия нарушились (забрали ведро), сбрасываем (или можно уменьшать постепенно)
            if (entity.progress > 0) {
                entity.progress = 0;
            }
        }

        entity.setChanged();
    }

    // --- МЕНЮ И СОХРАНЕНИЕ ---
    @Override
    public Component getDisplayName() { return Component.literal("Сусловый чан"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MaltVatMenu(id, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.createTag(registries));
        tag.putInt("waterLevel", waterLevel);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.fromTag(tag.getList("inventory", 10), registries);
        waterLevel = tag.getInt("waterLevel");
        progress = tag.getInt("progress");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}