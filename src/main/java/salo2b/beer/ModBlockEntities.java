package salo2b.beer;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = 
            DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, BeerMod.MODID);

    public static final RegistryObject<BlockEntityType<BreweryBlockEntity>> BREWERY_BE = 
            BLOCK_ENTITIES.register("brewery_be", () -> BlockEntityType.Builder.of(
                    BreweryBlockEntity::new, ModBlocks.BREWERY.get()).build(null));
}