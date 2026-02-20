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
    // Свойство поворота для blockstates
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    // Идеальный хитбокс: "худой" и "низкий" (в облипку кружке)
    // 6.5 и 9.5 — делают ширину 3 пикселя
    // 6.0 — высота 6 пикселей (под пену)
    protected static final VoxelShape SHAPE = Block.box(6.5D, 0.0D, 6.5D, 9.5D, 6.0D, 9.5D);

    public BeerMugBlock(BlockBehaviour.Properties properties) {
        // Устанавливаем настройки (стекло, прозрачность, сила)
        super(properties.noOcclusion().strength(0.3F).sound(SoundType.GLASS));
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }

    // Применяем маленькую рамку (хитбокс)
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // Определяем направление при установке (лицом к игроку)
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // Регистрируем состояние FACING в блоке
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    // Сбор кружки кликом (ПКМ без шифта)
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide) {
            // Создаем предмет из этого же блока
            ItemStack itemStack = new ItemStack(this.asItem());

            // Пытаемся добавить в инвентарь (автоматически ищет стак, если stacksTo > 1)
            if (!player.getInventory().add(itemStack)) {
                player.drop(itemStack, false);
            }

            // Убираем кружку с земли
            level.removeBlock(pos, false);
        }
        return InteractionResult.SUCCESS;
    }
}
