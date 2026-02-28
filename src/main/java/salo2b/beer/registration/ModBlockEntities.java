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

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeerMod.MODID);

    // Регистрация сущности для ротора мельницы
    public static final Supplier<BlockEntityType<WindmillRotorBlockEntity>> WINDMILL_ROTOR =
            BLOCK_ENTITIES.register("windmill_rotor",
                    () -> BlockEntityType.Builder.of(WindmillRotorBlockEntity::new, ModBlocks.WINDMILL_ROTOR.get()).build(null));

    // Регистрация Бочки
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeerBarrelBlockEntity>> BEER_BARREL_BE =
            BLOCK_ENTITIES.register("beer_barrel_be", () ->
                    BlockEntityType.Builder.of(BeerBarrelBlockEntity::new, ModBlocks.BEER_BARREL.get()).build(null));

    // Регистрация Пивоварни (Brewery)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreweryBlockEntity>> BREWERY_BE =
            BLOCK_ENTITIES.register("brewery_be", () ->
                    BlockEntityType.Builder.of(BreweryBlockEntity::new, ModBlocks.BREWERY.get()).build(null));

    // Регистрация Чана для солода
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaltVatBlockEntity>> MALT_VAT_BE =
            BLOCK_ENTITIES.register("malt_vat_be", () ->
                    BlockEntityType.Builder.of(MaltVatBlockEntity::new, ModBlocks.MALT_VAT.get()).build(null));

    // Це реєструє "мозок" для жерновів
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MillstoneBlockEntity>> MILLSTONE =
            BLOCK_ENTITIES.register("millstone",
                    () -> BlockEntityType.Builder.of(MillstoneBlockEntity::new, ModBlocks.MILLSTONE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WindmillShaftBlockEntity>> WINDMILL_SHAFT =
            BLOCK_ENTITIES.register("windmill_shaft",
                    () -> BlockEntityType.Builder.of(WindmillShaftBlockEntity::new, ModBlocks.WINDMILL_SHAFT.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GearboxBlockEntity>> GEARBOX =
            BLOCK_ENTITIES.register("gearbox",
                    () -> BlockEntityType.Builder.of(GearboxBlockEntity::new, ModBlocks.GEARBOX.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
