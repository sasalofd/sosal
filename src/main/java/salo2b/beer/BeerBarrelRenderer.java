package salo2b.beer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.block.state.BlockState;

public class BeerBarrelRenderer implements BlockEntityRenderer<BeerBarrelBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public BeerBarrelRenderer(BlockEntityRendererProvider.Context context) {
        // Получаем стандартный диспетчер отрисовки блоков
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BeerBarrelBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BeerBarrelBlock)) return;

        // Получаем масштаб из BlockEntity (от 1.0 до 1.2)
        float scale = be.getExpansionScale(partialTicks);

        poseStack.pushPose();

        // 1. Сдвигаем в центр блока (чтобы увеличивалась из центра, а не из угла)
        poseStack.translate(0.5, 0.5, 0.5);

        // 2. Применяем масштаб
        poseStack.scale(scale, scale, scale);

        // 3. Сдвигаем обратно
        poseStack.translate(-0.5, -0.5, -0.5);

        // 4. Отрисовываем саму модель блока с новыми параметрами
        this.dispatcher.renderSingleBlock(state, poseStack, buffer, combinedLight, combinedOverlay);

        poseStack.popPose();
    }
}