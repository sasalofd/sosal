package salo2b.beer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.state.BlockState;

public class BeerBarrelRenderer implements BlockEntityRenderer<BeerBarrelBlockEntity> {
    private final BlockRenderDispatcher dispatcher;

    public BeerBarrelRenderer(BlockEntityRendererProvider.Context context) {
        this.dispatcher = context.getBlockRenderDispatcher();
    }

    @Override
    public void render(BeerBarrelBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState state = be.getBlockState();
        if (!(state.getBlock() instanceof BeerBarrelBlock)) return;

        float scale = be.getExpansionScale(partialTicks);
        
        // Время до взрыва
        long timeLeft = be.getTargetTime() - be.getLevel().getGameTime();

        // Пульсация только если до взрыва более 4 секунд (80 тиков)
        if (scale > 1.0f && timeLeft > 80) {
            // Очень мягкая пульсация (амплитуда 0.005 вместо 0.02)
            float pulse = Mth.sin((be.getLevel().getGameTime() + partialTicks) * 0.2f) * 0.005f;
            scale += pulse;
        }

        poseStack.pushPose();

        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.scale(scale, scale, scale);
        poseStack.translate(-0.5, -0.5, -0.5);

        this.dispatcher.getModelRenderer().tesselateBlock(
                be.getLevel(),
                this.dispatcher.getBlockModel(state),
                state,
                be.getBlockPos(),
                poseStack,
                buffer.getBuffer(RenderType.cutout()),
                false,
                be.getLevel().random,
                state.getSeed(be.getBlockPos()),
                combinedOverlay
        );

        poseStack.popPose();
    }
}