package salo2b.beer;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, BeerMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, BeerMod.MODID);

    // ИСПРАВЛЕННЫЙ POI: В 1.21.1 мы берем все возможные стейты блока
    public static final DeferredHolder<PoiType, PoiType> BEER_POI = POI_TYPES.register("beer_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.BREWERY.get().getStateDefinition().getPossibleStates()),
                    1, // Билеты на работу
                    1  // Радиус поиска (валидно 1)
            ));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BREWMASTER =
            PROFESSIONS.register("brewmaster", () -> new VillagerProfession(
                    "brewmaster", // Это имя ДОЛЖНО совпадать с названием файла .png
                    holder -> holder.value() == BEER_POI.get(),
                    holder -> holder.value() == BEER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(),
                    null
            ));
}