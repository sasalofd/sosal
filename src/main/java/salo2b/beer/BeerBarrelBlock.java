package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BeerBarrelBlock extends Block {

    public static final MapCodec<BeerBarrelBlock> CODEC = simpleCodec(BeerBarrelBlock::new);

    public BeerBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockState blockBelow = level.getBlockState(pos.below());
            if (blockBelow.is(Blocks.CAMPFIRE) || blockBelow.is(Blocks.SOUL_CAMPFIRE)) {
                player.sendSystemMessage(Component.literal("Процесс брожения идет... Тепло от костра помогает!"));
            } else {
                player.sendSystemMessage(Component.literal("Слишком холодно. Поставь бочку над костром!"));
            }
        }
        return InteractionResult.SUCCESS;
    }
}
