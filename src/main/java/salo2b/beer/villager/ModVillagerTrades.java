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

import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.trading.ItemCost;
import net.minecraft.world.item.trading.MerchantOffer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.village.VillagerTradesEvent;

import java.util.List;

@EventBusSubscriber(modid = BeerMod.MODID)
public class ModVillagerTrades {

    @SubscribeEvent
    public static void addCustomTrades(VillagerTradesEvent event) {
        // Проверяем, что это наш Пивовар
        if (event.getType() == ModVillagers.BREWMASTER.get()) {
            List<VillagerTrades.ItemListing> tradesLvl1 = event.getTrades().get(1);
            List<VillagerTrades.ItemListing> tradesLvl2 = event.getTrades().get(2);
            List<VillagerTrades.ItemListing> tradesLvl3 = event.getTrades().get(3);

            // Уровень 1: Обычное пиво
            tradesLvl1.add((entity, random) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 3),
                    new ItemStack(ModItems.BEER.get(), 1),
                    16, 2, 0.05f));

            // Уровень 2: Сидр (Скорость) — Дороже
            tradesLvl2.add((entity, random) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 7),
                    new ItemStack(ModItems.CIDER.get(), 1),
                    12, 5, 0.05f));

            // Уровень 3: Ячменное пиво (Сила) — Самое дорогое
            tradesLvl3.add((entity, random) -> new MerchantOffer(
                    new ItemCost(Items.EMERALD, 15),
                    new ItemStack(ModItems.BARLEY_BEER.get(), 1),
                    8, 10, 0.05f));
        }
    }
}
