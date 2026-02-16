package salo2b.beer;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;

public class WildHopsBlock extends BushBlock {

    public static final MapCodec<WildHopsBlock> CODEC = simpleCodec(WildHopsBlock::new);

    public WildHopsBlock(Properties properties) {
        super(properties);
    }

    public WildHopsBlock() {
        this(BlockBehaviour.Properties.of()
                .mapColor(MapColor.PLANT)
                .noCollission()
                .instabreak()
                .sound(SoundType.GRASS)
                .offsetType(BlockBehaviour.OffsetType.XZ));
    }

    @Override
    public MapCodec<? extends BushBlock> codec() {
        return CODEC;
    }
}
