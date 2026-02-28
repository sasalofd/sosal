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
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

public class MaltVatScreen extends AbstractContainerScreen<MaltVatMenu> {
    private static final ResourceLocation TEXTURE =
            ResourceLocation.fromNamespaceAndPath(BeerMod.MODID, "textures/gui/malt_vat.png");

    public MaltVatScreen(MaltVatMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 176;
        this.imageHeight = 166;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        // 1. Рисуем фон
        guiGraphics.blit(TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        // 2. Рисуем стрелку (Прогресс варки)
        // Координаты стрелки на экране: x+78, y+55 (между ведрами)
        // Координаты стрелки в файле: 176, 0
        guiGraphics.blit(TEXTURE, x + 78, y + 55, 176, 0, menu.getScaledProgress(), 16);

        // 3. Рисуем ВОДУ (Пробирка)
        int waterHeight = menu.getScaledWater();
        if (waterHeight > 0) {
            // Допустим, пробирка нарисована слева, около слота с водой
            int tankX = x + 40;
            int tankBottomY = y + 70; // Низ пробирки

            // Рисуем синий прямоугольник снизу вверх
            // Цвет: 0xFF + R(00) G(00) B(FF) = Синий
            guiGraphics.fill(tankX, tankBottomY - waterHeight, tankX + 10, tankBottomY, 0xFF0000FF);
        }
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        renderTooltip(guiGraphics, mouseX, mouseY);
    }
}
