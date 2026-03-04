package salo2b.beer.worldgen;

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
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;

public class ModConfiguredFeatures {
    // Создаем ключ для нашей яблони
    public static final ResourceKey<ConfiguredFeature<?, ?>> APPLE_TREE = 
            ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                    ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "apple_tree"));

    public static final ResourceKey<ConfiguredFeature<?, ?>> SALT_SPIKE = 
            ResourceKey.create(Registries.CONFIGURED_FEATURE, 
                    ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "salt_spike"));
    }
