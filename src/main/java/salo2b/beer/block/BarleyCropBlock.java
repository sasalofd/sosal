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
