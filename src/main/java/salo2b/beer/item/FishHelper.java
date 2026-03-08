package salo2b.beer.item;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import salo2b.beer.registration.ModItems;

public class FishHelper {
    public static Item getSaltedVariant(Item raw) {
        if (raw == Items.COD) return ModItems.SALTED_COD.get();
        if (raw == Items.SALMON) return ModItems.SALTED_SALMON.get();
        if (raw == Items.PUFFERFISH) return ModItems.SALTED_PUFFERFISH.get();
        if (raw == Items.TROPICAL_FISH) return ModItems.SALTED_TROPICAL_FISH.get();
        return null;
    }

    public static Item getDriedVariant(Item salted) {
        if (salted == ModItems.SALTED_COD.get()) return ModItems.DRIED_COD.get();
        if (salted == ModItems.SALTED_SALMON.get()) return ModItems.DRIED_SALMON.get();
        if (salted == ModItems.SALTED_PUFFERFISH.get()) return ModItems.DRIED_PUFFERFISH.get();
        if (salted == ModItems.SALTED_TROPICAL_FISH.get()) return ModItems.DRIED_TROPICAL_FISH.get();
        return null;
    }

    public static boolean isRawFish(ItemStack stack) {
        return getSaltedVariant(stack.getItem()) != null;
    }

    public static boolean isSaltedFish(ItemStack stack) {
        return getDriedVariant(stack.getItem()) != null;
    }
    
    public static boolean isDriedFish(ItemStack stack) {
        return stack.is(ModItems.DRIED_COD.get()) || stack.is(ModItems.DRIED_SALMON.get()) || stack.is(ModItems.DRIED_PUFFERFISH.get()) || stack.is(ModItems.DRIED_TROPICAL_FISH.get());
    }
}
