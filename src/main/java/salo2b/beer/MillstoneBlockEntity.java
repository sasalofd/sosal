package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public class MillstoneBlockEntity extends BlockEntity {
    // Инвентарь: 0 слот - вход (ячмень), 1 слот - выход (солод/мука)
    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public float angle = 0;
    public float prevAngle = 0;
    private int progress = 0;
    private final int MAX_PROGRESS = 100; // Сколько тиков (5 секунд) мелется одна порция

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // Проверяем, есть ли что перемалывать
        ItemStack input = entity.inventory.getStackInSlot(0);
        // Тут нужно указать твой предмет ячменя. Допустим, это ModItems.BARLEY
        if (!input.isEmpty() && input.is(ModItems.BARLEY.get())) {

            // 1. Анимация вращения (только если идет работа)
            entity.angle += 3.0F;
            if (entity.angle >= 360) {
                entity.angle -= 360;
                entity.prevAngle -= 360;
            }

            // 2. Логика прогресса (только на сервере)
            if (!level.isClientSide) {
                entity.progress++;
                if (entity.progress >= entity.MAX_PROGRESS) {
                    entity.craftItem();
                    entity.progress = 0;
                }
            }
        } else {
            entity.progress = 0; // Сбрасываем, если ячмень кончился
        }
    }

    private void craftItem() {
        ItemStack input = inventory.getStackInSlot(0);
        ItemStack result = new ItemStack(ModItems.MALT.get()); // Твой результат (солод)

        input.shrink(1); // Забираем 1 ячмень

        // Кладем результат в выходной слот
        ItemStack output = inventory.getStackInSlot(1);
        if (output.isEmpty()) {
            inventory.setStackInSlot(1, result);
        } else {
            output.grow(1);
        }
    }

    // Сохранение данных при перезагрузке мира
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