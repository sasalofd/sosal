package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {
    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(BeerMod.MODID);

    // Семена
    public static final DeferredItem<Item> HOPS_SEEDS = ITEMS.register("hops_seeds",
            () -> new ItemNameBlockItem(ModBlocks.HOPS_CROP.get(), new Item.Properties()));

    // Хмель
    public static final DeferredItem<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));
    // Отфильтрованное пиво
    public static final DeferredItem<Item> FILTERED_BEER = ITEMS.register("filtered_beer",
            () -> new Item(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(4).saturationModifier(0.3f).alwaysEdible().build())));

    // Светлое пиво
    public static final DeferredItem<Item> LIGHT_BEER = ITEMS.register("light_beer",
            () -> new Item(new Item.Properties().stacksTo(16).food(new FoodProperties.Builder().nutrition(2).saturationModifier(0.5f).alwaysEdible().build())));
    // Ячмень
    public static final DeferredItem<Item> BARLEY = ITEMS.register("barley",
            () -> new Item(new Item.Properties()));

    // Солод
    public static final DeferredItem<Item> MALT = ITEMS.register("malt",
            () -> new Item(new Item.Properties()));

    // Пиво (с эффектами)
    public static final DeferredItem<Item> BEER = ITEMS.register("beer",
            () -> new BeerMugItem(
                    ModBlocks.BEER.get(),
                    new Item.Properties()
                            .stacksTo(16)
                            .food(new FoodProperties.Builder()
                                    .nutrition(4)
                                    .saturationModifier(0.3f)
                                    .alwaysEdible()
                                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0f)
                                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0), 1.0f)
                                    .build())
            ));

    // Пивоварня как предмет
    public static final DeferredItem<Item> BREWERY_ITEM = ITEMS.register("brewery",
            () -> new BlockItem(ModBlocks.BREWERY.get(), new Item.Properties()));
}
