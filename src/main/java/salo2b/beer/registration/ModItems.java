package salo2b.beer.registration;

import salo2b.beer.BeerMod;
import salo2b.beer.item.BeerItem;
import salo2b.beer.item.BeerMugItem;
import salo2b.beer.item.HopsSeedsItem;
import salo2b.beer.item.WetBarleyItem;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

import java.util.List;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(BeerMod.MODID);

    // --- ПИВО И НАПИТКИ ---

    public static final DeferredItem<Item> BEER = ITEMS.register("beer",
            () -> new BeerMugItem(ModBlocks.BEER.get(), new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4).saturationModifier(0.5f).alwaysEdible()
                            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0f)           // Тошнота 10с
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 600, 0), 1.0f) // Сопротивление 30с
                            .effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 200, 0), 1.0f)      // Регенерация 10с
                            .build())
            ));

    public static final DeferredItem<Item> LIGHT_BEER = ITEMS.register("light_beer",
            () -> new BeerMugItem(ModBlocks.LIGHT_BEER.get(), new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(3).saturationModifier(0.6f).alwaysEdible()
                            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 400, 0), 1.0f)              // Удача 20с
                            .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0), 1.0f)    // Скорость 10с
                            .effect(() -> new MobEffectInstance(MobEffects.GLOWING, 200, 0), 1.0f)           // Свечение 10с
                            .build())
            ));

    public static final DeferredItem<Item> FILTERED_BEER = ITEMS.register("filtered_beer",
            () -> new BeerMugItem(ModBlocks.FILTERED_BEER.get(), new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(5).saturationModifier(0.4f).alwaysEdible()
                            .effect(() -> new MobEffectInstance(MobEffects.DIG_SPEED, 600, 0), 1.0f)         // Спешка 30с
                            .effect(() -> new MobEffectInstance(MobEffects.NIGHT_VISION, 600, 0), 1.0f)      // Ночное зрение 30с
                            .build())
            ));

    // Остальные предметы без изменений
    public static final DeferredItem<Item> HOPS_SEEDS = ITEMS.register("hops_seeds",
            () -> new HopsSeedsItem(new Item.Properties()));

    public static final DeferredItem<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));

    // --- ВЕДРА И ЖИДКОСТИ ---
    public static final DeferredHolder<Item, Item> WORT_BUCKET = ITEMS.register("wort_bucket",
            () -> new Item(new Item.Properties().stacksTo(16)));

    // --- НАПИТКИ ---
    public static final DeferredHolder<Item, Item> CIDER = ITEMS.register("cider",
            () -> new Item(new Item.Properties().stacksTo(16)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(4).saturationModifier(0.3f).alwaysEdible().build())));

    public static final DeferredHolder<Item, Item> BARLEY_BEER = ITEMS.register("barley_beer",
            () -> new Item(new Item.Properties().stacksTo(16)
                    .food(new net.minecraft.world.food.FoodProperties.Builder()
                            .nutrition(5).saturationModifier(0.4f).alwaysEdible().build())));

    public static final DeferredItem<Item> BARLEY = ITEMS.register("barley",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> MALT = ITEMS.register("malt",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> BREWERY_ITEM = ITEMS.register("brewery",
            () -> new BlockItem(ModBlocks.BREWERY.get(), new Item.Properties()));

    public static final DeferredItem<Item> BARLEY_SEEDS = ITEMS.register("barley_seeds",
            () -> new ItemNameBlockItem(ModBlocks.BARLEY_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> GREEN_APPLE = ITEMS.register("green_apple",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).build())));

    public static final DeferredItem<Item> WET_BARLEY_SEEDS = ITEMS.register("wet_barley_seeds",
            () -> new WetBarleyItem(new Item.Properties().stacksTo(64)));

    public static final DeferredItem<Item> CRUSHED_MALT = ITEMS.register("crushed_malt",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SALT_CRYSTAL = ITEMS.register("salt_crystal_item",
            () -> new Item(new Item.Properties()));

    public static final DeferredItem<Item> SALTED_FISH = ITEMS.register("salted_fish",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.3f).build())));

    public static final DeferredItem<Item> DRIED_FISH = ITEMS.register("dried_fish",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder().nutrition(5).saturationModifier(0.8f).build())));

    public static final DeferredItem<Item> FISH_DRYER = ITEMS.register("fish_dryer",
            () -> new BlockItem(ModBlocks.FISH_DRYER.get(), new Item.Properties()));
}
