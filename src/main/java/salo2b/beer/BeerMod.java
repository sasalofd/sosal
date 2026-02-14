package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod(BeerMod.MODID)
public class BeerMod {
    public static final String MODID = "beer";

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final RegistryObject<CreativeModeTab> BEER_TAB = CREATIVE_MODE_TABS.register("beer_tab",
            () -> CreativeModeTab.builder()
                    .icon(() -> new ItemStack(ModItems.BEER.get()))
                    .title(Component.translatable("itemGroup.beer_tab"))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.HOPS_SEEDS.get());
                        output.accept(ModItems.HOPS.get());

                        // ИСПРАВЛЕНО: Берем кружку из блоков, так как в ModItems мы её удалили
                        output.accept(ModBlocks.WOODEN_MUG.get());

                        output.accept(ModItems.BEER.get());
                        output.accept(ModItems.BREWERY_ITEM.get());
                    })
                    .build());

    public BeerMod() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        MinecraftForge.EVENT_BUS.register(this);
    }
}
