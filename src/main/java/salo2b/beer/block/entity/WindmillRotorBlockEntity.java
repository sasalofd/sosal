package salo2b.beer.block.entity;

import salo2b.beer.registration.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WindmillRotorBlockEntity extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    private int soundTimer = 0;
    public boolean isSpinning = true;

    public WindmillRotorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDMILL_ROTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindmillRotorBlockEntity entity) {
        entity.isSpinning = true; // Ротор ВСЕГДА генерирует энергию
        entity.prevAngle = entity.angle;
        entity.angle += 2.0F; // Вернул стандартное направление
        if (entity.angle >= 360) {
            entity.angle -= 360;
            entity.prevAngle -= 360;
        }

        if (level.isClientSide) {
            entity.soundTimer++;
            if (entity.soundTimer >= 80) {
                float pitch = 0.5f + level.random.nextFloat() * 0.3f;
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.3f, pitch, false);
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 0.1f, 0.8f, false);
                entity.soundTimer = 0;
            }
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
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isSpinning", isSpinning);
        tag.putFloat("angle", angle);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        isSpinning = tag.getBoolean("isSpinning");
        angle = tag.getFloat("angle");
        prevAngle = angle;
    }
}
