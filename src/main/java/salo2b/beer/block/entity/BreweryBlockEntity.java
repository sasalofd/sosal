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

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.ItemStackHandler;

public class BreweryBlockEntity extends BlockEntity {

    // Бак для сусла (ВХОД)
    public final FluidTank tank = new FluidTank(4000) {
        @Override
        protected void onContentsChanged() {
            updateBlockVisuals();
            setChanged();
        }
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == ModFluids.WORT_SOURCE.get();
        }
    };

    // Слот для ингредиента (хмель, яблоки и т.д.)
    public final ItemStackHandler inventory = new ItemStackHandler(1) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return stack.is(ModItems.HOPS.get()) ||
                   stack.is(ModItems.GREEN_APPLE.get()) ||
                   stack.is(ModItems.BARLEY.get());
        }
    };

    public int servings = 0;
    private int brewTime = 0;
    private static final int MAX_BREW_TIME = 400;
    private ItemStack resultItem = ItemStack.EMPTY;

    public BreweryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREWERY_BE.get(), pos, state);
    }

    public int getWortLevel() { return tank.getFluidAmount() / 500; } // Для старой логики рендеринга
    public boolean hasIngredient() { return !inventory.getStackInSlot(0).isEmpty(); }

    public int getBrewingStage() {
        if (brewTime <= 0) return 0;
        float progress = (float) brewTime / MAX_BREW_TIME;
        return (int) (progress * 5) + 1;
    }

    // 1. Добавление сусла через ведро (ВРУЧНУЮ)
    public boolean addWort() {
        if (tank.getSpace() >= 1000 && brewTime == 0 && servings == 0) {
            tank.fill(new FluidStack(ModFluids.WORT_SOURCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
            return true;
        }
        return false;
    }

    // --- COMPATIBILITY METHODS ---
    public boolean addIngredient(ItemStack stack) {
        if (stack.isEmpty() || servings > 0 || brewTime > 0) return false;
        if (!inventory.getStackInSlot(0).isEmpty()) return false;
        
        ItemStack toInsert = stack.copy();
        toInsert.setCount(1);
        ItemStack remainder = inventory.insertItem(0, toInsert, false);
        return remainder.isEmpty();
    }

    public ItemStack takeResult() {
        if (servings > 0) {
            ItemStack output = getResult().copy();
            if (!output.isEmpty()) {
                servings--;
                if (servings <= 0) {
                    resultItem = ItemStack.EMPTY;
                }
                updateBlockVisuals();
                return output;
            }
        }
        return ItemStack.EMPTY;
    }

    public ItemStack getResult() {
        if (servings > 0) {
            return resultItem.isEmpty() ? new ItemStack(ModItems.BEER.get()) : resultItem;
        }
        return ItemStack.EMPTY;
    }

    public void updateBlockVisuals() {
        setChanged();
        if (level != null) {
            BlockState currentState = level.getBlockState(worldPosition);
            int visualWaterLevel = tank.getFluidAmount() > 0 ? 1 : 0;
            if (tank.getFluidAmount() >= 3000) visualWaterLevel = 2;
            boolean hasBeer = servings > 0;

            level.setBlock(worldPosition, currentState
                    .setValue(BreweryBlock.WATER_LEVEL, visualWaterLevel)
                    .setValue(BreweryBlock.HAS_BEER, hasBeer), 3);
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BreweryBlockEntity be) {
        if (level.isClientSide) return;

        ItemStack ingredient = be.inventory.getStackInSlot(0);
        
        // Условие варки: есть жидкость Сусло (минимум 1000mB) и ингредиент, и нет готового пива
        if (be.tank.getFluidAmount() >= 1000 && !ingredient.isEmpty() && be.servings == 0) {
            be.brewTime++;
            if (be.brewTime >= MAX_BREW_TIME) {
                be.brewTime = 0;
                be.tank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                be.servings = 3; // Выход порций
                
                // Определяем тип пива
                if (ingredient.is(ModItems.HOPS.get())) {
                    be.resultItem = new ItemStack(ModItems.BEER.get());
                } else if (ingredient.is(ModItems.GREEN_APPLE.get())) {
                    be.resultItem = new ItemStack(ModItems.CIDER.get());
                } else if (ingredient.is(ModItems.BARLEY.get())) {
                    be.resultItem = new ItemStack(ModItems.BARLEY_BEER.get());
                } else {
                    be.resultItem = new ItemStack(ModItems.BEER.get());
                }
                
                ingredient.shrink(1);
                be.updateBlockVisuals();
            }
        } else {
            be.brewTime = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("tank", tank.writeToNBT(registries, new CompoundTag()));
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("brewTime", brewTime);
        tag.putInt("servings", servings);
        if (!resultItem.isEmpty()) {
            tag.put("resultItem", resultItem.save(registries));
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("tank")) tank.readFromNBT(registries, tag.getCompound("tank"));
        if (tag.contains("inventory")) inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        brewTime = tag.getInt("brewTime");
        servings = tag.getInt("servings");
        if (tag.contains("resultItem")) {
            resultItem = ItemStack.parse(registries, tag.getCompound("resultItem")).orElse(ItemStack.EMPTY);
        }
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
