package salo2b.beer;

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, BeerMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, BeerMod.MODID);

    // ПОИ: Привязываем профессию к блоку пивоварни
    public static final DeferredHolder<PoiType, PoiType> BEER_POI = POI_TYPES.register("beer_poi",
            () -> new PoiType(
                    ImmutableSet.copyOf(ModBlocks.BREWERY.get().getStateDefinition().getPossibleStates()),
                    1, 1
            ));

    // Профессия Пивовара
    public static final DeferredHolder<VillagerProfession, VillagerProfession> BREWMASTER =
            PROFESSIONS.register("brewmaster", () -> new VillagerProfession(
                    "brewmaster",
                    holder -> holder.value() == BEER_POI.get(),
                    holder -> holder.value() == BEER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(),
                    null
            ));

    /**
     * Логика NPC: заставляем его стоять на месте, если он пивовар.
     * Этот метод должен быть зарегистрирован в основном шине событий Neoforge (NeoForge.EVENT_BUS).
     */
    @SubscribeEvent
    public static void onVillagerSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            if (villager.getVillagerData().getProfession() == BREWMASTER.get()) {
                // Чтобы не деспавнился
                villager.setPersistenceRequired();

                // Ограничиваем движение радиусом в 1 блок от точки появления
                villager.restrictTo(villager.blockPosition(), 1);

                // Обнуляем скорость, чтобы он не пытался бежать сквозь препятствия
                var speed = villager.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MOVEMENT_SPEED);
                if (speed != null) {
                    speed.setBaseValue(0.0D);
                }
            }
        }
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        PROFESSIONS.register(eventBus);
    }
}