package salo2b.beer.effect;

import salo2b.beer.registration.ModEffects;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.living.MobEffectEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;

public class DrunkennessEvents {
    
    @SubscribeEvent
    public static void onEffectRemove(MobEffectEvent.Remove event) {
        if (event.getEffect() == null || event.getEffectInstance() == null) return;
        
        if (event.getEffect().is(ModEffects.DRUNK) || 
            event.getEffect().is(ModEffects.MILD_DRUNK) || 
            event.getEffect().is(ModEffects.STRONG_DRUNK) || 
            event.getEffect().is(ModEffects.HANGOVER) || 
            event.getEffect().is(ModEffects.BLACKOUT)) {
            
            // Разрешаем естественное истечение времени
            if (event.getEffectInstance().getDuration() <= 2) {
                return; 
            }
            
            // Разрешаем удаление, если установлен наш флаг
            if (event.getEntity() instanceof Player player) {
                if (player.getPersistentData().getBoolean("beer.allow_remove")) {
                    return;
                }
            }

            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (player.level().isClientSide) return;

        CompoundTag data = player.getPersistentData();
        
        if (data.getBoolean("beer.needs_update")) {
            data.remove("beer.needs_update");
            int mugs = data.getInt("beer.mug_count");
            
            data.putBoolean("beer.allow_remove", true);
            if (mugs <= 0) {
                data.putInt("beer.state", 0);
                data.putBoolean("beer.trigger_pending", false);
                player.removeEffect(ModEffects.DRUNK);
                player.removeEffect(ModEffects.MILD_DRUNK);
                player.removeEffect(ModEffects.STRONG_DRUNK);
                player.removeEffect(ModEffects.HANGOVER);
            } else {
                int duration = 12000 + (mugs - 1) * 4800;
                player.removeEffect(ModEffects.DRUNK);
                player.addEffect(new MobEffectInstance(ModEffects.DRUNK, duration, mugs - 1, false, true, true));
            }
            data.putBoolean("beer.allow_remove", false);
        }
    }
}
