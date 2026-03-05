package salo2b.beer.block.salt;

import salo2b.beer.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class BloomingSaltBlock extends Block {
    public BloomingSaltBlock(Properties properties) {
        super(properties);
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Растем только если рядом вода
        if (!isNearWater(level, pos)) return;

        // Шанс роста кристаллов на гранях (шанс 1 к 5)
        if (random.nextInt(5) == 0) {
            Direction dir = Direction.getRandom(random);
            BlockPos crystalPos = pos.relative(dir);
            BlockState currentState = level.getBlockState(crystalPos);
            
            if (currentState.isAir() || currentState.is(net.minecraft.world.level.block.Blocks.WATER)) {
                level.setBlock(crystalPos, ModBlocks.SALT_CRYSTAL.get().defaultBlockState()
                        .setValue(SaltCrystalBlock.FACING, dir)
                        .setValue(SaltCrystalBlock.AGE, 0), 2);
            }
        }
    }

    private boolean isNearWater(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-3, -3, -3), pos.offset(3, 3, 3))) {
            if (level.getBlockState(checkPos).is(net.minecraft.world.level.block.Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }
}
