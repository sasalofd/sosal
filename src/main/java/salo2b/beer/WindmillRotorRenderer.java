package salo2b.beer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.data.ModelData;

public class WindmillRotorRenderer implements BlockEntityRenderer<WindmillRotorBlockEntity> {

    public WindmillRotorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WindmillRotorBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        BlockState state = entity.getBlockState();

        // Проверяем FACING именно ротора
        if (!state.hasProperty(WindmillRotorBlock.FACING)) return;

        Direction facing = state.getValue(WindmillRotorBlock.FACING);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        poseStack.pushPose();

        // Центрируем модель
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // Поворачиваем лопасти "лицом" к миру
        float rotationY = facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationY + 180));

        // Вращение
        float fluidAngle = entity.prevAngle + (entity.angle - entity.prevAngle) * partialTick;
        poseStack.mulPose(Axis.ZP.rotationDegrees(fluidAngle));

        // Масштаб (делаем лопасти большими)
        poseStack.scale(5.0F, 5.0F, 5.0F);

        poseStack.translate(-0.5D, -0.5D, -0.5D);

        // Отрисовка модели из JSON
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                bufferSource.getBuffer(RenderType.cutout()),
                state, model, 1.0F, 1.0F, 1.0F,
                combinedLight, combinedOverlay,
                ModelData.EMPTY, null
        );

        poseStack.popPose();
    }
}