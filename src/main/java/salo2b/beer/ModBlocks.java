package salo2b.beer;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

// Если класс ModItems лежит в другом пакете, раскомментируйте строку ниже:
// import salo2b.beer.item.ModItems;

public class ModBlocks {

    public static final DeferredRegister<Block> BLOCKS =
            DeferredRegister.create(ForgeRegistries.BLOCKS, BeerMod.MODID);

    // Блок хмеля
    public static final RegistryObject<Block> HOPS_CROP = BLOCKS.register("hops_crop",
            () -> new HopsCropBlock(BlockBehaviour.Properties.copy(Blocks.WHEAT).noOcclusion().noCollission()));

    // Блок пивоварни
    public static final RegistryObject<Block> BREWERY = BLOCKS.register("brewery",
            () -> new BreweryBlock(BlockBehaviour.Properties.copy(Blocks.IRON_BLOCK).noOcclusion().strength(4.0f).requiresCorrectToolForDrops()));

    // Блок пустой кружки (с авто-регистрацией обычного предмета)
    public static final RegistryObject<Block> WOODEN_MUG = registerBlock("wooden_mug",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));

    // Блок ПИВА (Полная кружка)
    // Мы используем BLOCKS.register (а не registerBlock), потому что предмет для него мы создаем ОТДЕЛЬНО в ModItems.
    // Мы используем класс WoodenMugBlock, чтобы полная кружка имела ту же форму (хитбокс), что и пустая.
    public static final RegistryObject<Block> BEER = BLOCKS.register("beer",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.copy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));


    // --- Вспомогательные методы ---

    // Регистрирует и блок, и предмет для него
    private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block) {
        RegistryObject<T> toReturn = BLOCKS.register(name, block);
        registerBlockItem(name, toReturn);
        return toReturn;
    }

    // Регистрирует простой BlockItem
    private static <T extends Block> RegistryObject<Item> registerBlockItem(String name, RegistryObject<T> block) {
        return ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    public static void register(IEventBus eventBus) {
        BLOCKS.register(eventBus);
    }
}
