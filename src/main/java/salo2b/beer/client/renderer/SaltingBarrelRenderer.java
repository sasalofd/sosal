package salo2b.beer.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import salo2b.beer.block.entity.SaltingBarrelBlockEntity;

public class SaltingBarrelRenderer implements BlockEntityRenderer<SaltingBarrelBlockEntity> {
    
    // Текстура соли для уровня (используем текстуру белого песка или снега как основу, либо саму соль если она блок, но возьмем стандартную)
    private static final ResourceLocation SALT_TEXTURE = ResourceLocation.withDefaultNamespace("block/white_concrete_powder");

    public SaltingBarrelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SaltingBarrelBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        
        int saltCount = be.getSaltCount();
        
        // Базовая высота дна
        double baseY = 0.125; // 2/16
        
        // Вычисляем высоту уровня соли. Макс соли = 12. Макс высота соли внутри бочки = 14/16 (чуть ниже края)
        // 0 соли -> высота 0 (уровень baseY)
        // 12 соли -> высота 12/16 (уровень baseY + 12/16)
        double saltFillHeight = saltCount > 0 ? (saltCount / 12.0) * 0.75 : 0;
        
        // 1. РЕНДЕРИНГ УРОВНЯ СОЛИ (как плоскость)
        if (saltCount > 0) {
            TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SALT_TEXTURE);
            VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());
            Matrix4f matrix = poseStack.last().pose();

            float minX = 0.125f; // 2/16
            float maxX = 0.875f; // 14/16
            float minZ = 0.125f;
            float maxZ = 0.875f;
            float y = (float) (baseY + saltFillHeight);

            float u0 = sprite.getU0();
            float u1 = sprite.getU1();
            float v0 = sprite.getV0();
            float v1 = sprite.getV1();

            // Рисуем квадрат (плоскость)
            vertexBuilder.addVertex(matrix, minX, y, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setLight(combinedLight).setNormal(0, 1, 0);
            vertexBuilder.addVertex(matrix, maxX, y, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setLight(combinedLight).setNormal(0, 1, 0);
            vertexBuilder.addVertex(matrix, maxX, y, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setLight(combinedLight).setNormal(0, 1, 0);
            vertexBuilder.addVertex(matrix, minX, y, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setLight(combinedLight).setNormal(0, 1, 0);
        }

        // 2. РЕНДЕРИНГ РЫБЫ (всегда лежит на уровне соли)
        double[][] fishOffsets = {
            {0.5, 0.5},
            {0.4, 0.4},
            {0.6, 0.6},
            {0.4, 0.6},
            {0.6, 0.4},
            {0.5, 0.35}
        };
        float[] fishRotations = {45, 15, 75, -15, -75, 90};

        // Рыба лежит ЧУТЬ выше уровня соли, чтобы не пересекаться текстурами
        double fishY = baseY + saltFillHeight + 0.01;

        for (int i = 0; i < be.getInventory().getSlots(); i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                
                // Чуть поднимаем каждую следующую рыбу
                poseStack.translate(fishOffsets[i][0], fishY + (i * 0.005), fishOffsets[i][1]);

                // Кладем плашмя
                poseStack.mulPose(Axis.XP.rotationDegrees(90));
                // Вращаем
                poseStack.mulPose(Axis.ZP.rotationDegrees(fishRotations[i]));

                poseStack.scale(0.65f, 0.65f, 0.65f);

                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, be.getLevel(), 0);
                poseStack.popPose();
            }
        }
    }
}
