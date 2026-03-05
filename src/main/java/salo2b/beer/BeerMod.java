package salo2b.beer;

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

import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.loading.FMLEnvironment;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import salo2b.beer.registration.*;
import salo2b.beer.client.renderer.*;
import salo2b.beer.client.screen.*;
import salo2b.beer.villager.*;

import java.util.function.Supplier;

import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.capabilities.Capabilities;

@Mod(BeerMod.MODID)
public class BeerMod {
    public static final String MODID = "beer";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public BeerMod(IEventBus modEventBus) {
        ModBlocks.BLOCKS.register(modEventBus);
        ModFeatures.register(modEventBus);
        ModDataComponents.DATA_COMPONENT_TYPES.register(modEventBus);
        ModEffects.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        ModMenuTypes.MENUS.register(modEventBus);
        ModVillagers.POI_TYPES.register(modEventBus);
        ModVillagers.PROFESSIONS.register(modEventBus);

        // Регистрация событий через Listener (убирает Warning)
        if (FMLEnvironment.dist == Dist.CLIENT) {
            modEventBus.addListener(this::registerBlockColors);
            modEventBus.addListener(this::onClientSetup);
            modEventBus.addListener(this::registerRenderers);
            modEventBus.addListener(this::registerScreens);
        }

        modEventBus.addListener(this::registerCapabilities);
        
        // Регистрация в основной шине для NPC и эффектов
        NeoForge.EVENT_BUS.register(ModVillagers.class);
        NeoForge.EVENT_BUS.register(salo2b.beer.effect.DrunkennessEvents.class);
    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        // --- MALT VAT ---
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MALT_VAT_BE.get(),
                (be, side) -> new net.neoforged.neoforge.items.wrapper.InvWrapper(be.inventory)
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.MALT_VAT_BE.get(),
                (be, side) -> be.fluidHandler
        );

        // --- BREWERY ---
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.BREWERY_BE.get(),
                (be, side) -> be.inventory
        );
        event.registerBlockEntity(
                Capabilities.ItemHandler.BLOCK,
                ModBlockEntities.MILLSTONE.get(),
                (be, side) -> {
                    if (be instanceof salo2b.beer.block.entity.IMillstoneBE millstone) return millstone.getInventory();
                    return null;
                }
        );
        event.registerBlockEntity(
                Capabilities.FluidHandler.BLOCK,
                ModBlockEntities.BREWERY_BE.get(),
                (be, side) -> be.tank
        );
    }

    // Методы регистрации событий без @EventBusSubscriber
    private void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        if (net.neoforged.fml.ModList.get().isLoaded("create")) {
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<salo2b.beer.compat.create.CreateCompat.CompatWindmillShaftBlockEntity>)(Object) ModBlockEntities.WINDMILL_SHAFT.get(), salo2b.beer.compat.create.CreateCompat.CompatWindmillShaftRenderer::new);
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<salo2b.beer.compat.create.CreateCompat.CompatWindmillRotorBlockEntity>)(Object) ModBlockEntities.WINDMILL_ROTOR.get(), salo2b.beer.compat.create.CreateCompat.CompatWindmillRotorRenderer::new);
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<salo2b.beer.compat.create.CreateCompat.CompatMillstoneBlockEntity>)(Object) ModBlockEntities.MILLSTONE.get(), salo2b.beer.compat.create.CreateCompat.CompatMillstoneRenderer::new);
        } else {
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<WindmillShaftBlockEntity>)(Object) ModBlockEntities.WINDMILL_SHAFT.get(), WindmillShaftRenderer::new);
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<WindmillRotorBlockEntity>)(Object) ModBlockEntities.WINDMILL_ROTOR.get(), WindmillRotorRenderer::new);
            event.registerBlockEntityRenderer((net.minecraft.world.level.block.entity.BlockEntityType<MillstoneBlockEntity>)(Object) ModBlockEntities.MILLSTONE.get(), MillstoneRenderer::new);
        }
        event.registerBlockEntityRenderer(ModBlockEntities.BEER_BARREL_BE.get(), BeerBarrelRenderer::new);
    }

    private void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenuTypes.MILLSTONE_MENU.get(), MillstoneScreen::new);
        event.register(ModMenuTypes.MALT_VAT_MENU.get(), MaltVatScreen::new);
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
                        output.accept(ModBlocks.LATTICE.get());
                        output.accept(ModBlocks.WOODEN_MUG.get());
                        output.accept(ModBlocks.BEER_BARREL.get());
                        output.accept(ModBlocks.MALT_VAT.get());
                        output.accept(ModBlocks.BREWERY.get());
                        output.accept(ModItems.WORT_BUCKET.get());
                        output.accept(ModItems.BEER.get());
                        output.accept(ModItems.CIDER.get());
                        output.accept(ModItems.BARLEY_BEER.get());
                        output.accept(ModItems.FILTERED_BEER.get());
                        output.accept(ModItems.LIGHT_BEER.get());
                        output.accept(ModBlocks.WINDMILL_ROTOR.get());
                        output.accept(ModBlocks.WINDMILL_SHAFT.get());
                        output.accept(ModBlocks.GEARBOX.get());
                        output.accept(ModBlocks.MILLSTONE.get());
                        output.accept(ModItems.CRUSHED_MALT.get());
                        output.accept(ModItems.SALT.get());
                        output.accept(ModItems.SALT_CRYSTAL.get());
                        output.accept(ModBlocks.SALT_BLOCK.get());
                        output.accept(ModBlocks.BLOOMING_SALT_BLOCK.get());
                    })
                    .build());

    private void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> {
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_FRUIT_LEAVES.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.APPLE_SAPLING.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.BARLEY_CROP.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.HOPS_VINE.get(), RenderType.cutout());
            ItemBlockRenderTypes.setRenderLayer(ModBlocks.SALT_CRYSTAL.get(), RenderType.cutout());
            });

    }

    private void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        event.register((state, world, pos, tintIndex) -> 0x3F76E4, ModBlocks.BREWERY.get());
    }
}
