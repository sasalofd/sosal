package salo2b.beer.registration;

import salo2b.beer.BeerMod;
import salo2b.beer.effect.*;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = 
            DeferredRegister.create(Registries.MOB_EFFECT, BeerMod.MODID);

    public static final DeferredHolder<MobEffect, DrunkEffect> DRUNK = 
            EFFECTS.register("drunk", DrunkEffect::new);

    public static final DeferredHolder<MobEffect, MildDrunkEffect> MILD_DRUNK = 
            EFFECTS.register("mild_drunk", MildDrunkEffect::new);

    public static final DeferredHolder<MobEffect, StrongDrunkEffect> STRONG_DRUNK = 
            EFFECTS.register("strong_drunk", StrongDrunkEffect::new);

    public static final DeferredHolder<MobEffect, HangoverEffect> HANGOVER = 
            EFFECTS.register("hangover", HangoverEffect::new);

    public static final DeferredHolder<MobEffect, BlackoutEffect> BLACKOUT = 
            EFFECTS.register("blackout", BlackoutEffect::new);

    public static void register(IEventBus eventBus) {
        EFFECTS.register(eventBus);
    }
}
