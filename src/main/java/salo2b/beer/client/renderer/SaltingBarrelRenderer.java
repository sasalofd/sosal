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
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.joml.Matrix4f;
import salo2b.beer.block.entity.SaltingBarrelBlockEntity;
import salo2b.beer.item.FishHelper;
import salo2b.beer.registration.ModItems;

public class SaltingBarrelRenderer implements BlockEntityRenderer<SaltingBarrelBlockEntity> {
    
    private static final ResourceLocation SALT_TEXTURE = ResourceLocation.withDefaultNamespace("block/white_concrete_powder");
    private static final ResourceLocation BARREL_TOP = ResourceLocation.withDefaultNamespace("block/barrel_top");
    private static final ResourceLocation BARREL_SIDE = ResourceLocation.withDefaultNamespace("block/barrel_side");

    private final CodModel<Entity> codModel;
    private final SalmonModel<Entity> salmonModel;
    private final PufferfishMidModel<Entity> pufferfishModel;
    private final TropicalFishModelA<Entity> tropicalFishModel;

    private static final ResourceLocation COD_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/cod.png");
    private static final ResourceLocation SALTED_COD_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_cod.png");
    
    private static final ResourceLocation SALMON_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/salmon.png");
    private static final ResourceLocation SALTED_SALMON_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_salmon.png");
    
    private static final ResourceLocation PUFFERFISH_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/pufferfish.png");
    private static final ResourceLocation SALTED_PUFFERFISH_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_pufferfish.png");
    
    private static final ResourceLocation TROPICAL_TEX = ResourceLocation.withDefaultNamespace("textures/entity/fish/tropical_a.png");
    private static final ResourceLocation SALTED_TROPICAL_TEX = ResourceLocation.fromNamespaceAndPath("beer", "textures/entity/fish/salted_tropical_a.png");

    public SaltingBarrelRenderer(BlockEntityRendererProvider.Context context) {
        this.codModel = new CodModel<>(context.bakeLayer(ModelLayers.COD));
        this.salmonModel = new SalmonModel<>(context.bakeLayer(ModelLayers.SALMON));
        this.pufferfishModel = new PufferfishMidModel<>(context.bakeLayer(ModelLayers.PUFFERFISH_MEDIUM));
        this.tropicalFishModel = new TropicalFishModelA<>(context.bakeLayer(ModelLayers.TROPICAL_FISH_SMALL));
    }

    @Override
    public void render(SaltingBarrelBlockEntity be, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay) {
        float openProgress = Mth.lerp(partialTicks, be.prevOpenProgress, be.openProgress);
        int saltCount = be.getSaltCount();
        double baseY = 0.125;
        double saltFillHeight = saltCount > 0 ? (saltCount / 12.0) * 0.75 : 0;
        
        Direction facing = Direction.NORTH;
        if (be.getBlockState().hasProperty(salo2b.beer.block.SaltingBarrelBlock.FACING)) {
            facing = be.getBlockState().getValue(salo2b.beer.block.SaltingBarrelBlock.FACING);
        }

        if (saltCount > 0) {
            renderSalt(poseStack, buffer, combinedLight, (float) (baseY + saltFillHeight));
        }

        renderFish(be, poseStack, buffer, combinedLight, combinedOverlay, baseY, saltFillHeight);
        renderLid(poseStack, buffer, combinedLight, combinedOverlay, openProgress, facing);
    }

