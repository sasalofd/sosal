package salo2b.beer.block;

import salo2b.beer.*;
import salo2b.beer.block.*;
import salo2b.beer.block.entity.*;
import salo2b.beer.item.*;
import salo2b.beer.menu.*;
import salo2b.beer.registration.*;
import salo2b.beer.villager.*;
import salo2b.beer.worldgen.*;
import salo2b.beer.client.renderer.*;
import salo2b.beer.client.screen.*;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class WildHopsBlock extends BushBlock {
    // Кодек для 1.21.1 - оставляем, это правильно
    public static final MapCodec<WildHopsBlock> CODEC = simpleCodec(WildHopsBlock::new);

    // Форма блока (чтобы рамка была как у кустика, а не как у камня)
    protected static final VoxelShape SHAPE = Block.box(2.0D, 0.0D, 2.0D, 14.0D, 13.0D, 14.0D);

    public WildHopsBlock(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state) {
        // Здесь мы указываем, какой предмет давать при выборе блока
        return new ItemStack(ModItems.HOPS.get());
    }
    @Override
    protected MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
