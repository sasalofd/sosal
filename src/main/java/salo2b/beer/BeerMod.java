package salo2b.beer;

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.EventBusSubscriber.Bus; // Поправленный импорт для Bus.MOD
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import java.util.function.Supplier;

@Mod(BeerMod.MODID)
public class BeerMod {
    public static final String MODID = "beer";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public BeerMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus); // Регистрация типов меню

        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerBlockColors);
            modEventBus.addListener(this::onClientSetup);
        }
    }

    @EventBusSubscriber(modid = MODID, bus = Bus.MOD, value = Dist.CLIENT)
    public static class ClientEvents {
        @SubscribeEvent
        public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
            event.registerBlockEntityRenderer(ModBlockEntities.WINDMILL_SHAFT.get(), WindmillShaftRenderer::new);
            event.registerBlockEntityRenderer(ModBlockEntities.WINDMILL_ROTOR.get(), WindmillRotorRenderer::new);
        }

        // ВАЖНО: Регистрация экрана жерновов
        @SubscribeEvent
        public static void registerScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.MILLSTONE_MENU.get(), MillstoneScreen::new);
        }
    }

    public static final Supplier<CreativeModeTab> BEER_TAB = CREATIVE_MODE_TABS.register("beer_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BEER.get()))
                    .title(Component.translatable("itemGroup.beer_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HOPS_SEEDS.get());
                        output.accept(ModItems.HOPS.get());
                        output.accept(ModItems.BARLEY_SEEDS.get());
                        output.accept(ModItems.BARLEY.get());
                        output.accept(ModItems.MALT.get());
                        output.accept(ModItems.GREEN_APPLE.get());
                        output.accept(ModBlocks.APPLE_SAPLING.get());
                        output.accept(ModItems.WET_BARLEY_SEEDS.get());
                        output.accept(ModBlocks.APPLE_LOG.get());
                        output.accept(ModBlocks.APPLE_LEAVES.get());
                        output.accept(ModBlocks.APPLE_FRUIT_LEAVES.get());
                        output.accept(ModBlocks.WOODEN_MUG.get());
                        output.accept(ModBlocks.BEER_BARREL.get());
                        output.accept(ModBlocks.MALT_VAT.get());
                        output.accept(ModItems.BEER.get());
                        output.accept(ModItems.FILTERED_BEER.get());
                        output.accept(ModItems.LIGHT_BEER.get());
                        output.accept(ModItems.BREWERY_ITEM.get());
                        output.accept(ModBlocks.WINDMILL_ROTOR.get());
                        output.accept(ModBlocks.WINDMILL_SHAFT.get());
                        output.accept(ModBlocks.MILLSTONE.get());
                        output.accept(ModItems.CRUSHED_MALT.get());
                        // Добавляем Gearbox в креатив
                        output.accept(ModBlocks.GEARBOX.get());
                    })
                    .build());

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_FRUIT_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_SAPLING.get(), RenderType.cutout());
        });
    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> 0x3F76E4, ModBlocks.BREWERY.get());
    }
}