package salo2b.beer.item;

import salo2b.beer.registration.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

public class HopsSeedsItem extends Item {
    public HopsSeedsItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        // Если кликнули по решетке
        if (state.is(ModBlocks.LATTICE.get())) {
            // Проверяем, можно ли посадить (под решеткой должна быть земля или другая лиана)
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            
            if (belowState.isFaceSturdy(level, belowPos, Direction.UP) || belowState.is(Blocks.FARMLAND)) {
                if (!level.isClientSide) {
                    level.setBlock(pos, ModBlocks.HOPS_VINE.get().defaultBlockState(), 3);
                    context.getItemInHand().shrink(1);
                    level.playSound(null, pos, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                }
                return InteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return InteractionResult.PASS;
    }
}
