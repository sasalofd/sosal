package salo2b.beer.menu;

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

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

public class MaltVatMenu extends AbstractContainerMenu {
    public final MaltVatBlockEntity blockEntity;
    private final ContainerData data;

    // Конструктор клиента
    public MaltVatMenu(int id, Inventory inv, FriendlyByteBuf extraData) {
        this(id, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(3));
    }

    // Конструктор сервера
    public MaltVatMenu(int id, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MALT_VAT_MENU.get(), id);
        checkContainerSize(inv, 4);
        this.blockEntity = (MaltVatBlockEntity) entity;
        this.data = data;

        // --- РАССТАНОВКА СЛОТОВ (Подгони под свою текстуру!) ---

        // Слот 0: Дробленый солод (Сверху)
        addSlot(new Slot(blockEntity.inventory, 0, 80, 15));

        // Слот 1: Ведро воды для бака (Слева)
        addSlot(new Slot(blockEntity.inventory, 1, 20, 55));

        // Слот 2: Пустое ведро (Снизу слева от стрелки)
        addSlot(new Slot(blockEntity.inventory, 2, 55, 55));

        // Слот 3: Результат (Справа от стрелки)
        addSlot(new Slot(blockEntity.inventory, 3, 110, 55) {
            @Override public boolean mayPlace(ItemStack stack) { return false; }
        });

        addDataSlots(data);
        addPlayerInventory(inv);
        addPlayerHotbar(inv);
    }

    // --- Данные для экрана ---
    public int getWaterLevel() {
        return this.data.get(2);
    }

    public int getScaledProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowSize = 24; // Длина стрелки в пикселях
        return maxProgress != 0 && progress != 0 ? progress * arrowSize / maxProgress : 0;
    }

    public int getScaledWater() {
        int water = this.data.get(2);
        int maxWater = 10;
        int tankHeight = 50; // Высота пробирки в пикселях
        return maxWater != 0 && water != 0 ? water * tankHeight / maxWater : 0;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(ContainerLevelAccess.create(blockEntity.getLevel(), blockEntity.getBlockPos()), player, ModBlocks.MALT_VAT.get());
    }

    // Стандартные методы инвентаря игрока
    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l) {
                this.addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
            }
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i) {
            this.addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        return ItemStack.EMPTY; // Упрощено для стабильности
    }
}
