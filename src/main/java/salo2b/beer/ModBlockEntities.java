package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, BeerMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<BreweryBlockEntity>> BREWERY_BE = 
            BLOCK_ENTITIES.register("brewery_be", () -> BlockEntityType.Builder.of(
                    BreweryBlockEntity::new, ModBlocks.BREWERY.get()).build(null));
}
