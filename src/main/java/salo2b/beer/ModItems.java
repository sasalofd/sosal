package salo2b.beer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;


public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(BeerMod.MODID);

    // 1. ПИВО (Нефильтрованное/Обычное) - beer
    // Эффекты: Тошнота, Отравление, Голод (10 сек) + Тёмный экран (Слепота)
    public static final DeferredItem<Item> BEER = ITEMS.register("beer",
            () -> new BeerMugItem(ModBlocks.BEER.get(), new Item.Properties()
                    .stacksTo(16) // Для пива лучше 1, чтобы работала логика пустой кружки
                    .food(new FoodProperties.Builder()
                            .nutrition(2).saturationModifier(0.2f).alwaysEdible()
                            .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 200, 0), 1.0f) // Тошнота 10с
                            .effect(() -> new MobEffectInstance(MobEffects.POISON, 200, 0), 1.0f)    // Отравление 10с
                            .effect(() -> new MobEffectInstance(MobEffects.HUNGER, 200, 0), 1.0f)    // Голод 10с
                            .effect(() -> new MobEffectInstance(MobEffects.BLINDNESS, 200, 0), 1.0f) // Темный экран 10с
                            .build())
            ));

    // 2. ОТФИЛЬТРОВАННОЕ ПИВО - filtered_beer
    // Эффекты: Сопротивление (15с), Удача (10с), Побочный: Слабость (5с)
    public static final DeferredItem<Item> FILTERED_BEER = ITEMS.register("filtered_beer",
            () -> new BeerMugItem(ModBlocks.FILTERED_BEER.get(), new Item.Properties()
                    .stacksTo(16)
                    .food(new FoodProperties.Builder()
                            .nutrition(4).saturationModifier(0.4f).alwaysEdible()
                            .effect(() -> new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 300, 0), 1.0f) // Сопротивление 15с
                            .effect(() -> new MobEffectInstance(MobEffects.LUCK, 200, 0), 1.0f)              // Удача 10с
                            .effect(() -> new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), 1.0f)          // Слабость 5с
                            .build())
            ));

    // 3. СВЕТЛОЕ ПИВО - light_beer
    // Эффекты: Удача (20с), Скорость (10с), Свечение (10с - легкий эффект)
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

    // Остальные предметы без изменений
    public static final DeferredItem<Item> HOPS_SEEDS = ITEMS.register("hops_seeds",
            () -> new ItemNameBlockItem(ModBlocks.HOPS_CROP.get(), new Item.Properties()));

    public static final DeferredItem<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));

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
    // Добавь это в твой ModItems.java
    public static final DeferredItem<Item> WET_BARLEY_SEEDS = ITEMS.register("wet_barley_seeds",
            () -> new WetBarleyItem(new Item.Properties().stacksTo(64)));
    public static final DeferredItem<Item> CRUSHED_MALT = ITEMS.register("crushed_malt",
            () -> new Item(new Item.Properties()));




}
