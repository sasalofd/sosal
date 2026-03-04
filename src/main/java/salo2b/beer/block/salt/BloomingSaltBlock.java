package salo2b.beer.block.salt;

import salo2b.beer.registration.ModBlocks;
import salo2b.beer.registration.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.storage.loot.LootParams;

import java.util.List;

public class BloomingSaltBlock extends Block {
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    public BloomingSaltBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        // Растет только если рядом есть вода (в радиусе 2 блоков)
        if (!isNearWater(level, pos)) return;

        int age = state.getValue(AGE);
        if (age < 3) {
            if (random.nextInt(2) == 0) {
                level.setBlock(pos, state.setValue(AGE, age + 1), 2);
            }
        } else {
            if (random.nextFloat() < 0.17578125f) {
                maybeFillCauldron(level, pos);
            }
        }
    }

    private boolean isNearWater(Level level, BlockPos pos) {
        for (BlockPos checkPos : BlockPos.betweenClosed(pos.offset(-2, -2, -2), pos.offset(2, 2, 2))) {
            if (level.getBlockState(checkPos).is(Blocks.WATER)) {
                return true;
            }
        }
        return false;
    }

    private void maybeFillCauldron(ServerLevel level, BlockPos pos) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        mutable.set(pos);
        for (int i = 0; i < 11; i++) {
            mutable.move(Direction.DOWN);
            BlockState stateBelow = level.getBlockState(mutable);
            if (stateBelow.is(Blocks.CAULDRON)) {
                level.setBlock(mutable, Blocks.WATER_CAULDRON.defaultBlockState(), 3);
                break;
            } else if (stateBelow.is(Blocks.WATER_CAULDRON)) {
                int waterLevel = stateBelow.getValue(LayeredCauldronBlock.LEVEL);
                if (waterLevel < 3) {
                    level.setBlock(mutable, stateBelow.setValue(LayeredCauldronBlock.LEVEL, waterLevel + 1), 3);
                }
                break;
            } else if (!stateBelow.isAir() && !stateBelow.is(Blocks.WATER)) {
                break;
            }
        }
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(AGE) == 3) {
            if (random.nextInt(10) == 0) {
                double x = pos.getX() + random.nextDouble();
                double y = pos.getY() - 0.05D;
                double z = pos.getZ() + random.nextDouble();
                level.addParticle(ParticleTypes.DRIPPING_WATER, x, y, z, 0.0D, 0.0D, 0.0D);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder params) {
        if (state.getValue(AGE) == 3) {
            return List.of(new ItemStack(ModItems.SALT_CRYSTAL.get(), 1));
        }
        return List.of(new ItemStack(ModBlocks.SALT_BLOCK.get()));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
