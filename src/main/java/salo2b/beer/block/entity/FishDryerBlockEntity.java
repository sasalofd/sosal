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
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.item.FishHelper;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModItems;
import java.util.List;

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
    
    // Анимация
    public final float[] swingAngles = new float[4];
    public final float[] swingSpeeds = new float[4];

    public FishDryerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_DRYER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FishDryerBlockEntity be) {
        if (level.isClientSide) {
            for (int i = 0; i < 4; i++) {
                if (!be.inventory.getStackInSlot(i).isEmpty()) {
                    // Гравитация и пружина
                    be.swingSpeeds[i] -= be.swingAngles[i] * 0.15f; 
                    be.swingSpeeds[i] *= 0.85f; // Трение
                    be.swingAngles[i] += be.swingSpeeds[i];
                    
                    // Легкий ветер
                    float wind = (float) Math.sin(level.getGameTime() * 0.05f + i) * 0.015f;
                    be.swingAngles[i] += wind;

                    // Столкновения с игроками
                    double xOffset = pos.getX() + 0.5 + (-0.02 + i * 0.6) - 0.5; // Примерное положение
                    AABB fishBox = new AABB(xOffset - 0.2, pos.getY() + 0.5, pos.getZ() - 0.2, xOffset + 0.2, pos.getY() + 1.5, pos.getZ() + 1.2);
                    List<Player> players = level.getEntitiesOfClass(Player.class, fishBox);
                    if (!players.isEmpty()) {
                        for (Player p : players) {
                            double velZ = p.getDeltaMovement().z;
                            if (Math.abs(velZ) > 0.01) {
                                be.swingSpeeds[i] += (float) (velZ * 0.3);
                            }
                        }
                    }
                }
            }
            return;
        }

        boolean changed = false;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && FishHelper.isSaltedFish(stack)) {
                be.progress[i]++;
                if (be.progress[i] >= DRY_TIME) {
                    net.minecraft.world.item.Item driedVariant = FishHelper.getDriedVariant(stack.getItem());
                    if (driedVariant != null) {
                        be.inventory.setStackInSlot(i, new ItemStack(driedVariant));
                        be.progress[i] = 0;
                        changed = true;
                    }
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
            if (FishHelper.isSaltedFish(stack)) {
                inventory.setStackInSlot(slot, stack.split(1));
                if (level != null && level.isClientSide) {
                    swingSpeeds[slot] = 0.2f; // Толчок при подвешивании
                }
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
