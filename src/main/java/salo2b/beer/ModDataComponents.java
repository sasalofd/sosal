package salo2b.beer;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import com.mojang.serialization.Codec;
import net.minecraft.network.codec.ByteBufCodecs;

public class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENT_TYPES =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, BeerMod.MODID);

    // Компонент для хранения времени в тиках (Long)
    public static final java.util.function.Supplier<DataComponentType<Long>> START_TIME =
            DATA_COMPONENT_TYPES.register("start_time",
                    () -> DataComponentType.<Long>builder()
                            .persistent(Codec.LONG)
                            .networkSynchronized(ByteBufCodecs.VAR_LONG)
                            .build());
}
