package salo2b.beer.registration;

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

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BeerMod.MODID);

    // Решетка для хмеля
    public static final DeferredBlock<Block> LATTICE = registerBlock("lattice",
            () -> new LatticeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).noOcclusion()));

    // Блок хмеля (лиана)
    public static final DeferredBlock<Block> HOPS_VINE = BLOCKS.register("hops_vine",
            () -> new HopsVineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).sound(SoundType.GRASS).noOcclusion()));

    // Дикий хмель
    public static final DeferredBlock<Block> WILD_HOPS = registerBlock("wild_hops",
            () -> new WildHopsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)));

    // Пивоварня
    public static final DeferredBlock<Block> BREWERY = BLOCKS.register("brewery",
            () -> new BreweryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().strength(4.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BEER_BARREL = registerBlock("beer_barrel",
            () -> new BeerBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));
    // Бревно яблони (используем дуб как основу)
    public static final DeferredBlock<Block> APPLE_LOG = registerBlock("apple_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));

    // Обычная листва
    public static final DeferredBlock<Block> APPLE_LEAVES = registerBlock("apple_leaves",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .noOcclusion() // Чтобы не было черных дыр
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));

    // Плодовая листва
    public static final DeferredBlock<Block> APPLE_FRUIT_LEAVES = registerBlock("apple_fruit_leaves",
            () -> new AppleFruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .randomTicks()
                    .noOcclusion()
                    // Этот параметр отключает затухание листвы, если мы не используем LeavesBlock класс напрямую
                    .isViewBlocking((state, level, pos) -> false)));

    // Саженец яблони
    public static final DeferredBlock<Block> APPLE_SAPLING = registerBlock("apple_sapling",
            () -> new SaplingBlock(
                    new TreeGrower("apple",
                            Optional.empty(), // Мега-дерево (нет)
                            Optional.of(ModConfiguredFeatures.APPLE_TREE), // Ссылка на генерацию
                            Optional.empty()),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

    // Пустая кружка
    public static final DeferredBlock<Block> WOODEN_MUG = registerBlock("wooden_mug",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));

    public static final DeferredBlock<Block> BEER = BLOCKS.register("beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> MILLSTONE = registerBlock("millstone",
            () -> net.neoforged.fml.ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createMillstone() :
                    new MillstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.STONE)));

    public static final DeferredBlock<Block> LIGHT_BEER = BLOCKS.register("light_beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> FILTERED_BEER = BLOCKS.register("filtered_beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> GEARBOX = registerBlock("gearbox",
            () -> net.neoforged.fml.ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createGearbox() :
                    new GearboxBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.WOOD).noOcclusion()));


    public static final DeferredBlock<Block> WINDMILL_ROTOR = registerBlock("windmill_rotor",
            () -> net.neoforged.fml.ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createRotor() :
                    new WindmillRotorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(3.0f)));



    public static final DeferredBlock<Block> MALT_VAT = registerBlock("malt_vat",
            () -> new MaltVatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));
    public static final DeferredBlock<Block> BARLEY_CROP = BLOCKS.register("barley_crop",
            () -> new BarleyCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .noCollission()
                    .instabreak()));
    // Добавь это в список своих блоков
    public static final DeferredBlock<Block> WINDMILL_SHAFT = registerBlock("windmill_shaft",
            () -> net.neoforged.fml.ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createShaft() :
                    new WindmillShaftBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.WOOD).strength(2.0f)));
    // Вспомогательный метод для регистрации блока вместе с предметом
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
