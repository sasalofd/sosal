package salo2b.beer.client.renderer;

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

public class WindmillShaftRenderer implements BlockEntityRenderer<WindmillShaftBlockEntity> {
    public WindmillShaftRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(WindmillShaftBlockEntity entity, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        BlockState state = entity.getBlockState();
        if (!state.hasProperty(WindmillShaftBlock.FACING)) return;

        Direction facing = state.getValue(WindmillShaftBlock.FACING);
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);

        poseStack.pushPose();
        poseStack.translate(0.5D, 0.5D, 0.5D);

        // 1. Считаем угол ОДИН раз (без дублей!)
        float fluidAngle = entity.prevAngle + (entity.angle - entity.prevAngle) * partialTick;

        if (facing.getAxis() == Direction.Axis.Y) {
            // 2. Инвертируем вращение для вертикали (ставим минус)
            poseStack.mulPose(Axis.YP.rotationDegrees(-fluidAngle));
        } else {
            float rotationY = facing.toYRot();
            poseStack.mulPose(Axis.YP.rotationDegrees(-rotationY));
            poseStack.mulPose(Axis.ZP.rotationDegrees(fluidAngle));
        }

        poseStack.translate(-0.5D, -0.5D, -0.5D);

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
