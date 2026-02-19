package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@Mod(BeerMod.MODID)
public class BeerMod {
    public static final String MODID = "beer";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final Supplier<CreativeModeTab> BEER_TAB = CREATIVE_MODE_TABS.register("beer_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BEER.get()))
                    .title(Component.translatable("itemGroup.beer_tab"))
                    .displayItems((parameters, output) -> {
                        // Растения и ингредиенты
                        output.accept(ModItems.HOPS_SEEDS.get());
                        output.accept(ModItems.HOPS.get());

                        // Внутри displayItems
                        output.accept(ModItems.BARLEY.get());
                        output.accept(ModItems.MALT.get());
                        output.accept(ModBlocks.MALT_VAT.get());


                        // Посуда и блоки
                        output.accept(ModBlocks.WOODEN_MUG.get());
                        output.accept(ModBlocks.BEER_BARREL.get()); // НАША БОЧКА

                        // Виды пива
                        output.accept(ModItems.BEER.get());         // Обычное
                        output.accept(ModItems.FILTERED_BEER.get()); // Фильтрованное
                        output.accept(ModItems.LIGHT_BEER.get());    // Светлое

                        // Оборудование
                        output.accept(ModItems.BREWERY_ITEM.get());
                    })
                    .build());

    public BeerMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerBlockColors);
        }
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> 0x3F76E4, ModBlocks.BREWERY.get());
    }
}
