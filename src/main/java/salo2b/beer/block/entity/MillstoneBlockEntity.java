package salo2b.beer.block.entity;

import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModBlocks;
import salo2b.beer.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

import java.util.HashSet;
import java.util.Set;

public class MillstoneBlockEntity extends BlockEntity implements IMillstoneBE {

    public final ItemStackHandler inventory = new ItemStackHandler(2) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    public int progress = 0;
    public final int MAX_PROGRESS = 100;
    public float angle = 0;
    public float prevAngle = 0;
    public boolean isPowered = false;

    public MillstoneBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MILLSTONE.get(), pos, state);
    }

    @Override
    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    public BlockPos getBlockPos() {
        return worldPosition;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MillstoneBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // Периодически обновляем статус питания
        if (level.getGameTime() % 20 == 0) {
            entity.isPowered = isPoweredByRotor(level, pos, state);
        }

        ItemStack input = entity.inventory.getStackInSlot(0);
        ItemStack output = entity.inventory.getStackInSlot(1);

        if (entity.isPowered) {
            entity.angle += 3.0F;
            if (entity.angle >= 360f) {
                entity.angle -= 360f;
                entity.prevAngle -= 360f;
            }

            if (!level.isClientSide && !input.isEmpty()) {
                ItemStack result = getResult(input);
                if (!result.isEmpty() && canInsertResult(output, result)) {
                    entity.progress++;
                    int maxProgress = input.is(ModItems.SALT_CRYSTAL.get()) ? 300 : entity.MAX_PROGRESS;
                    if (entity.progress >= maxProgress) {
                        entity.craftItem(result);
                        entity.progress = 0;
                    }
                }
            }
        } else if (!level.isClientSide && entity.progress > 0) {
            entity.progress = 0;
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    private static ItemStack getResult(ItemStack input) {
        if (input.is(ModItems.MALT.get()) || input.is(ModItems.BARLEY.get())) {
            return new ItemStack(ModItems.CRUSHED_MALT.get());
        }
        if (input.is(ModItems.SALT_CRYSTAL.get())) {
            return new ItemStack(ModItems.SALT.get());
        }
        return ItemStack.EMPTY;
    }

    private static boolean canInsertResult(ItemStack output, ItemStack result) {
        return output.isEmpty() || (ItemStack.isSameItem(output, result) && output.getCount() < output.getMaxStackSize());
    }

    private void craftItem(ItemStack result) {
        ItemStack input = inventory.getStackInSlot(0);
        input.shrink(1);
        inventory.insertItem(1, result, false);
        setChanged();
    }

    private static boolean isPoweredByRotor(Level level, BlockPos pos, BlockState state) {
        for (Direction dir : Direction.values()) {
            if (dir == Direction.DOWN) continue;
            if (checkPowerRecursive(level, pos.relative(dir), 0, new HashSet<>())) {
                return true;
            }
        }
        return false;
    }

    private static boolean checkPowerRecursive(Level level, BlockPos currentPos, int distance, Set<BlockPos> visited) {
        if (distance > 16 || visited.contains(currentPos)) return false;
        visited.add(currentPos);

        BlockState state = level.getBlockState(currentPos);
        if (state.is(ModBlocks.WINDMILL_ROTOR.get())) return true;

        if (state.is(ModBlocks.WINDMILL_SHAFT.get()) || state.is(ModBlocks.GEARBOX.get())) {
            for (Direction dir : Direction.values()) {
                if (checkPowerRecursive(level, currentPos.relative(dir), distance + 1, visited)) {
                    return true;
                }
            }
        }
        return false;
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
        tag.putBoolean("isPowered", isPowered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        progress = tag.getInt("progress");
        isPowered = tag.getBoolean("isPowered");
    }
}
