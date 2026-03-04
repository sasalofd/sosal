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

        if (pos.getY() > 50) return false;

        for (int i = 0; i < 128; i++) {
            BlockPos currentPos = pos.offset(random.nextInt(32) - 16, random.nextInt(16) - 8, random.nextInt(32) - 16);
            
            if (isValidCeilingLocation(level, currentPos)) {
                // Дистанция 25 блоков для редкости
                if (isTooCloseToOtherSalt(level, currentPos)) continue;
                
                int caveHeight = getCaveHeight(level, currentPos);
                if (caveHeight >= 8) {
                    generateSharpAdaptiveGlacier(level, currentPos, random, caveHeight);
                    return true;
                }
            }
        }

        return false;
    }

    private void generateSharpAdaptiveGlacier(WorldGenLevel level, BlockPos topPos, RandomSource random, int caveHeight) {
        BlockState salt = ModBlocks.SALT_BLOCK.get().defaultBlockState();
        
        boolean isGiant = caveHeight > 30;
        boolean isMedium = caveHeight > 15;
        
        int maxHeight = isGiant ? 18 + random.nextInt(15) : (isMedium ? 7 + random.nextInt(5) : 4 + random.nextInt(3));
        maxHeight = Math.min(maxHeight, caveHeight - 2);
        
        List<BlockPos> placedPositions = new ArrayList<>();
        int waterH = -1;

        // 1. Генерация тела (ЦЕНТР ВСЕГДА ЦЕЛЬНЫЙ)
        for (int h = 0; h < maxHeight; h++) {
            double progress = (double) h / maxHeight;
            int radius;
            
            if (isGiant) {
                if (progress < 0.25) radius = 3;
                else if (progress < 0.5) radius = 2;
                else if (progress < 0.75) radius = 1;
                else radius = 0;
            } else {
                if (progress < 0.3) radius = 2; 
                else if (progress < 0.6) radius = 1; 
                else radius = 0;
            }
            
            if (h >= maxHeight - 1) radius = 0;

            if (waterH == -1 && radius >= 1 && h >= 2) {
                waterH = h;
            }

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    // ЦЕНТРАЛЬНЫЙ СТЕРЖЕНЬ (x=0, z=0) СТАВИМ ВСЕГДА
                    boolean isCenter = (x == 0 && z == 0);
                    
                    if (!isCenter) {
                        // Шум только для боковых блоков
                        if (random.nextFloat() < 0.2f) continue;
                        double d = x * x + z * z;
                        if (d > radius * radius + random.nextFloat() * 0.8) continue;
                    }

                    BlockPos p = topPos.offset(x, -h, z);
                    if (isReplaceable(level, p)) {
                        if (h == waterH && isCenter) continue; // Место для воды
                        
                        level.setBlock(p, salt, 2);
                        placedPositions.add(p);
                    }
                }
            }
        }

        // 2. Размещение ВОДЫ
        if (waterH != -1) {
            BlockPos waterPos = topPos.below(waterH);
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                BlockPos p = waterPos.relative(dir);
                if (level.getBlockState(p).isAir() || level.getBlockState(p).is(Blocks.WATER)) {
                    level.setBlock(p, salt, 2);
                    placedPositions.add(p);
                }
            }
            level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);
        }

        // 3. Спутники (всегда прикреплены к телу)
        int satellites = (isGiant ? 5 : 2) + random.nextInt(2);
        for (int i = 0; i < satellites; i++) {
            Direction dir = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            int dist = (isGiant) ? 2 : 1;
            BlockPos sStart = topPos.relative(dir, dist).below(random.nextInt(5));
            
            if (isReplaceable(level, sStart)) {
                int sHeight = 2 + random.nextInt(maxHeight / 2);
                for (int h = 0; h < sHeight; h++) {
                    if (random.nextFloat() < 0.1f) continue;
                    BlockPos p = sStart.below(h);
                    if (isReplaceable(level, p)) {
                        level.setBlock(p, salt, 2);
                        placedPositions.add(p);
                    }
                }
            }
        }

        // 4. Точки роста
        if (!placedPositions.isEmpty()) {
            placedPositions.sort((a, b) -> Integer.compare(a.getY(), b.getY()));
            level.setBlock(placedPositions.get(0), ModBlocks.BLOOMING_SALT_BLOCK.get().defaultBlockState().setValue(BloomingSaltBlock.AGE, 0), 2);
            
            if (isGiant || maxHeight > 10) {
                int extraPoints = isGiant ? 3 : 1;
                for (int i = 1; i <= extraPoints && i < placedPositions.size(); i++) {
                    if (placedPositions.get(i).getY() < topPos.getY() - (maxHeight / 3)) {
                        level.setBlock(placedPositions.get(i), ModBlocks.BLOOMING_SALT_BLOCK.get().defaultBlockState().setValue(BloomingSaltBlock.AGE, 0), 2);
                    }
                }
            }
        }
    }

    private int getCaveHeight(WorldGenLevel level, BlockPos pos) {
        int h = 0;
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos().set(pos);
        while (h < 64) {
            mutable.move(Direction.DOWN);
            if (!level.isEmptyBlock(mutable) && !level.getBlockState(mutable).is(Blocks.WATER)) break;
            h++;
        }
        return h;
    }

    private boolean isTooCloseToOtherSalt(WorldGenLevel level, BlockPos pos) {
        // Дистанция 25 блоков
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-25, -10, -25), pos.offset(25, 10, 25))) {
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
        if (!state.is(BlockTags.BASE_STONE_OVERWORLD) && !state.is(BlockTags.BASE_STONE_NETHER) && !state.is(Blocks.DEEPSLATE)) return false;
        return level.isEmptyBlock(pos.below()) || level.getBlockState(pos.below()).is(Blocks.WATER);
    }
}
