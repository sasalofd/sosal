package salo2b.beer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModItems;

public class FishDryerBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final int[] progress = new int[4];
    private static final int DRY_TIME = 2400;

    public FishDryerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_DRYER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FishDryerBlockEntity be) {
        boolean changed = false;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(ModItems.SALTED_FISH.get())) {
                be.progress[i]++;
                if (be.progress[i] >= DRY_TIME) {
                    be.inventory.setStackInSlot(i, new ItemStack(ModItems.DRIED_FISH.get()));
                    be.progress[i] = 0;
                    changed = true;
                }
            } else {
                be.progress[i] = 0;
            }
        }
        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public ItemInteractionResult useItemOnSlot(Player player, InteractionHand hand, ItemStack stack, int slot) {
        ItemStack slotStack = inventory.getStackInSlot(slot);
        if (slotStack.isEmpty()) {
            if (stack.is(ModItems.SALTED_FISH.get())) {
                inventory.setStackInSlot(slot, stack.split(1));
                return ItemInteractionResult.SUCCESS;
            }
        } else {
            if (player.isShiftKeyDown() || stack.isEmpty()) {
                ItemStack taken = inventory.getStackInSlot(slot).copy();
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putIntArray("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        int[] savedProgress = tag.getIntArray("progress");
        if (savedProgress.length == 4) {
            System.arraycopy(savedProgress, 0, progress, 0, 4);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
