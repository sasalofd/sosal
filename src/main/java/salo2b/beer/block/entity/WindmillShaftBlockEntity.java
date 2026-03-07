package salo2b.beer.block.entity;

import salo2b.beer.block.WindmillShaftBlock;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class WindmillShaftBlockEntity extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    public boolean isPowered = false;

    public WindmillShaftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDMILL_SHAFT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindmillShaftBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // Каждые 20 тиков (1 секунда) перепроверяем питание более тщательно
        if (level.getGameTime() % 20 == 0) {
            entity.isPowered = isPoweredBySource(level, pos, state);
        }

        if (entity.isPowered) {
            entity.angle += 2.0F;
            if (entity.angle >= 360) {
                entity.angle -= 360;
                entity.prevAngle -= 360;
            }
        }
    }

    private static boolean isPoweredBySource(Level level, BlockPos pos, BlockState state) {
        // Проверяем всех соседей на наличие источника энергии
        for (Direction dir : Direction.values()) {
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
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("isPowered", this.isPowered);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.isPowered = tag.getBoolean("isPowered");
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
