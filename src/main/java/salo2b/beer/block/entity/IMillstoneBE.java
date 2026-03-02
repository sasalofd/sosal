package salo2b.beer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.items.ItemStackHandler;

public interface IMillstoneBE {
    ItemStackHandler getInventory();
    BlockPos getBlockPos();
    Level getLevel();
}
