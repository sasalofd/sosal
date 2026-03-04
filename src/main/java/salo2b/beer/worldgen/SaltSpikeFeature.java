package salo2b.beer.worldgen;

import com.mojang.serialization.Codec;
import salo2b.beer.registration.ModBlocks;
import salo2b.beer.block.salt.BloomingSaltBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.tags.BlockTags;

import java.util.ArrayList;
import java.util.List;

public class SaltSpikeFeature extends Feature<NoneFeatureConfiguration> {
    public SaltSpikeFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos pos = context.origin();
        RandomSource random = context.random();

        if (pos.getY() > 20) return false;

        for (int i = 0; i < 8; i++) {
            BlockPos currentPos = pos.offset(random.nextInt(16) - 8, random.nextInt(16) - 8, random.nextInt(16) - 8);
            
            if (isValidCeilingLocation(level, currentPos)) {
                if (isTooCloseToOtherSalt(level, currentPos)) continue;
                
                int caveHeight = getCaveHeight(level, currentPos);
                if (caveHeight > 10) {
                    generateAdaptiveGlacier(level, currentPos, random, caveHeight);
                    return true;
                }
            }
        }

        return false;
    }

    private void generateAdaptiveGlacier(WorldGenLevel level, BlockPos topPos, RandomSource random, int caveHeight) {
        BlockState salt = ModBlocks.SALT_BLOCK.get().defaultBlockState();
        
        // Масштабируем параметры под высоту пещеры
        boolean isGiant = caveHeight > 25;
        int maxHeight = isGiant ? 10 + random.nextInt(8) : 4 + random.nextInt(4);
        int startRadius = isGiant ? 3 : 2;
        
        List<BlockPos> placedPositions = new ArrayList<>();

        // 1. Генерация тела пика
        for (int h = 0; h < maxHeight; h++) {
            // Динамический радиус: широкое основание, медленное сужение
            double progress = (double) h / maxHeight;
            int radius;
            if (isGiant) {
                if (progress < 0.2) radius = 3;
                else if (progress < 0.5) radius = 2;
                else if (progress < 0.8) radius = 1;
                else radius = 0;
            } else {
                if (progress < 0.3) radius = 2;
                else if (progress < 0.7) radius = 1;
                else radius = 0;
            }
            
            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    if (x * x + z * z <= radius * radius + random.nextInt(2)) {
                        BlockPos p = topPos.offset(x, -h, z);
                        
                        if (h < 2 && !isAttached(level, p)) continue;

                        if (isReplaceable(level, p)) {
                            // Вода внутри верхней части
                            if (h == 1 && x == 0 && z == 0) {
                                level.setBlock(p, Blocks.WATER.defaultBlockState(), 2);
                            } else {
                                level.setBlock(p, salt, 2);
                                placedPositions.add(p);
                            }
                        }
                    }
                }
            }
        }

        // 2. Спутники для неровности (у гигантов их больше)
        int satellites = (isGiant ? 4 : 2) + random.nextInt(3);
        for (int i = 0; i < satellites; i++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int dist = isGiant ? 2 + random.nextInt(2) : 1 + random.nextInt(2);
            int sHeight = 1 + random.nextInt(isGiant ? 6 : 3);
            BlockPos sStart = topPos.relative(dir, dist).below(random.nextInt(3));
            
            for (int h = 0; h < sHeight; h++) {
                BlockPos p = sStart.below(h);
                if (isReplaceable(level, p) && isAttached(level, p)) {
                    level.setBlock(p, salt, 2);
                    placedPositions.add(p);
                }
            }
        }

        // 3. Размещение точек роста (Blooming Salt)
        if (!placedPositions.isEmpty()) {
            // Для гигантов ставим несколько точек на концах
            if (isGiant) {
                placedPositions.sort((a, b) -> Integer.compare(a.getY(), b.getY()));
                int bloomCount = 2 + random.nextInt(3);
                for (int i = 0; i < Math.min(bloomCount, placedPositions.size()); i++) {
                    level.setBlock(placedPositions.get(i), ModBlocks.BLOOMING_SALT_BLOCK.get().defaultBlockState().setValue(BloomingSaltBlock.AGE, 0), 2);
                }
            } else {
                BlockPos lowest = topPos;
                for (BlockPos p : placedPositions) {
                    if (p.getY() < lowest.getY()) lowest = p;
                }
                level.setBlock(lowest, ModBlocks.BLOOMING_SALT_BLOCK.get().defaultBlockState().setValue(BloomingSaltBlock.AGE, 0), 2);
            }
        }
    }

    private int getCaveHeight(WorldGenLevel level, BlockPos pos) {
        int h = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(pos);
        while (h < 40) {
            mutable.move(Direction.DOWN);
            if (!level.isEmptyBlock(mutable) && !level.getBlockState(mutable).is(Blocks.WATER)) break;
            h++;
        }
        return h;
    }

    private boolean isAttached(WorldGenLevel level, BlockPos pos) {
        for (Direction dir : Direction.values()) {
            BlockState adj = level.getBlockState(pos.relative(dir));
            if (adj.isSolid() || adj.is(ModBlocks.SALT_BLOCK.get())) return true;
        }
        return false;
    }

    private boolean isTooCloseToOtherSalt(WorldGenLevel level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-15, -10, -15), pos.offset(15, 10, 15))) {
            if (level.getBlockState(checkPos).is(ModBlocks.SALT_BLOCK.get()) || level.getBlockState(checkPos).is(ModBlocks.BLOOMING_SALT_BLOCK.get())) {
                return true;
            }
        }
        return false;
    }

    private boolean isReplaceable(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.is(Blocks.WATER) || state.is(BlockTags.REPLACEABLE);
    }

    private boolean isValidCeilingLocation(WorldGenLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (!state.is(BlockTags.BASE_STONE_OVERWORLD)) return false;
        return level.isEmptyBlock(pos.below()) || level.getBlockState(pos.below()).is(Blocks.WATER);
    }
}