    private void renderSalt(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, float y) {
        TextureAtlasSprite sprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(SALT_TEXTURE);
        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.solid());
        Matrix4f matrix = poseStack.last().pose();
        float minX = 0.125f, maxX = 0.875f, minZ = 0.125f, maxZ = 0.875f;
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        vertexBuilder.addVertex(matrix, minX, y, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(0).setLight(combinedLight).setNormal(0, 1, 0);
        vertexBuilder.addVertex(matrix, maxX, y, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(0).setLight(combinedLight).setNormal(0, 1, 0);
        vertexBuilder.addVertex(matrix, maxX, y, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(0).setLight(combinedLight).setNormal(0, 1, 0);
        vertexBuilder.addVertex(matrix, minX, y, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(0).setLight(combinedLight).setNormal(0, 1, 0);
    }

    private void renderLid(PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, float openProgress, Direction facing) {
        poseStack.pushPose();
        
        // 1. Центрируем по верху бочки
        poseStack.translate(0.5, 1.025, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(-facing.toYRot()));

        // 2. Точка фиксации: Задний Правый угол.
        // Чтобы крышка была привязана к ПРАВОМУ углу и открывалась ВПРАВО:
        // Используем 0.5 для Права в локальной системе.
        poseStack.translate(0.5, 0, -0.5);
        
        // 3. Вращаем ВПРАВО (CCW на +90 градусов в этой системе координат)
        poseStack.mulPose(Axis.YP.rotationDegrees(openProgress * 90));
        
        // 4. Сдвигаемся обратно
        poseStack.translate(-0.5, 0, 0.5);

        VertexConsumer vertexBuilder = buffer.getBuffer(RenderType.cutout());
        TextureAtlasSprite topSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BARREL_TOP);
        TextureAtlasSprite sideSprite = Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(BARREL_SIDE);

        float minX = -0.5f, maxX = 0.5f, minY = -0.0625f, maxY = 0.0625f, minZ = -0.5f, maxZ = 0.5f;

        drawQuad(poseStack, vertexBuilder, minX, maxY, minZ, maxX, maxY, maxZ, topSprite, combinedLight, combinedOverlay, Direction.UP);
        drawQuad(poseStack, vertexBuilder, minX, minY, minZ, maxX, minY, maxZ, topSprite, combinedLight, combinedOverlay, Direction.DOWN);
        drawQuad(poseStack, vertexBuilder, minX, minY, minZ, maxX, maxY, minZ, sideSprite, combinedLight, combinedOverlay, Direction.NORTH);
        drawQuad(poseStack, vertexBuilder, minX, minY, maxZ, maxX, maxY, maxZ, sideSprite, combinedLight, combinedOverlay, Direction.SOUTH);
        drawQuad(poseStack, vertexBuilder, minX, minY, minZ, minX, maxY, maxZ, sideSprite, combinedLight, combinedOverlay, Direction.WEST);
        drawQuad(poseStack, vertexBuilder, maxX, minY, minZ, maxX, maxY, maxZ, sideSprite, combinedLight, combinedOverlay, Direction.EAST);
        poseStack.popPose();
    }

    private void renderFish(SaltingBarrelBlockEntity be, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, int combinedOverlay, double baseY, double saltFillHeight) {
        double[][] fishOffsets = { {0.5, 0.5}, {0.4, 0.45}, {0.6, 0.55}, {0.45, 0.6}, {0.55, 0.4}, {0.5, 0.35} };
        float[] fishRotations = {45, 15, 75, -15, -75, 100};
        double fishY = baseY + saltFillHeight + 0.05; 
        for (int i = 0; i < be.getInventory().getSlots(); i++) {
            ItemStack stack = be.getInventory().getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(fishOffsets[i][0], fishY + (i * 0.01), fishOffsets[i][1]);
                poseStack.mulPose(Axis.YP.rotationDegrees(fishRotations[i]));
                poseStack.scale(0.45f, 0.45f, 0.45f);
                renderFishModel(stack, poseStack, buffer, combinedLight, combinedOverlay);
                poseStack.popPose();
            }
        }
    }

    private void renderFishModel(ItemStack stack, PoseStack poseStack, MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {
        int color = 0xFFFFFFFF;
        VertexConsumer vertexConsumer;
        
        if (stack.is(Items.COD) || stack.is(ModItems.SALTED_COD.get())) {
            ResourceLocation tex = FishHelper.isSaltedFish(stack) ? SALTED_COD_TEX : COD_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            codModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.SALMON) || stack.is(ModItems.SALTED_SALMON.get())) {
            ResourceLocation tex = FishHelper.isSaltedFish(stack) ? SALTED_SALMON_TEX : SALMON_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            salmonModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else if (stack.is(Items.PUFFERFISH) || stack.is(ModItems.SALTED_PUFFERFISH.get())) {
            ResourceLocation tex = FishHelper.isSaltedFish(stack) ? SALTED_PUFFERFISH_TEX : PUFFERFISH_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            pufferfishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        } else {
            ResourceLocation tex = FishHelper.isSaltedFish(stack) ? SALTED_TROPICAL_TEX : TROPICAL_TEX;
            vertexConsumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(tex));
            tropicalFishModel.renderToBuffer(poseStack, vertexConsumer, combinedLight, combinedOverlay, color);
        }
    }

    private void drawQuad(PoseStack poseStack, VertexConsumer buffer, float minX, float minY, float minZ, float maxX, float maxY, float maxZ, TextureAtlasSprite sprite, int light, int overlay, Direction side) {
        Matrix4f matrix = poseStack.last().pose();
        float u0 = sprite.getU0(), u1 = sprite.getU1(), v0 = sprite.getV0(), v1 = sprite.getV1();
        switch (side) {
            case UP:
                buffer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                buffer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                buffer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 1, 0);
                break;
            case DOWN:
                buffer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
                buffer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
                buffer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
                buffer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, -1, 0);
                break;
            case NORTH:
                buffer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
                buffer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
                buffer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
                buffer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, -1);
                break;
            case SOUTH:
                buffer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                buffer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                buffer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(0, 0, 1);
                break;
            case WEST:
                buffer.addVertex(matrix, minX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
                buffer.addVertex(matrix, minX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
                buffer.addVertex(matrix, minX, minY, minZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
                buffer.addVertex(matrix, minX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(-1, 0, 0);
                break;
            case EAST:
                buffer.addVertex(matrix, maxX, maxY, minZ).setColor(255, 255, 255, 255).setUv(u0, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
                buffer.addVertex(matrix, maxX, maxY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v0).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
                buffer.addVertex(matrix, maxX, minY, maxZ).setColor(255, 255, 255, 255).setUv(u1, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
                buffer.addVertex(matrix, maxX, minY, minZ).setColor(255, 255, 255, 255).setUv(u0, v1).setOverlay(overlay).setLight(light).setNormal(1, 0, 0);
                break;
        }
    }
}
