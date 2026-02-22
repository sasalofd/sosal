package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class WindmillRotorBlockEntity extends BlockEntity {
    public float angle = 0;
    public float prevAngle = 0;
    private int soundTimer = 0; // Таймер для звука

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

        // Логика звука (только на стороне клиента)
        if (level.isClientSide) {
            entity.soundTimer++;

            // Проигрываем звук каждые 80 тиков (примерно раз в 4 секунды)
            if (entity.soundTimer >= 80) {
                // Выбираем случайную высоту звука, чтобы он не надоедал
                float pitch = 0.5f + level.random.nextFloat() * 0.3f;

                // Звук скрипа (используем замедленный звук сундука или калитки)
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.3f, pitch, false);

                // Звук ветра (тихий гул)
                level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                        SoundEvents.ELYTRA_FLYING, SoundSource.BLOCKS, 0.1f, 0.8f, false);

                entity.soundTimer = 0;
            }
        }
    }
}