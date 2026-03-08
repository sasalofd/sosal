package salo2b.beer.registration;

import salo2b.beer.*;
import salo2b.beer.block.*;
import salo2b.beer.block.entity.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeerMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> WINDMILL_ROTOR =
            BLOCK_ENTITIES.register("windmill_rotor", () -> {
                if (ModList.get().isLoaded("create")) {
                    return BlockEntityType.Builder.of(salo2b.beer.compat.create.CreateCompat.CompatWindmillRotorBlockEntity::new, ModBlocks.WINDMILL_ROTOR.get()).build(null);
                }
                return BlockEntityType.Builder.of(WindmillRotorBlockEntity::new, ModBlocks.WINDMILL_ROTOR.get()).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeerBarrelBlockEntity>> BEER_BARREL_BE =
            BLOCK_ENTITIES.register("beer_barrel_be", () ->
                    BlockEntityType.Builder.of(BeerBarrelBlockEntity::new, ModBlocks.BEER_BARREL.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreweryBlockEntity>> BREWERY_BE =
            BLOCK_ENTITIES.register("brewery_be", () ->
                    BlockEntityType.Builder.of(BreweryBlockEntity::new, ModBlocks.BREWERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaltVatBlockEntity>> MALT_VAT_BE =
            BLOCK_ENTITIES.register("malt_vat_be", () ->
                    BlockEntityType.Builder.of(MaltVatBlockEntity::new, ModBlocks.MALT_VAT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> MILLSTONE =
            BLOCK_ENTITIES.register("millstone", () -> {
                if (ModList.get().isLoaded("create")) {
                    return BlockEntityType.Builder.of(salo2b.beer.compat.create.CreateCompat.CompatMillstoneBlockEntity::new, ModBlocks.MILLSTONE.get()).build(null);
                }
                return BlockEntityType.Builder.of(MillstoneBlockEntity::new, ModBlocks.MILLSTONE.get()).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> WINDMILL_SHAFT =
            BLOCK_ENTITIES.register("windmill_shaft", () -> {
                if (ModList.get().isLoaded("create")) {
                    return BlockEntityType.Builder.of(salo2b.beer.compat.create.CreateCompat.CompatWindmillShaftBlockEntity::new, ModBlocks.WINDMILL_SHAFT.get()).build(null);
                }
                return BlockEntityType.Builder.of(WindmillShaftBlockEntity::new, ModBlocks.WINDMILL_SHAFT.get()).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<?>> GEARBOX =
            BLOCK_ENTITIES.register("gearbox", () -> {
                if (ModList.get().isLoaded("create")) {
                    return BlockEntityType.Builder.of(salo2b.beer.compat.create.CreateCompat.CompatGearboxBlockEntity::new, ModBlocks.GEARBOX.get()).build(null);
                }
                return BlockEntityType.Builder.of(GearboxBlockEntity::new, ModBlocks.GEARBOX.get()).build(null);
            });

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FishDryerBlockEntity>> FISH_DRYER_BE =
            BLOCK_ENTITIES.register("fish_dryer_be", () ->
                    BlockEntityType.Builder.of(FishDryerBlockEntity::new, ModBlocks.FISH_DRYER.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SaltingBarrelBlockEntity>> SALTING_BARREL_BE =
            BLOCK_ENTITIES.register("salting_barrel_be", () ->
                    BlockEntityType.Builder.of(SaltingBarrelBlockEntity::new, ModBlocks.SALTING_BARREL.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
