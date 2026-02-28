package salo2b.beer.registration;

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

import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.network.IContainerFactory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModMenuTypes {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, "beer");

    public static final DeferredHolder<MenuType<?>, MenuType<MillstoneMenu>> MILLSTONE_MENU =
            MENUS.register("millstone_menu", () -> IMenuTypeExtension.create(MillstoneMenu::new));
    public static final DeferredHolder<MenuType<?>, MenuType<MaltVatMenu>> MALT_VAT_MENU =
            MENUS.register("malt_vat_menu", () -> IMenuTypeExtension.create(MaltVatMenu::new));
}
