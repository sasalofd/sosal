package salo2b.beer.registration;

import salo2b.beer.BeerMod;
import salo2b.beer.worldgen.SaltSpikeFeature;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModFeatures {
    public static final DeferredRegister<Feature<?>> FEATURES = 
            DeferredRegister.create(Registries.FEATURE, BeerMod.MODID);

    public static final DeferredHolder<Feature<?>, SaltSpikeFeature> SALT_SPIKE = 
            FEATURES.register("salt_spike", () -> new SaltSpikeFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus eventBus) {
        FEATURES.register(eventBus);
    }
}
