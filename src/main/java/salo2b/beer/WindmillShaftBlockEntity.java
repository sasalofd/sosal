package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
        // Вал крутиться синхронно з лопатями
        entity.angle += 2.0F;
        if (entity.angle >= 360) {
            entity.angle -= 360;
            entity.prevAngle -= 360;
        }
    }
}