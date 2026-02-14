package salo2b.beer;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = BeerMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void registerBlockColors(RegisterColorHandlersEvent.Block event) {
        // Регистрируем цвет для нашего блока пивоварни
        // 0x3F76E4 — это красивый синий цвет воды
        event.register((state, world, pos, tintIndex) -> 0x3F76E4, ModBlocks.BREWERY.get());
    }
}