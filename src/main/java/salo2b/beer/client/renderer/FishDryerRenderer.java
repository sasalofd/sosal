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
import org.joml.Matrix4f;

public class FishDryerRenderer implements BlockEntityRenderer<FishDryerBlockEntity> {
    
    private final CodModel<Entity> codModel;
    private final SalmonModel<Entity> salmonModel;
    private final PufferfishMidModel<Entity> pufferfishModel;
    private final TropicalFishModelA<Entity> tropicalFishModel;

    private static final ResourceLocation COD_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/cod.png");
    private static final ResourceLocation SALTED_COD_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_cod.png");
    private static final ResourceLocation DRIED_COD_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/dried_cod.png");
    
    private static final ResourceLocation SALMON_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
    private static final ResourceLocation SALTED_SALMON_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_salmon.png");
    private static final ResourceLocation DRIED_SALMON_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/dried_salmon.png");
    
    private static final ResourceLocation PUFFERFISH_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/pufferfish.png");
    private static final ResourceLocation SALTED_PUFFERFISH_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_pufferfish.png");
    private static final ResourceLocation DRIED_PUFFERFISH_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/dried_pufferfish.png");
    
    private static final ResourceLocation TROPICAL_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation SALTED_TROPICAL_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_tropical_a.png");
    private static final ResourceLocation DRIED_TROPICAL_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/dried_tropical_a.png");
    
    private static final ResourceLocation ROPE_TEX = ResourceLocation.withDefaultNamespace("textures/block/white_wool.png");

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
        
        // Точка подвеса веревки (наверху перекладины)
        double topY = 2.94;
        // Длина веревки
        double ropeLength = 1.16;
        
        // Истинное смещение центров веревок по X (относительно центра LEFT блока)
        double[] ropeXOffsets = {0.09375, 0.71875, 1.34375, 1.90625};

        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            
            poseStack.pushPose();
            
            float swingX = be.swingAnglesX[i];
            float swingZ = be.swingAnglesZ[i];
            
            // 1. Перемещаемся к истинной точке подвеса веревки
            poseStack.translate(ropeXOffsets[i], topY, 0.0);

            // 2. Вращаем маятник (качание веревки и рыбы вместе)
            poseStack.mulPose(Axis.ZP.rotationDegrees(swingX * 57.29f)); // Влево-вправо
            poseStack.mulPose(Axis.XP.rotationDegrees(swingZ * 57.29f)); // Вперед-назад
            
            // 3. Рисуем веревку сверху вниз
            renderRope(poseStack, buffer, combinedLight, combinedOverlay, ropeLength);

            if (!stack.isEmpty()) {
                // 4. Спускаемся в конец веревки
                poseStack.translate(0, -ropeLength, 0);

                // 5. Компенсация внутреннего смещения модели ванильной рыбы.
                // Модели рыб имеют смещенный центр примерно на +0.84375 по оси X после всех поворотов.
                double modelXOffset = -0.84375; 

                if (stack.is(Items.PUFFERFISH) || stack.is(ModItems.SALTED_PUFFERFISH.get()) || stack.is(ModItems.DRIED_PUFFERFISH.get())) {
                    modelXOffset += 0.05; 
                } else if (stack.is(Items.TROPICAL_FISH) || stack.is(ModItems.SALTED_TROPICAL_FISH.get()) || stack.is(ModItems.DRIED_TROPICAL_FISH.get())) {
                    modelXOffset -= 0.05; 
                }
                
                poseStack.translate(modelXOffset, 0, 0);
                
                // Масштабируем рыбу
                poseStack.scale(0.65f, 0.65f, 0.65f);
                
                // Корректировка высоты рта к концу веревки
                poseStack.translate(0, -0.15, 0);
                
                // Разворачиваем ванильную модель в вертикальное положение
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
                poseStack.mulPose(Axis.XP.rotationDegrees(-90));
                poseStack.mulPose(Axis.ZP.rotationDegrees(90));

                renderFishModel(stack, poseStack, buffer, combinedLight, combinedOverlay);
            }

