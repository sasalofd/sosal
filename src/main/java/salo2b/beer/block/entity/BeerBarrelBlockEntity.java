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
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class BeerBarrelBlockEntity extends BlockEntity {
    private int timer = 0;
    private int mugsCount = 0;

    private static final int TIME_FILTERED = 200;
    private static final int TIME_LIGHT = 600;

    private static final long EXPLOSION_DELAY = 1000L;
    public static final long WARNING_PERIOD = 300L;
    private long targetTime = 0;
    public boolean isFullOfEliteBeer = false;

    public BeerBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BEER_BARREL_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, BeerBarrelBlockEntity be) {
        if (level.isClientSide) {
            // --- ЭМБИЕНТ-ЗВУКИ (Только на клиенте) ---
            
            // 1. Бульканье (очень тихое и редкое, слышно только в упор)
            if (be.mugsCount > 0 && level.getGameTime() % 160 == 0) {
                Player nearestPlayer = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0, false);
                if (nearestPlayer != null) {
                    level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                        SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 0.1f, 0.5f, false);
                }
            }

            if (be.isFullOfEliteBeer && be.targetTime > 0) {
                long timeLeft = be.targetTime - level.getGameTime();
                
                if (timeLeft <= WARNING_PERIOD && timeLeft > 0) {
                    // 2. Постоянное тихое шипение вздутой бочки
                    if (level.getGameTime() % 40 == 0) {
                        level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                            SoundEvents.GENERIC_EXTINGUISH_FIRE, SoundSource.BLOCKS, 0.07f, 0.6f, false);
                    }

                    // СТРУИ ПЕРЕД ВЗРЫВОМ (Брызги меда)
                    if (level.random.nextFloat() < 0.8f) {
                        double angle = level.random.nextDouble() * Math.PI * 2;
                        double verticalAngle = (level.random.nextDouble() - 0.5) * Math.PI;
                        
                        double speed = 0.5 + level.random.nextDouble() * 0.6;
                        
                        double vx = Math.cos(angle) * Math.cos(verticalAngle) * speed;
                        double vy = Math.sin(verticalAngle) * speed + 0.3; 
                        double vz = Math.sin(angle) * Math.cos(verticalAngle) * speed;

                        level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()), 
                            pos.getX() + 0.5 + Math.cos(angle) * 0.45, 
                            pos.getY() + 0.5 + Math.sin(verticalAngle) * 0.45, 
                            pos.getZ() + 0.5 + Math.sin(angle) * 0.45, 
                            vx, vy, vz);
                    }
                }
            }
            return;
        }

        if (be.mugsCount > 0) {
            be.timer++;

            if (be.timer >= TIME_LIGHT && !be.isFullOfEliteBeer) {
                be.isFullOfEliteBeer = true;
                be.targetTime = level.getGameTime() + EXPLOSION_DELAY;
                be.setChanged();
                level.sendBlockUpdated(pos, state, state, 3);
            }

            if (be.isFullOfEliteBeer && be.targetTime > 0) {
                long timeLeft = be.targetTime - level.getGameTime();

                if (timeLeft <= WARNING_PERIOD && !state.getValue(BeerBarrelBlock.SWOLLEN)) {
                    level.setBlock(pos, state.setValue(BeerBarrelBlock.SWOLLEN, true), 3);
                }

                if (level.getGameTime() >= be.targetTime) {
                    // МОЩНЫЙ ВСПЛЕСК ПАРТИКЛОВ В МОМЕНТ ВЗРЫВА
                    if (level instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.HONEY_BLOCK.defaultBlockState()), 
                            pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 
                            150, 0.5, 0.5, 0.5, 0.7);
                    }

                    level.explode(null, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 3.0F, Level.ExplosionInteraction.BLOCK);
                    level.removeBlock(pos, false);
                    return;
                }
            }
        } else {
            if (state.getValue(BeerBarrelBlock.SWOLLEN)) {
                level.setBlock(pos, state.setValue(BeerBarrelBlock.SWOLLEN, false), 3);
            }
            be.timer = 0;
            be.isFullOfEliteBeer = false;
            be.targetTime = 0;
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public float getExpansionScale(float partialTicks) {
        if (!isFullOfEliteBeer || targetTime <= 0 || level == null) return 1.0f;

        long timeLeft = targetTime - level.getGameTime();
        if (timeLeft > WARNING_PERIOD) return 1.0f;
        
        float progress = 1.0f - ((float)timeLeft - partialTicks) / WARNING_PERIOD;
        
        if (timeLeft < 80) {
            float extraProgress = 1.0f - ((float)timeLeft - partialTicks) / 80f;
            return 1.15f + (extraProgress * extraProgress * 0.15f);
        }

        return 1.0f + (progress * 0.15f);
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

    public long getTargetTime() { return targetTime; }
    public long getWarningPeriod() { return WARNING_PERIOD; }

    public boolean addMug() {
        if (this.mugsCount < 10) {
            this.mugsCount++;
            setChanged();
            if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
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
        if (this.mugsCount == 0) {
            this.isFullOfEliteBeer = false;
            this.targetTime = 0;
            this.timer = 0;
        }
        setChanged();
        if (this.level != null) this.level.sendBlockUpdated(this.worldPosition, getBlockState(), getBlockState(), 3);
        return result;
    }

    public int getMugsCount() { return mugsCount; }

    public String getStageName() {
        if (mugsCount == 0) return "Пусто";
        if (timer >= TIME_LIGHT) return "Светлое";
        if (timer >= TIME_FILTERED) return "Фильтрованное";
        return "Нефильтрованное";
    }

    public ItemStack getStageItem() {
        if (timer >= TIME_LIGHT) return new ItemStack(ModItems.LIGHT_BEER.get());
        if (timer >= TIME_FILTERED) return new ItemStack(ModItems.FILTERED_BEER.get());
        return new ItemStack(ModItems.BEER.get());
    }

    public boolean canFillWith(ItemStack stack) {
        if (mugsCount == 0) return stack.is(ModItems.BEER.get());
        return stack.is(getStageItem().getItem());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("beer.timer", timer);
        tag.putInt("beer.mugs", mugsCount);
        tag.putLong("beer.target_time", targetTime);
        tag.putBoolean("beer.is_elite", isFullOfEliteBeer);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.timer = tag.getInt("beer.timer");
        this.mugsCount = tag.getInt("beer.mugs");
        this.targetTime = tag.getLong("beer.target_time");
        this.isFullOfEliteBeer = tag.getBoolean("beer.is_elite");
    }
}
