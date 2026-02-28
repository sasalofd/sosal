package salo2b.beer.villager;

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

import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.npc.Villager;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModVillagers {
    public static final DeferredRegister<PoiType> POI_TYPES =
            DeferredRegister.create(Registries.POINT_OF_INTEREST_TYPE, BeerMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS =
            DeferredRegister.create(Registries.VILLAGER_PROFESSION, BeerMod.MODID);

    public static final DeferredHolder<PoiType, PoiType> BEER_POI = POI_TYPES.register("beer_poi",
            () -> new PoiType(ImmutableSet.copyOf(ModBlocks.BREWERY.get().getStateDefinition().getPossibleStates()), 1, 1));

    public static final DeferredHolder<VillagerProfession, VillagerProfession> BREWMASTER =
            PROFESSIONS.register("brewmaster", () -> new VillagerProfession("brewmaster",
                    holder -> holder.value() == BEER_POI.get(), holder -> holder.value() == BEER_POI.get(),
                    ImmutableSet.of(), ImmutableSet.of(), null));

    // БЛОКИРОВКА УРОНА (Только для тех, у кого есть тег bar_npc)
    @SubscribeEvent
    public static void onVillagerHurt(LivingIncomingDamageEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            if (villager.getTags().contains("bar_npc")) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onVillagerSpawn(EntityJoinLevelEvent event) {
        if (event.getEntity() instanceof Villager villager) {
            if (villager.getTags().contains("bar_npc")) {

                villager.setPersistenceRequired();
                villager.setInvulnerable(true);

                // Скорость и отбрасывание
                villager.getAttribute(Attributes.MOVEMENT_SPEED).setBaseValue(0.0D);
                villager.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(1.0D);

                // Отключаем коллизию через команду (Team)
                var scoreboard = villager.level().getScoreboard();
                var team = scoreboard.getPlayerTeam("bar_npc_team");
                if (team == null) {
                    team = scoreboard.addPlayerTeam("bar_npc_team");
                    team.setCollisionRule(net.minecraft.world.scores.Team.CollisionRule.NEVER);
                }
                scoreboard.addPlayerToTeam(villager.getScoreboardName(), team);

                // Очистка AI и взгляд
                villager.getBrain().removeAllBehaviors();
                villager.goalSelector.addGoal(0, new LookAtPlayerGoal(villager, Player.class, 8.0F));
            }
        }
    }

    public static void register(IEventBus eventBus) {
        POI_TYPES.register(eventBus);
        PROFESSIONS.register(eventBus);
    }
}
