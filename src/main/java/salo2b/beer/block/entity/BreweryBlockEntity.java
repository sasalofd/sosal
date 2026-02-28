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
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BreweryBlockEntity extends BlockEntity {

    private int wortLevel = 0;
    private ItemStack ingredient = ItemStack.EMPTY;
    private ItemStack result = ItemStack.EMPTY;

    public int servings = 0;

    private int brewTime = 0;
    private static final int MAX_BREW_TIME = 400;

    public BreweryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREWERY_BE.get(), pos, state);
    }

    public int getWortLevel() { return wortLevel; }
    public ItemStack getResult() { return result; }

    // НОВЫЙ МЕТОД: Проверяет, есть ли ингредиент внутри (для текста)
    public boolean hasIngredient() {
        return !ingredient.isEmpty();
    }

    public int getBrewingStage() {
        if (!result.isEmpty()) return 5;
        if (brewTime <= 0) return 0;
        float progress = (float) brewTime / MAX_BREW_TIME;
        return (int) (progress * 5) + 1;
    }

    // 1. Добавление сусла
    public boolean addWort() {
        if (this.wortLevel < 6 && this.result.isEmpty() && this.brewTime == 0) {
            this.wortLevel++;
            updateBlockVisuals(); // Обновляем вид блока сразу
            return true;
        }
        return false;
    }

    // 2. Добавление ингредиента
    public boolean addIngredient(ItemStack stack) {
        if (this.ingredient.isEmpty() && this.result.isEmpty() && this.wortLevel > 0) {
            if (stack.is(ModItems.HOPS.get()) ||
                    stack.is(ModItems.GREEN_APPLE.get()) ||
                    stack.is(ModItems.BARLEY.get())) {

                this.ingredient = stack.copy();
                this.ingredient.setCount(1);
                setChanged(); // Сохраняем, но визуал блока менять не обязательно (ингредиент не видно снаружи)
                return true;
            }
        }
        return false;
    }

    // 3. Забрать результат (ИСПРАВЛЕНА СИНХРОНИЗАЦИЯ)
    public ItemStack takeResult() {
        if (!this.result.isEmpty() && this.servings > 0) {
            ItemStack output = this.result.copy();
            output.setCount(1);

            this.servings--;

            if (this.servings <= 0) {
                this.result = ItemStack.EMPTY;
                this.brewTime = 0;
                // Если сусла не осталось, сбрасываем всё
                if (this.wortLevel == 0) {
                    // Текстура: Пусто
                }
            }

            // ВАЖНО: Принудительно обновляем текстуру блока после взятия
            updateBlockVisuals();
            return output;
        }
        return ItemStack.EMPTY;
    }

    // --- ЛОГИКА ВАРКИ ---
    public static void tick(Level level, BlockPos pos, BlockState state, BreweryBlockEntity be) {
        if (level.isClientSide) return;

        if (!be.result.isEmpty()) return;

        if (be.wortLevel >= 2 && !be.ingredient.isEmpty()) {
            be.brewTime++;

            if (be.brewTime % 20 == 0) be.setChanged();

            if (be.brewTime >= MAX_BREW_TIME) {
                // ВАРКА ЗАКОНЧЕНА
                be.brewTime = 0;
                be.wortLevel -= 2;

                if (be.ingredient.is(ModItems.HOPS.get())) {
                    be.result = new ItemStack(ModItems.BEER.get());
                } else if (be.ingredient.is(ModItems.GREEN_APPLE.get())) {
                    be.result = new ItemStack(ModItems.CIDER.get());
                } else if (be.ingredient.is(ModItems.BARLEY.get())) {
                    be.result = new ItemStack(ModItems.BARLEY_BEER.get());
                }

                be.servings = 3;
                be.ingredient = ItemStack.EMPTY;

                // ВАЖНО: Принудительно ставим текстуру "Есть пиво"
                be.updateBlockVisuals();
            }
        } else {
            if (be.brewTime > 0) {
                be.brewTime = 0;
                be.setChanged();
            }
        }
    }

    // Метод для синхронизации текстуры блока с данными
    public void updateBlockVisuals() {
        setChanged();
        if (level != null) {
            BlockState currentState = level.getBlockState(worldPosition);

            // Вычисляем визуальный уровень воды (0, 1 или 2) для проперти блока
            int visualWaterLevel = 0;
            if (wortLevel > 0) visualWaterLevel = 1; // Если есть хоть немного - показываем уровень 1
            if (wortLevel >= 8) visualWaterLevel = 2; // Если почти полон - уровень 2

            // Есть ли готовое пиво?
            boolean hasBeer = !result.isEmpty();

            // Применяем состояние к блоку
            level.setBlock(worldPosition, currentState
                    .setValue(BreweryBlock.WATER_LEVEL, visualWaterLevel)
                    .setValue(BreweryBlock.HAS_BEER, hasBeer), 3);

            // Шлем пакет обновления данных
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("wortLevel", wortLevel);
        tag.putInt("brewTime", brewTime);
        tag.putInt("servings", servings);
        if (!ingredient.isEmpty()) tag.put("ingredient", ingredient.save(registries));
        if (!result.isEmpty()) tag.put("result", result.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        wortLevel = tag.getInt("wortLevel");
        brewTime = tag.getInt("brewTime");
        servings = tag.getInt("servings");
        if (tag.contains("ingredient")) ingredient = ItemStack.parseOptional(registries, tag.getCompound("ingredient"));
        if (tag.contains("result")) result = ItemStack.parseOptional(registries, tag.getCompound("result"));
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
