package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;

public class AppleFruitLeavesBlock extends Block {
    // Стадии: 0 - пусто, 1 - маленькое яблоко, 2 - спелое яблоко
    public static final IntegerProperty AGE = BlockStateProperties.AGE_2;

    public AppleFruitLeavesBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    // Метод случайного тика (рост)
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        int age = state.getValue(AGE);
        if (age < 2 && random.nextInt(5) == 0) { // Шанс роста 1 к 5
            level.setBlock(pos, state.setValue(AGE, age + 1), 3);
        }
    }

    // Сбор яблок через ПКМ
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        int age = state.getValue(AGE);
        if (age == 2) { // Если яблоко спелое
            // Выдаем яблоко
            popResource(level, pos, new ItemStack(ModItems.GREEN_APPLE.get()));
            // Сбрасываем стадию на 0
            level.setBlock(pos, state.setValue(AGE, 0), 3);
            // Звук сбора
            level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0f, 1.0f);
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }
}
