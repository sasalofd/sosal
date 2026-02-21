package salo2b.beer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BeerMugBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Твои настройки хитбоксов для подгона
    private static final VoxelShape SHAPE_NORTH = Block.box(6.2D, 0.0D, 5.2D, 10.8D, 5.5D, 10.8D);
    private static final VoxelShape SHAPE_SOUTH = Block.box(5.2D, 0.0D, 5.0D, 9.9D, 5.5D, 10.8D);
    private static final VoxelShape SHAPE_EAST = Block.box(5.2D, 0.0D, 6.2D, 11.0D, 5.5D, 10.8D);
    private static final VoxelShape SHAPE_WEST = Block.box(5.0D, 0.0D, 5.2D, 10.8D, 5.5D, 9.9D);

    public BeerMugBlock(BlockBehaviour.Properties properties) {
        // Заменили SoundType.OAK_PLANKS на SoundType.WOOD (звук забора/дерева)
        super(properties.noOcclusion().strength(0.3F).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case SOUTH -> SHAPE_SOUTH;
            case EAST -> SHAPE_EAST;
            case WEST -> SHAPE_WEST;
            default -> SHAPE_NORTH;
        };
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            ItemStack itemStack = new ItemStack(this.asItem());
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }
            level.removeBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }
}
