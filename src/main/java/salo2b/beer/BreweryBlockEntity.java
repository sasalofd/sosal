package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class BreweryBlockEntity extends BlockEntity {

    private int waterLevel = 0;
    private int hopsCount = 0;
    private int brewTime = 0;
    private int beerCount = 0;

    // 200 тиков = 10 секунд.
    private static final int MAX_BREW_TIME = 200;

    public BreweryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BREWERY_BE.get(), pos, state);
    }

    public int getWaterLevel() { return waterLevel; }
    public int getHopsCount() { return hopsCount; }
    public int getBeerCount() { return beerCount; }

    public int getBrewingStage() {
        if (beerCount > 0) return 5;
        if (brewTime <= 0) return 0;
        
        float progress = (float) brewTime / MAX_BREW_TIME;
        int stage = (int) (progress * 5) + 1;
        return Math.min(stage, 5);
    }

    public boolean addWater() {
        if (this.waterLevel < 2 && this.beerCount == 0 && this.brewTime == 0) {
            this.waterLevel++;
            setChanged();
            // Обновляем клиент
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return true;
        }
        return false;
    }

    public boolean addHops() {
        if (this.hopsCount < 5 && this.beerCount == 0 && this.brewTime == 0) {
            this.hopsCount++;
            setChanged();
            // !!! ВАЖНО: Обновляем клиент, чтобы пошел пар !!!
            if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            return true;
        }
        return false;
    }

    public boolean takeBeer() {
        if (this.beerCount > 0) {
            this.beerCount--;
            if (this.beerCount == 0) {
                this.brewTime = 0;
                this.waterLevel = 0;
                this.hopsCount = 0;
                if (level != null) {
                    level.setBlock(worldPosition, getBlockState().setValue(BreweryBlock.HAS_BEER, false).setValue(BreweryBlock.WATER_LEVEL, 0), 3);
                }
            }
            setChanged();
            return true;
        }
        return false;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BreweryBlockEntity be) {
        if (level.isClientSide) return;

        if (be.waterLevel >= 2 && be.hopsCount >= 5 && be.beerCount == 0) {
            be.brewTime++;

            // Синхронизация прогресса (раз в секунду)
            if (be.brewTime % 20 == 0) {
                be.setChanged();
            }

            if (be.brewTime >= MAX_BREW_TIME) {
                be.brewTime = 0;
                be.hopsCount = 0;
                be.beerCount = 4;

                level.setBlock(pos, state.setValue(BreweryBlock.HAS_BEER, true), 3);
                
                be.setChanged();
                // Финальное обновление
                level.sendBlockUpdated(pos, state, state, 3);
            }
        } else {
            if (be.brewTime > 0) {
                be.brewTime = 0;
                be.setChanged();
            }
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("water", waterLevel);
        tag.putInt("hops", hopsCount);
        tag.putInt("brewTime", brewTime);
        tag.putInt("beer", beerCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        waterLevel = tag.getInt("water");
        hopsCount = tag.getInt("hops");
        brewTime = tag.getInt("brewTime");
        beerCount = tag.getInt("beer");
    }

    @Override
    public CompoundTag getUpdateTag() {
        return saveWithoutMetadata();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}