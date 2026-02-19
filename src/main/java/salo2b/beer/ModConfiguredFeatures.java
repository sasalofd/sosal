package salo2b.beer;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModConfiguredFeatures {
    // Создаем ключ для нашей яблони
    public static final ResourceKey<ConfiguredFeature<?, ?>> APPLE_TREE =
            ResourceKey.create(Registries.CONFIGURED_FEATURE,
                    ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "apple_tree"));
}
