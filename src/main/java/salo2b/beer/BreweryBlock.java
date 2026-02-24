package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.BreweryBlockEntity;

public class BreweryBlock extends BaseEntityBlock {

    public static final MapCodec<BreweryBlock> CODEC = simpleCodec(BreweryBlock::new);

    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty WATER_LEVEL = IntegerProperty.create("water_level", 0, 2);
    public static final BooleanProperty HAS_BEER = BooleanProperty.create("has_beer");

    private static final VoxelShape SHAPE = makeShape();

    private static VoxelShape makeShape() {
        VoxelShape shape = Shapes.empty();
        shape = Shapes.or(shape, Block.box(0, 0, 0, 16, 2, 16));
        shape = Shapes.or(shape, Block.box(0, 2, 0, 16, 16, 2));
        shape = Shapes.or(shape, Block.box(0, 2, 14, 16, 16, 16));
        shape = Shapes.or(shape, Block.box(0, 2, 2, 2, 16, 14));
        shape = Shapes.or(shape, Block.box(14, 2, 2, 16, 16, 14));
        return shape;
    }

    public BreweryBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATER_LEVEL, 0)
                .setValue(HAS_BEER, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATER_LEVEL, HAS_BEER);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.isClientSide) return ItemInteractionResult.SUCCESS;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BreweryBlockEntity brewery) {

            // 1. ЗАЛИВАЕМ СУСЛО
            if (stack.is(ModItems.WORT_BUCKET.get())) {
                if (brewery.addWort()) {
                    level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                    if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                    player.displayClientMessage(Component.literal("§6Сусло добавлено (" + brewery.getWortLevel() + "/6)"), true);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            // 2. ДОБАВЛЯЕМ ИНГРЕДИЕНТЫ
            if (brewery.addIngredient(stack)) {
                level.playSound(null, pos, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 1.0F, 1.0F);
                if (!player.isCreative()) stack.shrink(1);
                player.displayClientMessage(Component.literal("§aИнгредиент добавлен!"), true);
                return ItemInteractionResult.SUCCESS;
            }

            // 3. ЗАБИРАЕМ ГОТОВОЕ ПИВО
            if (stack.is(ModBlocks.WOODEN_MUG.get().asItem())) {
                ItemStack result = brewery.takeResult();
                if (!result.isEmpty()) {
                    level.playSound(null, pos, SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.5F, 1.0F);
                    if (!player.isCreative()) stack.shrink(1);
                    if (!player.getInventory().add(result)) player.drop(result, false);
                    player.displayClientMessage(Component.literal("§6Напиток готов!"), true);
                    return ItemInteractionResult.SUCCESS;
                }
            }

            // 4. ПРОВЕРКА СТАТУСА (Пустой рукой)
            if (stack.isEmpty()) {
                // Если пиво готово
                if (!brewery.getResult().isEmpty()) {
                    String beerName = brewery.getResult().getHoverName().getString();
                    player.displayClientMessage(Component.literal("§aГотово: " + beerName + " §e(" + brewery.servings + " порций)"), true);
                }
                // Если не готово, показываем сусло и ингредиенты
                else {
                    int ingredientCount = brewery.hasIngredient() ? 1 : 0;
                    String status = "§6Сусла: " + brewery.getWortLevel() + " §7| §eИнгредиентов: " + ingredientCount;

                    if (brewery.getBrewingStage() > 0) {
                        status += " §c(Варится...)";
                    }
                    player.displayClientMessage(Component.literal(status), true);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BreweryBlockEntity brewery && brewery.getBrewingStage() > 0 && brewery.getBrewingStage() < 5) {
            if (random.nextInt(5) == 0) {
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, 0, 0.03, 0);
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new BreweryBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) return null;
        return createTickerHelper(type, ModBlockEntities.BREWERY_BE.get(), BreweryBlockEntity::tick);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) { return SHAPE; }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }
}