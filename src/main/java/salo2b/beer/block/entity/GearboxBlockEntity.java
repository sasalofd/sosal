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
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class GearboxBlockEntity extends BlockEntity {
    public GearboxBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEARBOX.get(), pos, state);
    }

    public boolean isPowered(Level level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockEntity be = level.getBlockEntity(pos.relative(dir));
            if (be instanceof WindmillShaftBlockEntity shaft) {
                if (Math.abs(shaft.angle - shaft.prevAngle) > 0.001f) return true;
            }
            if (be instanceof WindmillRotorBlockEntity) return true;
        }
        return false;
    }
}
