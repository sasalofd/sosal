package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class WindmillShaftBlockEntity extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    public boolean isPowered = false;

    public WindmillShaftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDMILL_SHAFT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindmillShaftBlockEntity entity) {
        entity.prevAngle = entity.angle;

        // ПРОВЕРКА ПИТАНИЯ ДЛЯ ВСЕХ (и для сервера, и для клиента)
        // Теперь твой экран сам понимает, что нужно запустить анимацию!
        boolean currentPower = false;

        if (state.hasProperty(WindmillShaftBlock.FACING)) {
            Direction facing = state.getValue(WindmillShaftBlock.FACING);
            // Ищем источник энергии СЗАДИ вала
            BlockPos sourcePos = pos.relative(facing.getOpposite());
            BlockEntity sourceEntity = level.getBlockEntity(sourcePos);

            if (sourceEntity instanceof WindmillRotorBlockEntity) {
                currentPower = true;
            } else if (sourceEntity instanceof WindmillShaftBlockEntity backShaft) {
                currentPower = backShaft.isPowered;
            } else if (sourceEntity instanceof GearboxBlockEntity gearbox) {
                if (gearbox.isPowered(level, sourcePos)) currentPower = true;
            }
        }

        entity.isPowered = currentPower;

        // АНИМАЦИЯ
        if (entity.isPowered) {
            entity.angle += 2.0F; // Скорость (должна совпадать с ротором)
            if (entity.angle >= 360) {
                entity.angle -= 360;
                entity.prevAngle -= 360;
            }
        }
    }

    public boolean isSpinning() {
        return this.isPowered;
    }

    // --- СОХРАНЕНИЕ ---
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
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}