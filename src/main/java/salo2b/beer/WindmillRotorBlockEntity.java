package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindmillRotorBlockEntity extends BlockEntity {
    // Поля должны быть public, чтобы рендер их видел
    public float angle = 0;
    public float prevAngle = 0;

    public WindmillRotorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WINDMILL_ROTOR.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, WindmillRotorBlockEntity entity) {
        entity.prevAngle = entity.angle;
        entity.angle += 2.0F; // Скорость вращения
        if (entity.angle >= 360) {
            entity.angle -= 360;
            entity.prevAngle -= 360;
        }
    }
}