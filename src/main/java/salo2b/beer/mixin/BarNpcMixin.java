package salo2b.beer.mixin;

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

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class BarNpcMixin {
    @Inject(method = "isPushable", at = @At("HEAD"), cancellable = true)
    private void beer$makeBarNpcUnpushable(CallbackInfoReturnable<Boolean> cir) {
        Entity entity = (Entity) (Object) this;
        if (entity.getTags().contains("bar_npc")) {
            cir.setReturnValue(false);
        }
    }
}
