package salo2b.beer;

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
import salo2b.beer.ModBlockEntities;
import salo2b.beer.ModItems;

public class BreweryBlockEntity extends BlockEntity {

    // Внутренние переменные
    private int wortLevel = 0;      // Уровень сусла (макс 10)
    private ItemStack ingredient = ItemStack.EMPTY; // Какой ингредиент лежит (Хмель/Яблоко/Ячмень)
    private ItemStack result = ItemStack.EMPTY;     // Готовый продукт

    private int brewTime = 0;
    private static final int MAX_BREW_TIME = 400; // 20 секунд

    public BreweryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREWERY_BE.get(), pos, state);
    }

    // --- МЕТОДЫ ДЛЯ ВЗАИМОДЕЙСТВИЯ (ВЫЗЫВАЮТСЯ ИЗ БЛОКА) ---

    public int getWortLevel() { return wortLevel; }
    public ItemStack getResult() { return result; }

    // Показываем прогресс (для сообщения игроку или визуала)
    public int getBrewingStage() {
        if (!result.isEmpty()) return 5; // Готово
        if (brewTime <= 0) return 0;
        float progress = (float) brewTime / MAX_BREW_TIME;
        return (int) (progress * 5) + 1;
    }

    // 1. Добавление сусла (вместо воды)
    public boolean addWort() {
        // Можно залить, если бак не полон и сейчас ничего не варится/не готово
        if (this.wortLevel < 10 && this.result.isEmpty() && this.brewTime == 0) {
            this.wortLevel++;
            updateBlock();
            return true;
        }
        return false;
    }

    // 2. Добавление ингредиента
    public boolean addIngredient(ItemStack stack) {
        // Можно положить, если нет ингредиента, нет готового пива и бак не пуст
        if (this.ingredient.isEmpty() && this.result.isEmpty() && this.wortLevel > 0) {
            // Проверяем, подходит ли предмет
            if (stack.is(ModItems.HOPS.get()) ||
                    stack.is(ModItems.GREEN_APPLE.get()) ||
                    stack.is(ModItems.BARLEY.get())) { // Используй CRUSHED_MALT или BARLEY по желанию

                this.ingredient = stack.copy();
                this.ingredient.setCount(1); // Берем только 1 штуку
                updateBlock();
                return true;
            }
        }
        return false;
    }

    // 3. Забрать результат
    public ItemStack takeResult() {
        if (!this.result.isEmpty()) {
            ItemStack output = this.result.copy();

            // Очищаем результат
            this.result = ItemStack.EMPTY;
            this.brewTime = 0;

            // Сусло тратится во время варки (в методе tick), поэтому тут просто отдаем предмет
            updateBlock();

            // Если сусло кончилось и пиво забрали - обновляем текстуру блока на пустую
            if (this.wortLevel == 0 && level != null) {
                level.setBlock(worldPosition, getBlockState().setValue(BreweryBlock.HAS_BEER, false).setValue(BreweryBlock.WATER_LEVEL, 0), 3);
            }

            return output;
        }
        return ItemStack.EMPTY;
    }

    // --- ЛОГИКА ВАРКИ (TICK) ---
    public static void tick(Level level, BlockPos pos, BlockState state, BreweryBlockEntity be) {
        if (level.isClientSide) return;

        // Условие варки: Есть 2 ведра сусла + Ингредиент + Нет готового результата
        if (be.wortLevel >= 2 && !be.ingredient.isEmpty() && be.result.isEmpty()) {
            be.brewTime++;

            if (be.brewTime % 20 == 0) be.setChanged(); // Сохраняем иногда

            if (be.brewTime >= MAX_BREW_TIME) {
                // ВАРКА ЗАКОНЧЕНА
                be.brewTime = 0;
                be.wortLevel -= 2; // Тратим 2 ведра сусла

                // Определяем, что сварилось
                if (be.ingredient.is(ModItems.HOPS.get())) {
                    be.result = new ItemStack(ModItems.BEER.get());
                } else if (be.ingredient.is(ModItems.GREEN_APPLE.get())) {
                    be.result = new ItemStack(ModItems.CIDER.get());
                } else if (be.ingredient.is(ModItems.BARLEY.get())) {
                    be.result = new ItemStack(ModItems.BARLEY_BEER.get());
                }

                be.ingredient = ItemStack.EMPTY; // Ингредиент исчезает

                // Визуальное обновление блока (полная кружка/бутылка)
                level.setBlock(pos, state.setValue(BreweryBlock.HAS_BEER, true), 3);

                be.updateBlock();
            }
        } else {
            // Если условия нарушились (например, забрали сусло читами), сбрасываем прогресс
            if (be.brewTime > 0) {
                be.brewTime = 0;
                be.setChanged();
            }
        }
    }

    private void updateBlock() {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }

    // --- СОХРАНЕНИЕ ДАННЫХ ---
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("wortLevel", wortLevel);
        tag.putInt("brewTime", brewTime);
        // Сохраняем предметы
        if (!ingredient.isEmpty()) tag.put("ingredient", ingredient.save(registries));
        if (!result.isEmpty()) tag.put("result", result.save(registries));
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        wortLevel = tag.getInt("wortLevel");
        brewTime = tag.getInt("brewTime");
        // Загружаем предметы
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