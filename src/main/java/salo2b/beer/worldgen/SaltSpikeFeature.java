package salo2b.beer.worldgen;

import com.mojang.serialization.Codec;
import salo2b.beer.registration.ModBlocks;
import salo2b.beer.block.salt.SaltCrystalBlock;
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

        for (int i = 0; i < 64; i++) {
            BlockPos currentPos = pos.offset(random.nextInt(32) - 16, random.nextInt(16) - 8, random.nextInt(32) - 16);
            
            if (isValidCeilingLocation(level, currentPos)) {
                if (isTooCloseToOtherSalt(level, currentPos)) continue;
                
                int caveHeight = getCaveHeight(level, currentPos);
                if (caveHeight >= 8) {
                    generateMassiveSharpGlacier(level, currentPos, random, caveHeight);
                    return true;
                }
            }
        }

        return false;
    }

    private void generateMassiveSharpGlacier(WorldGenLevel level, BlockPos topPos, RandomSource random, int caveHeight) {
        BlockState salt = ModBlocks.SALT_BLOCK.get().defaultBlockState();
        
        boolean isGiant = caveHeight > 30;
        int maxHeight = isGiant ? 15 + random.nextInt(10) : 6 + random.nextInt(4);
        maxHeight = Math.min(maxHeight, caveHeight - 2);
        
        List<BlockPos> placedPositions = new ArrayList<>();

        // 1. Генерация ГЕРМЕТИЧНОГО конуса
        for (int h = 0; h < maxHeight; h++) {
            double progress = (double) h / maxHeight;
            int radius = isGiant ? (progress < 0.3 ? 3 : (progress < 0.6 ? 2 : (progress < 0.85 ? 1 : 0)))
                                : (progress < 0.4 ? 2 : (progress < 0.8 ? 1 : 0));
            
            if (h >= maxHeight - 1) radius = 0;

            for (int x = -radius; x <= radius; x++) {
                for (int z = -radius; z <= radius; z++) {
                    boolean isCore = (Math.abs(x) <= 1 && Math.abs(z) <= 1);
                    if (!isCore && random.nextFloat() < 0.2f) continue;

                    if (x * x + z * z <= radius * radius + 0.8) {
                        BlockPos p = topPos.offset(x, -h, z);
                        if (isReplaceable(level, p)) {
                            level.setBlock(p, salt, 2);
                            placedPositions.add(p);
                        }
                    }
                }
            }
        }

        // 2. Интеграция воды ВНУТРЬ (на h=2)
        BlockPos waterPos = topPos.below(2);
        level.setBlock(waterPos, Blocks.WATER.defaultBlockState(), 2);

        // 3. Точки роста (Blooming Salt) на кончиках
        if (!placedPositions.isEmpty()) {
            placedPositions.sort((a, b) -> Integer.compare(a.getY(), b.getY()));
            int bloomCount = isGiant ? 3 : 1;
            for (int i = 0; i < Math.min(bloomCount, placedPositions.size()); i++) {
                // Основной Blooming блок на самом кончике
                BlockPos tip = placedPositions.get(i);
                level.setBlock(tip, ModBlocks.BLOOMING_SALT_BLOCK.get().defaultBlockState(), 2);

                // 1. Всегда растим кристалл ВНИЗ (100% шанс)
                BlockPos downPos = tip.below();
                if (level.isEmptyBlock(downPos) || level.getBlockState(downPos).is(Blocks.WATER)) {
                    BlockState downState = ModBlocks.SALT_CRYSTAL.get().defaultBlockState()
                            .setValue(SaltCrystalBlock.FACING, Direction.DOWN)
                            .setValue(SaltCrystalBlock.AGE, random.nextInt(4))
                            .setValue(SaltCrystalBlock.WATERLOGGED, level.getBlockState(downPos).is(Blocks.WATER));
                    if (downState.canSurvive(level, downPos)) {
                        level.setBlock(downPos, downState, 2);
                    }
                }

                // 2. Растим кристаллы на остальных случайных гранях (кроме нижней)
                for (Direction dir : Direction.values()) {
                    if (dir == Direction.DOWN) continue; // Нижний уже обработан
                    
                    if (random.nextFloat() < 0.2f) { // 20% шанс для остальных граней
                        BlockPos crystalPos = tip.relative(dir);
                        if (level.isEmptyBlock(crystalPos) || level.getBlockState(crystalPos).is(Blocks.WATER)) {
                            BlockState crystalState = ModBlocks.SALT_CRYSTAL.get().defaultBlockState()
                                    .setValue(SaltCrystalBlock.FACING, dir)
                                    .setValue(SaltCrystalBlock.AGE, random.nextInt(4))
                                    .setValue(SaltCrystalBlock.WATERLOGGED, level.getBlockState(crystalPos).is(Blocks.WATER));

                            if (crystalState.canSurvive(level, crystalPos)) {
                                level.setBlock(crystalPos, crystalState, 2);
                            }
                        }
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
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-15, -5, -15), pos.offset(15, 5, 15))) {
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
