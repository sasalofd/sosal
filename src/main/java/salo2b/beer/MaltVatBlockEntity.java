package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class MaltVatBlockEntity extends BlockEntity {
    private boolean hasWater = false;
    private boolean hasBarley = false;
    private int progress = 0;

    // 20 минут = 24000 тиков. Для теста можешь поставить 200 (10 секунд)
    private static final int COOK_TIME = 24000;

    public MaltVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MALT_VAT_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaltVatBlockEntity be) {
        if (level.isClientSide) return;

        // Процесс идет только если есть и вода, и ячмень
        if (be.hasWater && be.hasBarley && be.progress < COOK_TIME) {
            be.progress++;

            // Редкий звук кипения (раз в 5 секунд)
            if (be.progress % 100 == 0) {
                level.playSound(null, pos, SoundEvents.LAVA_AMBIENT, SoundSource.BLOCKS, 0.2f, 1.2f);
            }
        }
    }

    public boolean addWater() {
        if (!hasWater) { hasWater = true; setChanged(); return true; }
        return false;
    }

    public boolean addBarley() {
        if (hasWater && !hasBarley) { hasBarley = true; setChanged(); return true; }
        return false;
    }

    public boolean isFinished() {
        return hasWater && hasBarley && progress >= COOK_TIME;
    }

    public void reset() {
        this.hasWater = false;
        this.hasBarley = false;
        this.progress = 0;
        setChanged();
    }

    public String getStatusMessage() {
        if (!hasWater) return "§7Чан пуст. Нужна вода.§r";
        if (!hasBarley) return "§7Нужен ячмень.§r";
        if (progress < COOK_TIME) {
            int percent = (int)((float)progress / COOK_TIME * 100);
            return "§eВарка солода: " + percent + "%§r";
        }
        return "§aСолод готов! Забирай.§r";
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("hasWater", hasWater);
        tag.putBoolean("hasBarley", hasBarley);
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.hasWater = tag.getBoolean("hasWater");
        this.hasBarley = tag.getBoolean("hasBarley");
        this.progress = tag.getInt("progress");
    }
}
