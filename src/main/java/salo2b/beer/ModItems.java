package salo2b.beer;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemNameBlockItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, BeerMod.MODID);

    // Семена
    public static final RegistryObject<Item> HOPS_SEEDS = ITEMS.register("hops_seeds",
            () -> new ItemNameBlockItem(ModBlocks.HOPS_CROP.get(), new Item.Properties()));

    // Хмель
    public static final RegistryObject<Item> HOPS = ITEMS.register("hops",
            () -> new Item(new Item.Properties()));

    // Пиво (с эффектами)
    public static final RegistryObject<Item> BEER = ITEMS.register("beer",
            () -> new BeerMugItem(
                    ModBlocks.BEER.get(),
                    new Item.Properties()
                            .stacksTo(1)
                            .food(new FoodProperties.Builder()
                                    .nutrition(4)
                                    .saturationMod(0.3f)
                                    .alwaysEat()
                                    .effect(() -> new MobEffectInstance(MobEffects.CONFUSION, 300, 0), 1.0f)
                                    .effect(() -> new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 300, 0), 1.0f)
                                    .build())
            ));

    // Пивоварня как предмет
    public static final RegistryObject<Item> BREWERY_ITEM = ITEMS.register("brewery",
            () -> new BlockItem(ModBlocks.BREWERY.get(), new Item.Properties()));
}
