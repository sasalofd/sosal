package salo2b.beer.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import salo2b.beer.block.FishDryerBlock;
import salo2b.beer.block.entity.FishDryerBlockEntity;

public class FishDryerRenderer implements BlockEntityRenderer<FishDryerBlockEntity> {
    public FishDryerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(FishDryerBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Direction facing = be.getBlockState().getValue(FishDryerBlock.FACING);
        
        poseStack.pushPose();
        
        // Вращение структуры вокруг центра LEFT_BOTTOM блока (0.5, 0.5)
        poseStack.translate(0.5, 0, 0.5);
        float rotation = -facing.toYRot() + 180;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // Рыба висит на веревке (1.0 + 15/16)
        double fishY = 1.39;
        
        // Смещение по X: сдвигаем вправо на 0.1
        double[] xOffsets = {-0.02, 0.58, 1.18, 1.78};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                // Z=0.04 - чуть перед веревкой
                poseStack.translate(xOffsets[i], fishY, 0);

                // Поворот спрайта, чтобы голова была сверху
                poseStack.mulPose(Axis.ZP.rotationDegrees(135));

                // Сдвигаем вниз совсем немного, чтобы голова была на веревке
                poseStack.translate(0, -0.2, 0);

                poseStack.scale(0.6f, 0.6f, 0.6f);

                Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED, combinedLight, combinedOverlay, poseStack, buffer, be.getLevel(), 0);
                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
}
