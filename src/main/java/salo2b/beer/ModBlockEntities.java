package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;
import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeerMod.MODID);

    // Регистрация Бочки
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BeerBarrelBlockEntity>> BEER_BARREL_BE =
            BLOCK_ENTITIES.register("beer_barrel_be", () ->
                    BlockEntityType.Builder.of(BeerBarrelBlockEntity::new, ModBlocks.BEER_BARREL.get()).build(null));

    // Регистрация Пивоварни (Brewery)
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreweryBlockEntity>> BREWERY_BE =
            BLOCK_ENTITIES.register("brewery_be", () ->
                    BlockEntityType.Builder.of(BreweryBlockEntity::new, ModBlocks.BREWERY.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MaltVatBlockEntity>> MALT_VAT_BE =
            BLOCK_ENTITIES.register("malt_vat_be", () ->
                    BlockEntityType.Builder.of(MaltVatBlockEntity::new, ModBlocks.MALT_VAT.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}