            poseStack.popPose();
        }

        poseStack.popPose();
    }

    private void renderRope(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, double length) {
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entitySolid(ROPE_TEX));
        Matrix4f matrix4f = poseStack.last().pose();
        
        float w = 0.025f; // Чуть более толстая веревка
        
        // Front face
        vertexConsumer.addVertex(matrix4f, -w, 0, w).setColor(200, 200, 200, 255).setUv(0, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix4f, -w, (float)-length, w).setColor(200, 200, 200, 255).setUv(0, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix4f, w, (float)-length, w).setColor(200, 200, 200, 255).setUv(w*2, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, 1);
        vertexConsumer.addVertex(matrix4f, w, 0, w).setColor(200, 200, 200, 255).setUv(w*2, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, 1);

        // Back face
        vertexConsumer.addVertex(matrix4f, w, 0, -w).setColor(200, 200, 200, 255).setUv(0, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix4f, w, (float)-length, -w).setColor(200, 200, 200, 255).setUv(0, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix4f, -w, (float)-length, -w).setColor(200, 200, 200, 255).setUv(w*2, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, -1);
        vertexConsumer.addVertex(matrix4f, -w, 0, -w).setColor(200, 200, 200, 255).setUv(w*2, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(0, 0, -1);

        // Left face
        vertexConsumer.addVertex(matrix4f, -w, 0, -w).setColor(200, 200, 200, 255).setUv(0, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1, 0, 0);
        vertexConsumer.addVertex(matrix4f, -w, (float)-length, -w).setColor(200, 200, 200, 255).setUv(0, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1, 0, 0);
        vertexConsumer.addVertex(matrix4f, -w, (float)-length, w).setColor(200, 200, 200, 255).setUv(w*2, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1, 0, 0);
        vertexConsumer.addVertex(matrix4f, -w, 0, w).setColor(200, 200, 200, 255).setUv(w*2, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(-1, 0, 0);

        // Right face
        vertexConsumer.addVertex(matrix4f, w, 0, w).setColor(200, 200, 200, 255).setUv(0, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1, 0, 0);
        vertexConsumer.addVertex(matrix4f, w, (float)-length, w).setColor(200, 200, 200, 255).setUv(0, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1, 0, 0);
        vertexConsumer.addVertex(matrix4f, w, (float)-length, -w).setColor(200, 200, 200, 255).setUv(w*2, (float)length).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1, 0, 0);
        vertexConsumer.addVertex(matrix4f, w, 0, -w).setColor(200, 200, 200, 255).setUv(w*2, 0).setOverlay(combinedOverlay).setLight(combinedLight).setNormal(1, 0, 0);
    }
    
    private void renderFishModel(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        int color = 0xFFFFFFFF; // Белый по умолчанию (используем цвета самой текстуры)
        
        VertexConsumer vertexConsumer;
        
        if (stack.is(Items.COD) || stack.is(ModItems.SALTED_COD.get()) || stack.is(ModItems.DRIED_COD.get())) {
            ResourceLocation tex = COD_TEX;
            if (FishHelper.isDriedFish(stack)) tex = DRIED_COD_TEX;
            else if (FishHelper.isSaltedFish(stack)) tex = SALTED_COD_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            codModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.SALMON) || stack.is(ModItems.SALTED_SALMON.get()) || stack.is(ModItems.DRIED_SALMON.get())) {
            ResourceLocation tex = SALMON_TEX;
            if (FishHelper.isDriedFish(stack)) tex = DRIED_SALMON_TEX;
            else if (FishHelper.isSaltedFish(stack)) tex = SALTED_SALMON_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            salmonModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.PUFFERFISH) || stack.is(ModItems.SALTED_PUFFERFISH.get()) || stack.is(ModItems.DRIED_PUFFERFISH.get())) {
            ResourceLocation tex = PUFFERFISH_TEX;
            if (FishHelper.isDriedFish(stack)) tex = DRIED_PUFFERFISH_TEX;
            else if (FishHelper.isSaltedFish(stack)) tex = SALTED_PUFFERFISH_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            pufferfishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else {
            ResourceLocation tex = TROPICAL_TEX;
            if (FishHelper.isDriedFish(stack)) tex = DRIED_TROPICAL_TEX;
            else if (FishHelper.isSaltedFish(stack)) tex = SALTED_TROPICAL_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            tropicalFishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        }
    }
}
