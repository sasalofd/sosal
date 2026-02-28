package salo2b.beer.client.screen;

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

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MillstoneScreen extends AbstractContainerScreen<MillstoneMenu> {
    // Используем стандартную текстуру инвентаря из Майнкрафта
    private static final ResourceLocation VANILLA_TEXTURE =
            ResourceLocation.withDefaultNamespace("textures/gui/container/inventory.png");

    public MillstoneScreen(MillstoneMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // Рисуем обычный фон инвентаря
        guiGraphics.blit(VANILLA_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // Рисуем два пустых слота посередине (вход и выход)
        guiGraphics.fill(x + 55, y + 34, x + 73, y + 52, 0x80000000); // Слот входа
        guiGraphics.fill(x + 115, y + 34, x + 133, y + 52, 0x80000000); // Слот выхода
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
