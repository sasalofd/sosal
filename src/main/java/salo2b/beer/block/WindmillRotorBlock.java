package salo2b.beer.block;

import salo2b.beer.block.entity.WindmillRotorBlockEntity;
import salo2b.beer.registration.ModBlockEntities;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

public class WindmillRotorBlock extends BaseEntityBlock {
    public static final MapCodec<WindmillRotorBlock> CODEC = simpleCodec(WindmillRotorBlock::new);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public WindmillRotorBlock(Properties properties) {
        super(properties.sound(SoundType.WOOD).noOcclusion());
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override protected MapCodec<? extends BaseEntityBlock> codec() { return CODEC; }
    @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
    
    @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace();
        if (direction.getAxis().isVertical()) {
            direction = context.getHorizontalDirection().getOpposite();
        }
        return this.defaultBlockState().setValue(FACING, direction);
    }

    @Nullable @Override public BlockEntity newBlockEntity(BlockPos pos, BlockState state) { return new WindmillRotorBlockEntity(pos, state); }
    @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {        
        return createTickerHelper(type, (BlockEntityType<WindmillRotorBlockEntity>)(Object)ModBlockEntities.WINDMILL_ROTOR.get(), WindmillRotorBlockEntity::tick);
    }

    @Override protected BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
    @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
}
