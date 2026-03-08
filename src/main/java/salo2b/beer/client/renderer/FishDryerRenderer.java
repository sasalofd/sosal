package salo2b.beer.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.CodModel;
import net.minecraft.client.model.PufferfishMidModel;
import net.minecraft.client.model.SalmonModel;
import net.minecraft.client.model.TropicalFishModelA;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import salo2b.beer.block.FishDryerBlock;
import salo2b.beer.block.entity.FishDryerBlockEntity;
import salo2b.beer.item.FishHelper;
import salo2b.beer.registration.ModItems;

public class FishDryerRenderer implements BlockEntityRenderer<FishDryerBlockEntity> {
    
    private final CodModel<Entity> codModel;
    private final SalmonModel<Entity> salmonModel;
    private final PufferfishMidModel<Entity> pufferfishModel;
    private final TropicalFishModelA<Entity> tropicalFishModel;

    private static final ResourceLocation COD_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/cod.png");
    private static final ResourceLocation SALMON_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
    private static final ResourceLocation PUFFERFISH_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/pufferfish.png");
    private static final ResourceLocation TROPICAL_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");

    public FishDryerRenderer(BlockEntityRendererProvider.Context context) {
        this.codModel = new CodModel<>(context.bakeLayer(ModelLayers.COD));
        this.salmonModel = new SalmonModel<>(context.bakeLayer(ModelLayers.SALMON));
        this.pufferfishModel = new PufferfishMidModel<>(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.tropicalFishModel = new TropicalFishModelA<>(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL));
    }

    @Override
    public void render(FishDryerBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        Direction facing = be.getBlockState().getValue(FishDryerBlock.FACING);
        
        poseStack.pushPose();
        
        // Вращение структуры вокруг центра LEFT_BOTTOM блока (0.5, 0.5)
        poseStack.translate(0.5, 0, 0.5);
        float rotation = -facing.toYRot() + 180;
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        
        // Рыба висит на веревке
        double fishY = 1.78;
        
        // Смещение по X
        double[] xOffsets = {-0.75, -0.12, 0.48, 1.08};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                
                // Плавно интерполируем угол качания для плавности рендера
                float swing = be.swingAngles[i];
                
                // Индивидуальная поправка смещения для разных моделей рыб,
                // так как их ванильные центры координат отличаются.
                double modelXOffset = 0;
                double modelZOffset = 0;


                if (stack.is(Items.PUFFERFISH) || stack.is(ModItems.SALTED_PUFFERFISH.get()) || stack.is(ModItems.DRIED_PUFFERFISH.get())) {
                    modelXOffset = 0.05; // Сдвигаем фугу чуть правее
                } else if (stack.is(Items.TROPICAL_FISH) || stack.is(ModItems.SALTED_TROPICAL_FISH.get()) || stack.is(ModItems.DRIED_TROPICAL_FISH.get())) {
                    modelXOffset = -0.05; // Сдвигаем тропическую левее
                }
                
                // Перемещаемся к точке подвеса на веревке
                poseStack.translate(xOffsets[i] + modelXOffset, fishY, modelZOffset);

                // Качание маятника вокруг точки подвеса
                poseStack.mulPose(Axis.XP.rotationDegrees(swing * 57.29f)); // радианы в градусы
                
                // Масштабируем, чтобы рыба не была гигантской
                poseStack.scale(0.65f, 0.65f, 0.65f);
                
                // Сдвигаемся вниз, чтобы точка подвеса была на веревке.
                // При вертикальном положении центр модели смещается.
                poseStack.translate(0, -0.15, 0);
                
                // Ванильные модели перевернуты (кверху пузом), поэтому переворачиваем обратно
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                
                // Поворачиваем рыбу вертикально (головой вверх)
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                
                // Поворачиваем плоскостью (боком) к игроку
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));

                renderFishModel(stack, poseStack, buffer, combinedLight, combinedOverlay);

                poseStack.popPose();
            }
        }

        poseStack.popPose();
    }
    
    private void renderFishModel(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        // Определяем тип и цвет
        int color = 0xFFFFFFFF; // Белый по умолчанию
        if (FishHelper.isDriedFish(stack)) {
            color = 0xFFA08060; // Коричневатый, сушеный
        } else if (FishHelper.isSaltedFish(stack)) {
            color = 0xFFDDDDDD; // Чуть бледный, в соли
        }
        
        VertexConsumer vertexConsumer;
        
        if (stack.is(Items.COD) || stack.is(ModItems.SALTED_COD.get()) || stack.is(ModItems.DRIED_COD.get())) {
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(COD_TEX));
            codModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.SALMON) || stack.is(ModItems.SALTED_SALMON.get()) || stack.is(ModItems.DRIED_SALMON.get())) {
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(SALMON_TEX));
            salmonModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.PUFFERFISH) || stack.is(ModItems.SALTED_PUFFERFISH.get()) || stack.is(ModItems.DRIED_PUFFERFISH.get())) {
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(PUFFERFISH_TEX));
            pufferfishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else {
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(TROPICAL_TEX));
            tropicalFishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        }
    }
}
