package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class HopsCropBlock extends CropBlock {
    // Максимальный возраст = 3
    public static final int MAX_AGE = 3;
    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;

    private static final VoxelShape[] SHAPE_BY_AGE = new VoxelShape[]{
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 2.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 6.0D, 16.0D),
            Block.box(0.0D, 0.0D, 0.0D, 16.0D, 8.0D, 16.0D)
    };

    public HopsCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    public IntegerProperty getAgeProperty() {
        return AGE;
    }

    @Override
    public int getMaxAge() {
        return MAX_AGE;
    }

    @Override
    protected ItemLike getBaseSeedId() {
        return ModItems.HOPS_SEEDS.get();
    }

    @Override
    protected int getBonemealAgeIncrease(Level level) {
        return level.random.nextFloat() < 0.5F ? 1 : 2;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE_BY_AGE[state.getValue(this.getAgeProperty())];
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    // --- СБОР НА ПКМ ---
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        int age = state.getValue(AGE);

        // Если растение полностью выросло (3 стадия)
        if (age == MAX_AGE) {
            if (!level.isClientSide) {
                // 1. Выбрасываем 2 хмеля (дроп)
                popResource(level, pos, new ItemStack(ModItems.HOPS.get(), 2));

                // Шанс на семена (если хотите, можно убрать)
                if (level.random.nextInt(2) == 0) {
                    popResource(level, pos, new ItemStack(ModItems.HOPS_SEEDS.get(), 1));
                }

                // 2. Играем звук сбора ягод
                level.playSound(null, pos, SoundEvents.SWEET_BERRY_BUSH_PICK_BERRIES, SoundSource.BLOCKS, 1.0F, 1.0F);

                // 3. Сбрасываем рост на 0
                level.setBlock(pos, state.setValue(AGE, 0), 2);
            }
            // Возвращаем SUCCESS (действие выполнено, рука дернулась)
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        // Если не выросло - возвращаем PASS (чтобы работала костная мука)
        return super.use(state, level, pos, player, hand, hit);
    }
}
