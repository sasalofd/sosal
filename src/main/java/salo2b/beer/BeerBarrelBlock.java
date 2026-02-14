package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class BeerBarrelBlock extends Block {
    public BeerBarrelBlock(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            // Проверка: что под бочкой?
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