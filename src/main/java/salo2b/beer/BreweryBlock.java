package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> pBuilder) {
        pBuilder.add(FACING, WATER_LEVEL, HAS_BEER);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (state.getValue(HAS_BEER)) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof BreweryBlockEntity brewery) {
            if (brewery.getWaterLevel() == 2 && brewery.getHopsCount() >= 5) {
                if (random.nextInt(2) == 0) {
                    double x = (double)pos.getX() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;
                    double y = (double)pos.getY() + 0.9D;
                    double z = (double)pos.getZ() + 0.5D + (random.nextDouble() - 0.5D) * 0.3D;
                    level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 0.0D, 0.03D, 0.0D);
                }

                if (random.nextInt(20) == 0) {
                    level.playLocalSound(pos.getX(), pos.getY(), pos.getZ(),
                            SoundEvents.LAVA_POP,
                            SoundSource.BLOCKS,
                            0.5F,
                            0.8F + random.nextFloat() * 0.2F,
                            false);
                }
            }
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext pContext) {
        return this.defaultBlockState().setValue(FACING, pContext.getHorizontalDirection().getOpposite());
    }

    @Override
    public RenderShape getRenderShape(BlockState pState) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BreweryBlockEntity brewery) {
                if (stack.is(Items.WATER_BUCKET)) {
                    if (brewery.addWater()) {
                        int newLevel = brewery.getWaterLevel();
                        level.setBlock(pos, state.setValue(WATER_LEVEL, newLevel).setValue(HAS_BEER, false), 3);
                        level.playSound(null, pos, SoundEvents.BUCKET_EMPTY, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!player.isCreative()) player.setItemInHand(hand, new ItemStack(Items.BUCKET));
                        player.displayClientMessage(Component.translatable("message.beer.water_added", newLevel).withStyle(ChatFormatting.AQUA), true);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                else if (stack.is(ModItems.HOPS.get())) {
                    if (brewery.addHops()) {
                        level.playSound(null, pos, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                        if (!player.isCreative()) stack.shrink(1);
                        player.displayClientMessage(Component.translatable("message.beer.hops_added", brewery.getHopsCount()).withStyle(ChatFormatting.GREEN), true);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
                else if (stack.is(ModBlocks.WOODEN_MUG.get().asItem())) {
                    if (brewery.takeBeer()) {
                        level.playSound(null, pos, SoundEvents.GENERIC_DRINK, SoundSource.BLOCKS, 0.5F, 1.0F);
                        if (!player.isCreative()) stack.shrink(1);
                        ItemStack beerStack = new ItemStack(ModItems.BEER.get());
                        if (!player.getInventory().add(beerStack)) {
                            player.drop(beerStack, false);
                        }

                        if (brewery.getBeerCount() == 0) {
                            level.setBlock(pos, state.setValue(WATER_LEVEL, 0).setValue(HAS_BEER, false), 3);
                        }

                        player.displayClientMessage(Component.translatable("message.beer.beer_taken", brewery.getBeerCount()).withStyle(ChatFormatting.GOLD), true);
                        return ItemInteractionResult.SUCCESS;
                    }
                }
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof BreweryBlockEntity brewery) {
                if (brewery.getBeerCount() > 0) {
                    player.displayClientMessage(Component.translatable("message.beer.ready_count", brewery.getBeerCount()).withStyle(ChatFormatting.GREEN), true);
                } else {
                    Component statusMessage = Component.translatable("message.beer.water_added", brewery.getWaterLevel()).withStyle(ChatFormatting.AQUA)
                            .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable("message.beer.hops_added", brewery.getHopsCount()).withStyle(ChatFormatting.GREEN))
                            .append(Component.literal(" | ").withStyle(ChatFormatting.GRAY))
                            .append(Component.translatable("message.beer.brewery_status", brewery.getBrewingStage()).withStyle(ChatFormatting.GOLD));
                    player.displayClientMessage(statusMessage, true);
                }
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

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
}
