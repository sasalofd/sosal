package salo2b.beer.registration;

import salo2b.beer.*;
import salo2b.beer.block.*;
import salo2b.beer.block.salt.*;
import salo2b.beer.block.entity.*;
import salo2b.beer.item.*;
import salo2b.beer.worldgen.ModConfiguredFeatures;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.TreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Optional;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(BeerMod.MODID);

    public static final DeferredBlock<Block> LATTICE = registerBlock("lattice",
            () -> new LatticeBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).noOcclusion()));

    public static final DeferredBlock<Block> HOPS_VINE = BLOCKS.register("hops_vine",
            () -> new HopsVineBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).sound(SoundType.GRASS).noOcclusion()));

    public static final DeferredBlock<Block> WILD_HOPS = registerBlock("wild_hops",
            () -> new WildHopsBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.TALL_GRASS)
                    .noCollission()
                    .instabreak()
                    .sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ)));

    public static final DeferredBlock<Block> BREWERY = BLOCKS.register("brewery",
            () -> new BreweryBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).noOcclusion().strength(4.0f).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BEER_BARREL = registerBlock("beer_barrel",
            () -> new BeerBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL)));

    public static final DeferredBlock<Block> APPLE_LOG = registerBlock("apple_log",
            () -> new RotatedPillarBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LOG)));

    public static final DeferredBlock<Block> APPLE_LEAVES = registerBlock("apple_leaves",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .noOcclusion()
                    .isSuffocating((s, l, p) -> false)
                    .isViewBlocking((s, l, p) -> false)));

    public static final DeferredBlock<Block> APPLE_FRUIT_LEAVES = registerBlock("apple_fruit_leaves",
            () -> new AppleFruitLeavesBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_LEAVES)
                    .randomTicks()
                    .noOcclusion()
                    .isViewBlocking((state, level, pos) -> false)));

    public static final DeferredBlock<Block> APPLE_SAPLING = registerBlock("apple_sapling",
            () -> new SaplingBlock(
                    new TreeGrower("apple",
                            Optional.empty(),
                            Optional.of(ModConfiguredFeatures.APPLE_TREE),
                            Optional.empty()),
                    BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_SAPLING)));

    public static final DeferredBlock<Block> WOODEN_MUG = registerBlock("wooden_mug",
            () -> new WoodenMugBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).strength(0.5f).noOcclusion()));

    public static final DeferredBlock<Block> BEER = BLOCKS.register("beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> MILLSTONE = registerBlock("millstone",
            () -> ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createMillstone() :
                    new MillstoneBlock(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).requiresCorrectToolForDrops().strength(3.5f).sound(SoundType.STONE)));

    public static final DeferredBlock<Block> LIGHT_BEER = BLOCKS.register("light_beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> FILTERED_BEER = BLOCKS.register("filtered_beer",
            () -> new BeerMugBlock(BlockBehaviour.Properties.of()));

    public static final DeferredBlock<Block> GEARBOX = registerBlock("gearbox",
            () -> ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createGearbox() :
                    new GearboxBlock(BlockBehaviour.Properties.of().strength(3.0f).sound(SoundType.WOOD).noOcclusion()));

    public static final DeferredBlock<Block> WINDMILL_ROTOR = registerBlock("windmill_rotor",
            () -> ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createRotor() :
                    new WindmillRotorBlock(BlockBehaviour.Properties.of().noOcclusion().strength(3.0f)));

    public static final DeferredBlock<Block> MALT_VAT = registerBlock("malt_vat",
            () -> new MaltVatBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.CAULDRON)));

    public static final DeferredBlock<Block> BARLEY_CROP = BLOCKS.register("barley_crop",
            () -> new BarleyCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<Block> HOPS_CROP = BLOCKS.register("hops_crop",
            () -> new HopsCropBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.WHEAT)
                    .noCollission()
                    .instabreak()));

    public static final DeferredBlock<Block> WINDMILL_SHAFT = registerBlock("windmill_shaft",
            () -> ModList.get().isLoaded("create") ?
                    salo2b.beer.compat.create.CreateCompat.createShaft() :
                    new WindmillShaftBlock(BlockBehaviour.Properties.of().noOcclusion().sound(SoundType.WOOD).strength(2.0f)));

    public static final DeferredBlock<Block> SALT_BLOCK = registerBlock("salt_block",
            () -> new Block(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.SNOW).strength(1.5f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops()));

    public static final DeferredBlock<Block> BLOOMING_SALT_BLOCK = registerBlock("blooming_salt_block",
            () -> new BloomingSaltBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).mapColor(MapColor.SNOW).randomTicks().strength(1.5f, 3.0f).sound(SoundType.STONE).requiresCorrectToolForDrops().lightLevel(state -> 10)));

    public static final DeferredBlock<Block> SALT_CRYSTAL = BLOCKS.register("salt_crystal",
            () -> new SaltCrystalBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.AMETHYST_CLUSTER).mapColor(MapColor.SNOW).randomTicks().strength(0.5f).sound(SoundType.GLASS).requiresCorrectToolForDrops().noOcclusion()));

    public static final DeferredBlock<Block> FISH_DRYER = BLOCKS.register("fish_dryer",
            () -> new FishDryerBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_FENCE).noOcclusion().strength(2.0f).sound(SoundType.WOOD)));

    public static final DeferredBlock<Block> SALTING_BARREL = registerBlock("salting_barrel",
            () -> new SaltingBarrelBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.BARREL).noOcclusion()));

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
