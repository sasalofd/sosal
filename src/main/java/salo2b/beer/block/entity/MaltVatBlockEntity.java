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
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;

public class MaltVatBlockEntity extends BlockEntity implements MenuProvider {

    public final SimpleContainer inventory = new SimpleContainer(4) {
        @Override
        public void setChanged() {
            super.setChanged();
            MaltVatBlockEntity.this.setChanged();
        }
    };

    // Бак для воды (10 ведер = 10000 mB) - ВХОД
    public final FluidTank waterTank = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
            if (level != null && !level.isClientSide) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
        @Override
        public boolean isFluidValid(FluidStack stack) {
            return stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER;
        }
    };

    // Бак для сусла (10 ведер = 10000 mB) - ВЫХОД
    public final FluidTank wortTank = new FluidTank(10000) {
        @Override
        protected void onContentsChanged() {
            setChanged();
        }
    };

    // Комбинированный обработчик для труб (Вход - вода, Выход - сусло)
    public final IFluidHandler fluidHandler = new IFluidHandler() {
        @Override
        public int getTanks() { return 2; }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? waterTank.getFluid() : wortTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return tank == 0 ? waterTank.getCapacity() : wortTank.getCapacity();
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 && stack.getFluid() == net.minecraft.world.level.material.Fluids.WATER;
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == net.minecraft.world.level.material.Fluids.WATER) {
                return waterTank.fill(resource, action);
            }
            return 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            if (resource.getFluid() == ModFluids.WORT_SOURCE.get()) {
                return wortTank.drain(resource, action);
            }
            return FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return wortTank.drain(maxDrain, action);
        }
    };

    private final IItemHandler itemHandler = new InvWrapper(inventory);

    protected final ContainerData data = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> MaltVatBlockEntity.this.progress;
                case 1 -> MaltVatBlockEntity.this.maxProgress;
                case 2 -> MaltVatBlockEntity.this.waterTank.getFluidAmount() / 100; // 0-100 for GUI
                case 3 -> MaltVatBlockEntity.this.wortTank.getFluidAmount() / 100;  // 0-100 for GUI
                default -> 0;
            };
        }
        @Override
        public void set(int index, int value) {
            switch (index) {
                case 0 -> MaltVatBlockEntity.this.progress = value;
                case 1 -> MaltVatBlockEntity.this.maxProgress = value;
                case 2, 3 -> {} 
            }
        }
        @Override
        public int getCount() { return 4; }
    };

    public int progress = 0;
    public int maxProgress = 400; 

    public MaltVatBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MALT_VAT_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MaltVatBlockEntity entity) {
        if (level.isClientSide) return;

        // 1. ПОПОЛНЕНИЕ ВОДЫ ИЗ ВЕДРА В GUI (Слот 1)
        ItemStack waterBucketInput = entity.inventory.getItem(1);
        if (waterBucketInput.is(Items.WATER_BUCKET) && entity.waterTank.getSpace() >= 1000) {
            entity.inventory.setItem(1, new ItemStack(Items.BUCKET));
            entity.waterTank.fill(new FluidStack(net.minecraft.world.level.material.Fluids.WATER, 1000), IFluidHandler.FluidAction.EXECUTE);
            entity.setChanged();
        }

        ItemStack inputIngredient = entity.inventory.getItem(0); 
        ItemStack emptyBucketSlot = entity.inventory.getItem(2); 
        ItemStack outputSlot = entity.inventory.getItem(3);      

        boolean hasIngredients = inputIngredient.is(ModItems.CRUSHED_MALT.get()) && inputIngredient.getCount() >= 3;
        boolean hasWater = entity.waterTank.getFluidAmount() >= 1000;
        
        // 2. ВАРКА СУСЛА В ЖИДКОСТЬ
        if (hasIngredients && hasWater && entity.wortTank.getSpace() >= 1000) {
            entity.progress++;
            if (entity.progress >= entity.maxProgress) {
                inputIngredient.shrink(3);
                entity.waterTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
                // Создаем жидкое сусло в баке
                entity.wortTank.fill(new FluidStack(ModFluids.WORT_SOURCE.get(), 1000), IFluidHandler.FluidAction.EXECUTE);
                entity.progress = 0;
                entity.setChanged();
            }
        } else if (entity.progress > 0) {
            entity.progress = 0;
            entity.setChanged();
        }

        // 3. РАЗЛИВ СУСЛА В ВЕДРА ДЛЯ ИГРОКА (если есть пустое ведро)
        if (entity.wortTank.getFluidAmount() >= 1000 && !emptyBucketSlot.isEmpty() && emptyBucketSlot.is(Items.BUCKET) && outputSlot.isEmpty()) {
            entity.wortTank.drain(1000, IFluidHandler.FluidAction.EXECUTE);
            emptyBucketSlot.shrink(1);
            entity.inventory.setItem(3, new ItemStack(ModItems.WORT_BUCKET.get()));
            entity.setChanged();
        }
    }

    // --- МЕНЮ И СОХРАНЕНИЕ ---
    @Override
    public Component getDisplayName() { return Component.literal("Сусловый чан"); }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new MaltVatMenu(id, inventory, this, this.data);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.createTag(registries));
        tag.put("waterTank", waterTank.writeToNBT(registries, new CompoundTag()));
        tag.put("wortTank", wortTank.writeToNBT(registries, new CompoundTag()));
        tag.putInt("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        inventory.fromTag(tag.getList("inventory", 10), registries);
        if (tag.contains("waterTank")) {
            waterTank.readFromNBT(registries, tag.getCompound("waterTank"));
        }
        if (tag.contains("wortTank")) {
            wortTank.readFromNBT(registries, tag.getCompound("wortTank"));
        }
        progress = tag.getInt("progress");
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
