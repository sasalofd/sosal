package salo2b.beer;

import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

public class BarleyCropBlock extends CropBlock {
    public BarleyCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
    }

    @Override
    protected ItemLike getBaseSeedId() {
        // Указываем, какие семена возвращаются при поломке (незрелого) или через грядку
        return ModItems.BARLEY_SEEDS.get();
    }
}
