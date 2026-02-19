package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BeerBarrelBlockEntity extends BlockEntity {
    private int timer = 0;
    private int mugsCount = 0;

    // СКОРОСТЬ БРОЖЕНИЯ
    private static final int TIME_FILTERED = 200; // 10 секунд
    private static final int TIME_LIGHT = 600;    // 30 секунд

    public BeerBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEER_BARREL_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BeerBarrelBlockEntity be) {
        if (level.isClientSide) return;

        // Брожение идет, если в бочке есть ХОТЯ БЫ ОДНА кружка
        if (be.mugsCount > 0) {
            be.timer++;

            // Бульканье каждые 3 секунды
            if (be.timer % 60 == 0) {
                level.playSound(null, pos, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, SoundSource.BLOCKS, 0.3f, 0.5f);
            }
        } else {
            be.timer = 0; // Если бочка пуста, обнуляем прогресс
        }
    }

    public boolean addMug() {
        if (this.mugsCount < 10) {
            this.mugsCount++;
            // Если ты доливаешь свежее пиво, брожение немного замедляется (сбрасывается на 25%)
            // или оставь таймер как есть, если хочешь, чтобы доливка не мешала.
            // Давай оставим таймер, чтобы бродить продолжало дальше.
            setChanged();
            return true;
        }
        return false;
    }

    public ItemStack takeMug() {
        if (this.mugsCount <= 0) return ItemStack.EMPTY;

        ItemStack result;
        if (timer >= TIME_LIGHT) result = new ItemStack(ModItems.LIGHT_BEER.get());
        else if (timer >= TIME_FILTERED) result = new ItemStack(ModItems.FILTERED_BEER.get());
        else result = new ItemStack(ModItems.BEER.get());

        this.mugsCount--;
        setChanged();
        return result;
    }

    public int getMugsCount() { return mugsCount; }

    public String getStageName() {
        if (mugsCount == 0) return "Пусто";
        if (timer >= TIME_LIGHT) return "Светлое";
        if (timer >= TIME_FILTERED) return "Фильтрованное";
        return "Нефильтрованное";
    }
    // Добавь эти методы в класс BeerBarrelBlockEntity

    // Определяем, какой предмет сейчас в бочке (зависит от стадии)
    public ItemStack getStageItem() {
        if (timer >= TIME_LIGHT) return new ItemStack(ModItems.LIGHT_BEER.get());
        if (timer >= TIME_FILTERED) return new ItemStack(ModItems.FILTERED_BEER.get());
        return new ItemStack(ModItems.BEER.get());
    }

    // Проверяем, можно ли залить это пиво в бочку
    public boolean canFillWith(ItemStack stack) {
        // Если бочка пустая, разрешаем заливать только обычное (нефильтрованное) пиво для старта
        if (mugsCount == 0) return stack.is(ModItems.BEER.get());

        // Если в бочке уже есть пиво, разрешаем доливать только тот же сорт
        return stack.is(getStageItem().getItem());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("beer.timer", timer);
        tag.putInt("beer.mugs", mugsCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.timer = tag.getInt("beer.timer");
        this.mugsCount = tag.getInt("beer.mugs");
    }
}
