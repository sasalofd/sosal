package salo2b.beer.registration;

import salo2b.beer.BeerMod;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.common.SoundActions;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.Keys.FLUID_TYPES, BeerMod.MODID);
    public static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(BuiltInRegistries.FLUID, BeerMod.MODID);

    // WORT FLUID TYPE
    public static final Supplier<FluidType> WORT_FLUID_TYPE = FLUID_TYPES.register("wort", () -> new FluidType(
            FluidType.Properties.create()
                    .descriptionId("fluid.beer.wort")
                    .fallDistanceModifier(0F)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .temperature(300)
                    .density(1040)
                    .viscosity(1500)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
            consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
                private static final ResourceLocation WORT_STILL = ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "block/beer_fluid");
                private static final ResourceLocation WORT_FLOW = ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "block/beer_fluid");

                @Override
                public ResourceLocation getStillTexture() { return WORT_STILL; }

                @Override
                public ResourceLocation getFlowingTexture() { return WORT_FLOW; }

                @Override
                public int getTintColor() { return 0xFF5C3A21; } // Brown Wort Color
            });
        }
    });

    // BEER FLUID TYPE
    public static final Supplier<FluidType> BEER_FLUID_TYPE = FLUID_TYPES.register("beer", () -> new FluidType(
            FluidType.Properties.create()
                    .descriptionId("fluid.beer.beer")
                    .fallDistanceModifier(0F)
                    .canExtinguish(false)
                    .canConvertToSource(false)
                    .temperature(300)
                    .density(1010)
                    .viscosity(1000)
                    .sound(SoundActions.BUCKET_FILL, SoundEvents.BUCKET_FILL)
                    .sound(SoundActions.BUCKET_EMPTY, SoundEvents.BUCKET_EMPTY)
    ) {
        @Override
        public void initializeClient(java.util.function.Consumer<net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions> consumer) {
            consumer.accept(new net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions() {
                private static final ResourceLocation BEER_STILL = ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "block/beer_fluid");
                private static final ResourceLocation BEER_FLOW = ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "block/beer_fluid");

                @Override
                public ResourceLocation getStillTexture() { return BEER_STILL; }

                @Override
                public ResourceLocation getFlowingTexture() { return BEER_FLOW; }
                
                @Override
                public int getTintColor() { return 0xFFF2C94C; } // Golden Beer Color
            });
        }
    });

    // WORT FLUIDS
    public static final Supplier<Fluid> WORT_SOURCE = FLUIDS.register("wort", () -> new BaseFlowingFluid.Source(ModFluids.WORT_PROPERTIES));
    public static final Supplier<Fluid> WORT_FLOWING = FLUIDS.register("flowing_wort", () -> new BaseFlowingFluid.Flowing(ModFluids.WORT_PROPERTIES));
    
    // BEER FLUIDS
    public static final Supplier<Fluid> BEER_SOURCE = FLUIDS.register("beer", () -> new BaseFlowingFluid.Source(ModFluids.BEER_PROPERTIES));
    public static final Supplier<Fluid> BEER_FLOWING = FLUIDS.register("flowing_beer", () -> new BaseFlowingFluid.Flowing(ModFluids.BEER_PROPERTIES));

    // PROPERTIES
    public static final BaseFlowingFluid.Properties WORT_PROPERTIES = new BaseFlowingFluid.Properties(WORT_FLUID_TYPE, WORT_SOURCE, WORT_FLOWING);
    public static final BaseFlowingFluid.Properties BEER_PROPERTIES = new BaseFlowingFluid.Properties(BEER_FLUID_TYPE, BEER_SOURCE, BEER_FLOWING);
}
