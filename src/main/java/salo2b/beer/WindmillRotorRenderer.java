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
    public void render(WindmillRotorBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState state = entity.getBlockState();

        // Защита от вылета
        if (!state.hasProperty(WindmillRotorBlock.FACING)) return;

        Direction facing = state.getValue(WindmillRotorBlock.FACING);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        poseStack.pushPose();

        // 1. Сдвигаем в центр блока
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // 2. Поворачиваем всю сцену "лицом" к игроку. (+180 градусов компенсации)
        float rotationY = facing.toYRot();
        poseStack.mulPose(Axis.YP.rotationDegrees(-rotationY + 180));

        // 3. Крутим лопасти вокруг оси Z
        float fluidAngle = entity.prevAngle + (entity.angle - entity.prevAngle) * partialTick;
        poseStack.mulPose(Axis.ZP.rotationDegrees(fluidAngle));

        // ==================================================
        // УВЕЛИЧИВАЕМ РАЗМЕР ЛОПАСТЕЙ!
        // Меняй 2.5F на любое другое число (например, 3.0F или 4.0F),
        // если хочешь сделать их еще больше или немного меньше.
        // Делаем лопасти по-настоящему огромными!
        poseStack.scale(5.0F, 5.0F, 5.0F);
        // ==================================================

        // 4. Возвращаем координаты на место
        poseStack.translate(-0.5D, -0.5D, -0.5D);

        // Отрисовка
        Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.cutout()),
                state, model, 1.0F, 1.0F, 1.0F,
                combinedLight, combinedOverlay,
                ModelData.EMPTY, null
        );

        poseStack.popPose();
    }
}