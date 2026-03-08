package salo2b.beer.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.block.entity.FishDryerBlockEntity;
import salo2b.beer.registration.ModBlockEntities;

import java.util.EnumMap;
import java.util.Map;

public class FishDryerBlock extends BaseEntityBlock {
    public static final MapCodec<FishDryerBlock> CODEC = simpleCodec(FishDryerBlock::new);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final EnumProperty<FishDryerPart> PART = EnumProperty.create("part", FishDryerPart.class);
    public static final EnumProperty<FishDryerHeight> H_PART = EnumProperty.create("height", FishDryerHeight.class);

    private static final Map<Direction, VoxelShape> LEFT_POST = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> RIGHT_POST = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_L_MID = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_M_MID = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_R_MID = new EnumMap<>(Direction.class);
    
    private static final Map<Direction, VoxelShape> ROPE_L_TOP = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_M_TOP = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_R_TOP = new EnumMap<>(Direction.class);
    private static final Map<Direction, VoxelShape> ROPE_TOP_BAR = new EnumMap<>(Direction.class);

    public FishDryerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH).setValue(PART, FishDryerPart.LEFT).setValue(H_PART, FishDryerHeight.BOTTOM));
        cacheShapes();
    }

    private void cacheShapes() {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            LEFT_POST.put(dir, calculateRotation(dir, Block.box(0, 0, 6, 4, 16, 10)));
            RIGHT_POST.put(dir, calculateRotation(dir, Block.box(12, 0, 6, 16, 16, 10)));
            
            // СРЕДНИЙ ЯРУС: Веревки только сверху (от 12.8 до 16.0)
            ROPE_L_MID.put(dir, calculateRotation(dir, Block.box(8.5, 12.8, 7.5, 10.5, 16, 8.5)));
            ROPE_M_MID.put(dir, calculateRotation(dir, Shapes.or(
                    Block.box(2.5, 12.8, 7.5, 4.5, 16, 8.5),
                    Block.box(12.5, 12.8, 7.5, 14.5, 16, 8.5)
            )));
            ROPE_R_MID.put(dir, calculateRotation(dir, Block.box(5.5, 12.8, 7.5, 7.5, 16, 8.5)));

            // ВЕРХНИЙ ЯРУС: Веревки от низа до перекладины (0.0 до 14.0) + перекладина (14.0 до 15.5)
            ROPE_L_TOP.put(dir, calculateRotation(dir, Block.box(8.5, 0, 7.5, 10.5, 14, 8.5)));
            ROPE_M_TOP.put(dir, calculateRotation(dir, Shapes.or(
                    Block.box(2.5, 0, 7.5, 4.5, 14, 8.5),
                    Block.box(12.5, 0, 7.5, 14.5, 14, 8.5)
            )));
            ROPE_R_TOP.put(dir, calculateRotation(dir, Block.box(5.5, 0, 7.5, 7.5, 14, 8.5)));
            ROPE_TOP_BAR.put(dir, calculateRotation(dir, Block.box(0, 14, 7.5, 16, 15.5, 8.5)));
        }
    }

    private static VoxelShape calculateRotation(Direction direction, VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{shape, Shapes.empty()};
        int times = (direction.get2DDataValue() - Direction.NORTH.get2DDataValue() + 4) % 4;
        for (int i = 0; i < times; i++) {
            buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], 
                Shapes.box(1 - maxZ, minY, minX, 1 - minZ, maxY, maxX)));
            buffer[0] = buffer[1];
            buffer[1] = Shapes.empty();
        }
        return buffer[0];
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        Direction facing = state.getValue(FACING);
        FishDryerPart part = state.getValue(PART);
        FishDryerHeight height = state.getValue(H_PART);
        
        VoxelShape shape = Shapes.empty();
        
        // Столбы
        if (part == FishDryerPart.LEFT) shape = Shapes.or(shape, LEFT_POST.get(facing));
        if (part == FishDryerPart.RIGHT) shape = Shapes.or(shape, RIGHT_POST.get(facing));
        
        // Веревки для СРЕДНЕГО яруса
        if (height == FishDryerHeight.MIDDLE) {
            if (part == FishDryerPart.LEFT) shape = Shapes.or(shape, ROPE_L_MID.get(facing));
            if (part == FishDryerPart.MIDDLE) shape = Shapes.or(shape, ROPE_M_MID.get(facing));
            if (part == FishDryerPart.RIGHT) shape = Shapes.or(shape, ROPE_R_MID.get(facing));
        }
        
        // Веревки для ВЕРХНЕГО яруса
        if (height == FishDryerHeight.TOP) {
            if (part == FishDryerPart.LEFT) shape = Shapes.or(shape, ROPE_L_TOP.get(facing));
            if (part == FishDryerPart.MIDDLE) shape = Shapes.or(shape, ROPE_M_TOP.get(facing));
            if (part == FishDryerPart.RIGHT) shape = Shapes.or(shape, ROPE_R_TOP.get(facing));
            shape = Shapes.or(shape, ROPE_TOP_BAR.get(facing));
        }
        
        return shape;
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        if (state.getValue(PART) == FishDryerPart.LEFT) return LEFT_POST.get(state.getValue(FACING));
        if (state.getValue(PART) == FishDryerPart.RIGHT) return RIGHT_POST.get(state.getValue(FACING));
        return Shapes.empty();
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.is(newState.getBlock())) {
            BlockPos mainPos = getMainPos(pos, state);
            Direction facing = state.getValue(FACING);
            Direction side = facing.getClockWise();
            
            // Ломаем всю структуру 3х3
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    BlockPos target = mainPos.relative(side, x).above(y);
                    if (level.getBlockState(target).is(this)) {
                        level.setBlock(target, Blocks.AIR.defaultBlockState(), 3);
                    }
                }
            }
            super.onRemove(state, level, pos, newState, isMoving);
        }
    }

    private BlockPos getMainPos(BlockPos pos, BlockState state) {
        Direction facing = state.getValue(FACING);
        FishDryerPart part = state.getValue(PART);
        FishDryerHeight height = state.getValue(H_PART);
        Direction sideBack = facing.getCounterClockWise();
        int xOff = (part == FishDryerPart.LEFT) ? 0 : (part == FishDryerPart.MIDDLE ? 1 : 2);
        int yOff = (height == FishDryerHeight.BOTTOM) ? 0 : (height == FishDryerHeight.MIDDLE ? 1 : 2);
        return pos.relative(sideBack, xOff).below(yOff);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, PART, H_PART);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction facing = context.getHorizontalDirection();
        BlockPos pos = context.getClickedPos();
        Level level = context.getLevel();
        Direction side = facing.getClockWise();
        for (int x = 0; x < 3; x++) {
            for (int y = 0; y < 3; y++) {
                BlockPos checkPos = pos.relative(side, x).above(y);
                if (!level.getBlockState(checkPos).canBeReplaced(context) || !level.getWorldBorder().isWithinBounds(checkPos)) return null;
            }
        }
        return this.defaultBlockState().setValue(FACING, facing).setValue(PART, FishDryerPart.LEFT).setValue(H_PART, FishDryerHeight.BOTTOM);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!level.isClientSide) {
            Direction side = state.getValue(FACING).getClockWise();
            for (int x = 0; x < 3; x++) {
                for (int y = 0; y < 3; y++) {
                    if (x == 0 && y == 0) continue;
                    BlockPos targetPos = pos.relative(side, x).above(y);
                    FishDryerPart part = x == 0 ? FishDryerPart.LEFT : (x == 1 ? FishDryerPart.MIDDLE : FishDryerPart.RIGHT);
                    FishDryerHeight height = y == 0 ? FishDryerHeight.BOTTOM : (y == 1 ? FishDryerHeight.MIDDLE : FishDryerHeight.TOP);
                    level.setBlock(targetPos, state.setValue(PART, part).setValue(H_PART, height), 3);
                }
            }
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) { return RenderShape.MODEL; }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return (state.getValue(PART) == FishDryerPart.LEFT && state.getValue(H_PART) == FishDryerHeight.BOTTOM) ? new FishDryerBlockEntity(pos, state) : null;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return (state.getValue(PART) == FishDryerPart.LEFT && state.getValue(H_PART) == FishDryerHeight.BOTTOM) ? createTickerHelper(type, ModBlockEntities.FISH_DRYER_BE.get(), FishDryerBlockEntity::tick) : null;
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        BlockPos mainPos = getMainPos(pos, state);
        BlockEntity be = level.getBlockEntity(mainPos);
        if (be instanceof FishDryerBlockEntity fishDryer) {
            Direction facing = state.getValue(FACING);
            FishDryerPart part = state.getValue(PART);
            
            // Локальные координаты клика внутри блока (0.0 - 1.0)
            Vec3 hitLoc = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
            double lx = switch (facing) {
                case NORTH -> hitLoc.x();
                case SOUTH -> 1.0 - hitLoc.x();
                case EAST -> hitLoc.z();
                case WEST -> 1.0 - hitLoc.z();
                default -> 0;
            };

            // Переводим в глобальную координату структуры (0.0 - 3.0)
            double gx = lx + (part == FishDryerPart.LEFT ? 0 : (part == FishDryerPart.MIDDLE ? 1 : 2));

            int slot = -1;
            if (gx >= 0.25 && gx < 0.9) slot = 0;
            else if (gx >= 0.9 && gx < 1.5) slot = 1;
            else if (gx >= 1.5 && gx < 2.1) slot = 2;
            else if (gx >= 2.1 && gx <= 2.75) slot = 3;

            if (slot != -1) return fishDryer.useItemOnSlot(player, hand, stack, slot);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    public enum FishDryerPart implements StringRepresentable {
        LEFT("left"), MIDDLE("middle"), RIGHT("right");
        private final String name;
        FishDryerPart(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }

    public enum FishDryerHeight implements StringRepresentable {
        BOTTOM("bottom"), MIDDLE("middle"), TOP("top");
        private final String name;
        FishDryerHeight(String name) { this.name = name; }
        @Override public String getSerializedName() { return this.name; }
    }
}
