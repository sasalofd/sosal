package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindmillShaftBlockEntity extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;

    public WindmillShaftBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDMILL_SHAFT.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindmillShaftBlockEntity entity) {
        entity.prevAngle = entity.angle;

        Direction facing = state.getValue(WindmillShaftBlock.FACING);
        // Ищем источник энергии СЗАДИ вала
        BlockPos sourcePos = pos.relative(facing.getOpposite());
        BlockEntity sourceEntity = level.getBlockEntity(sourcePos);

        boolean hasPower = false;

        if (sourceEntity instanceof WindmillRotorBlockEntity) {
            hasPower = true; // Прямое подключение к ротору
        } else if (sourceEntity instanceof WindmillShaftBlockEntity backShaft) {
            // Если сзади другой вал, проверяем, крутится ли он (используем небольшое отклонение)
            if (Math.abs(backShaft.angle - backShaft.prevAngle) > 0.001f) {
                hasPower = true;
            }
        }
        // ... внутри tick ...
        if (sourceEntity instanceof GearboxBlockEntity gearbox) {
            if (gearbox.isPowered(level, sourcePos)) hasPower = true;
        }

        if (hasPower) {
            entity.angle += 2.0F;
            if (entity.angle >= 360) {
                entity.angle -= 360;
                entity.prevAngle -= 360;
            }
        }
    }

    // Метод для жерновов, чтобы они знали, работает ли механизм
    public boolean isSpinning() {
        return Math.abs(this.angle - this.prevAngle) > 0.001f;
    }
}