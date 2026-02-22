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

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public float angle = 0;
    public float prevAngle = 0;
    private int progress = 0;
    private final int MAX_PROGRESS = 100;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // 1. ПРОВЕРКА ПИТАНИЯ
        BlockEntity above = level.getBlockEntity(pos.above());
        boolean hasPower = false;

        if (above instanceof WindmillShaftBlockEntity shaft && shaft.isSpinning()) {
            hasPower = true;
        } else if (above instanceof GearboxBlockEntity gearbox && gearbox.isPowered(level, pos.above())) {
            hasPower = true;
        }

        ItemStack input = entity.inventory.getStackInSlot(0);
        ItemStack output = entity.inventory.getStackInSlot(1);

        // 2. УСЛОВИЕ РАБОТЫ
        if (hasPower && !input.isEmpty() && input.is(ModItems.MALT.get()) && canInsertResult(output)) {
            entity.angle += 3.0F;

            if (!level.isClientSide) {
                entity.progress++;
                if (entity.progress >= entity.MAX_PROGRESS) {
                    entity.craftItem();
                    entity.progress = 0;
                }
            }
        } else {
            entity.progress = 0;
        }
    }

    // ВАЖНО: Этот метод проверяет, есть ли место для дробленого солода
    private static boolean canInsertResult(ItemStack output) {
        return output.isEmpty() || (output.is(ModItems.CRUSHED_MALT.get()) && output.getCount() < output.getMaxStackSize());
    }

    private void craftItem() {
        ItemStack input = inventory.getStackInSlot(0);
        ItemStack result = new ItemStack(ModItems.CRUSHED_MALT.get());

        if (!input.isEmpty()) {
            input.shrink(1); // Забираем 1 солод
            ItemStack output = inventory.getStackInSlot(1);
            if (output.isEmpty()) {
                inventory.setStackInSlot(1, result);
            } else {
                output.grow(1); // Увеличиваем стак дробленого солода
            }
            setChanged(); // Помечаем блок как измененный для сохранения
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