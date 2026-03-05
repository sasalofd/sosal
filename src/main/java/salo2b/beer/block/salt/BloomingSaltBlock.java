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
        if (!isNearWater(level, pos)) return;

        // Добавляем шанс 50%, чтобы замедлить общий рост
        if (random.nextBoolean()) return;

        // 1. Проверяем нижнюю грань в приоритете
        BlockPos downPos = pos.below();
        BlockState downState = level.getBlockState(downPos);
        if (!downState.is(ModBlocks.SALT_CRYSTAL.get())) {
            if (downState.isAir() || downState.is(net.minecraft.world.level.block.Blocks.WATER)) {
                BlockState crystalState = ModBlocks.SALT_CRYSTAL.get().defaultBlockState()
                        .setValue(SaltCrystalBlock.FACING, Direction.DOWN)
                        .setValue(SaltCrystalBlock.AGE, 0)
                        .setValue(SaltCrystalBlock.WATERLOGGED, downState.is(net.minecraft.world.level.block.Blocks.WATER));
                
                if (crystalState.canSurvive(level, downPos)) {
                    level.setBlock(downPos, crystalState, 3);
                    return; // В этом тике достаточно
                }
            }
        }

        // 2. Если снизу уже есть кристалл, пробуем другие грани (лимит 3 всего)
        int crystalCount = 0;
        for (Direction dir : Direction.values()) {
            if (level.getBlockState(pos.relative(dir)).is(ModBlocks.SALT_CRYSTAL.get())) {
                crystalCount++;
            }
        }

        if (crystalCount >= 3) return;

        Direction[] dirs = Direction.values();
        for (int i = dirs.length - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Direction temp = dirs[i];
            dirs[i] = dirs[j];
            dirs[j] = temp;
        }

        for (Direction dir : dirs) {
            if (dir == Direction.DOWN) continue; // Мы его уже проверили

            BlockPos crystalPos = pos.relative(dir);
            BlockState currentState = level.getBlockState(crystalPos);

            if (currentState.isAir() || currentState.is(net.minecraft.world.level.block.Blocks.WATER)) {
                BlockState crystalState = ModBlocks.SALT_CRYSTAL.get().defaultBlockState()
                        .setValue(SaltCrystalBlock.FACING, dir)
                        .setValue(SaltCrystalBlock.AGE, 0)
                        .setValue(SaltCrystalBlock.WATERLOGGED, currentState.is(net.minecraft.world.level.block.Blocks.WATER));
                
                if (crystalState.canSurvive(level, crystalPos)) {
                    level.setBlock(crystalPos, crystalState, 3);
                    break;
                }
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
