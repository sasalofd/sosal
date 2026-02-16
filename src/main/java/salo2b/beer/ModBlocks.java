package salo2b.beer;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BeerMod.MODID);

    // Блок хмеля
    public static final DeferredBlock<Block> HOPS_CROP = BLOCKS.register("hops_crop",
            () -> new HopsCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT).noOcclusion().noCollission()));

    // Дикий хмель
    public static final DeferredBlock<Block> WILD_HOPS = registerBlock("wild_hops",
            () -> new WildHopsBlock());

    // Пивоварня
    public static final DeferredBlock<Block> BREWERY = BLOCKS.register("brewery",
            () -> new BreweryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().strength(4.0f).requiresCorrectToolForDrops()));

    // Пустая кружка
    public static final DeferredBlock<Block> WOODEN_MUG = registerBlock("wooden_mug",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));

    // Полная кружка (блок)
    public static final DeferredBlock<Block> BEER = BLOCKS.register("beer",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));


    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block) {
        DeferredBlock<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block) {
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
