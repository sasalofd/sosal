package salo2b.beer.compat.create;

import salo2b.beer.block.*;
import salo2b.beer.registration.ModItems;
import salo2b.beer.menu.MillstoneMenu;
import salo2b.beer.registration.ModBlockEntities;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlock;
import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import com.simibubi.create.foundation.block.IBE;

public class CreateCompat {

    public static Block createRotor() {
        return new CompatWindmillRotorBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion().strength(3.0f));
    }

    public static Block createShaft() {
        return new CompatWindmillShaftBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion().strength(2.0f));
    }

    public static Block createGearbox() {
        return new CompatGearboxBlock(BlockBehaviour.Properties.of().sound(SoundType.WOOD).noOcclusion().strength(3.0f));
    }

    public static Block createMillstone() {
        return new CompatMillstoneBlock(BlockBehaviour.Properties.of().sound(SoundType.STONE).noOcclusion().strength(3.5f).requiresCorrectToolForDrops());
    }

    public static BlockEntityType.BlockEntitySupplier<?> getRotorBESupplier() { return CompatWindmillRotorBlockEntity::new; }
    public static BlockEntityType.BlockEntitySupplier<?> getShaftBESupplier() { return CompatWindmillShaftBlockEntity::new; }
    public static BlockEntityType.BlockEntitySupplier<?> getGearboxBESupplier() { return CompatGearboxBlockEntity::new; }
    public static BlockEntityType.BlockEntitySupplier<?> getMillstoneBESupplier() { return CompatMillstoneBlockEntity::new; }

    // ==========================================
    // WINDMILL ROTOR
    // ==========================================

    public static class CompatWindmillRotorBlock extends KineticBlock implements IBE<CompatWindmillRotorBlockEntity> {
        public static final MapCodec<CompatWindmillRotorBlock> CODEC = simpleCodec(CompatWindmillRotorBlock::new);
        public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

        public CompatWindmillRotorBlock(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }

        @Override protected MapCodec<? extends KineticBlock> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
        @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) {
            return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
        }
        @Override protected BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
        @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
        @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(FACING).getAxis(); }
        @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return face == state.getValue(FACING).getOpposite(); }
        @Override public Class<CompatWindmillRotorBlockEntity> getBlockEntityClass() { return CompatWindmillRotorBlockEntity.class; }
        @Override public BlockEntityType<? extends CompatWindmillRotorBlockEntity> getBlockEntityType() { return (BlockEntityType<? extends CompatWindmillRotorBlockEntity>) ModBlockEntities.WINDMILL_ROTOR.get(); }
        @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    }

    public static class CompatWindmillRotorBlockEntity extends GeneratingKineticBlockEntity {
        private boolean updateNetwork = true;
        public CompatWindmillRotorBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WINDMILL_ROTOR.get(), pos, state); }
        @Override public float getGeneratedSpeed() {
            return 32f; // Always return positive speed so it spins consistently in all directions
        }
        @Override public float calculateAddedStressCapacity() { return 8192f; }
        @Override public void tick() {
            super.tick();
            if (level != null && updateNetwork && !level.isClientSide) { updateGeneratedRotation(); updateNetwork = false; }
        }
    }

    public static class CompatWindmillRotorRenderer extends KineticBlockEntityRenderer<CompatWindmillRotorBlockEntity> {
        public CompatWindmillRotorRenderer(BlockEntityRendererProvider.Context context) { super(context); }
        @Override protected void renderSafe(CompatWindmillRotorBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
            BlockState state = be.getBlockState();
            if (!state.hasProperty(CompatWindmillRotorBlock.FACING)) return;
            Direction facing = state.getValue(CompatWindmillRotorBlock.FACING);
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            float speed = be.getSpeed();
            float time = be.getLevel().getGameTime() + partialTicks;
            float fluidAngle = (time * speed * 1f / 10f) % 360;
            ms.pushPose();
            ms.translate(0.5D, 0.5D, 0.5D);
            ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot() + 180));
            ms.mulPose(Axis.ZP.rotationDegrees(fluidAngle));
            ms.scale(5.0F, 5.0F, 5.0F);
            ms.translate(-0.5D, -0.5D, -0.5D);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer.getBuffer(RenderType.cutout()), state, model, 1.0F, 1.0F, 1.0F, light, overlay, ModelData.EMPTY, null);
            ms.popPose();
        }
    }

    // ==========================================
    // WINDMILL SHAFT
    // ==========================================

    public static class CompatWindmillShaftBlock extends KineticBlock implements IBE<CompatWindmillShaftBlockEntity> {
        public static final MapCodec<CompatWindmillShaftBlock> CODEC = simpleCodec(CompatWindmillShaftBlock::new);
        public static final DirectionProperty FACING = BlockStateProperties.FACING;
        public CompatWindmillShaftBlock(Properties properties) { super(properties); this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH)); }
        @Override protected MapCodec<? extends KineticBlock> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
        @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getClickedFace()); }
        @Override protected BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
        @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }
        @Override public Direction.Axis getRotationAxis(BlockState state) { return state.getValue(FACING).getAxis(); }
        @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return face.getAxis() == state.getValue(FACING).getAxis(); }
        @Override public Class<CompatWindmillShaftBlockEntity> getBlockEntityClass() { return CompatWindmillShaftBlockEntity.class; }
        @Override public BlockEntityType<? extends CompatWindmillShaftBlockEntity> getBlockEntityType() { return (BlockEntityType<? extends CompatWindmillShaftBlockEntity>) ModBlockEntities.WINDMILL_SHAFT.get(); }
        @Override protected RenderShape getRenderShape(BlockState state) { return RenderShape.ENTITYBLOCK_ANIMATED; }
    }

    public static class CompatWindmillShaftBlockEntity extends KineticBlockEntity {
        public CompatWindmillShaftBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.WINDMILL_SHAFT.get(), pos, state); }
    }

    public static class CompatWindmillShaftRenderer extends KineticBlockEntityRenderer<CompatWindmillShaftBlockEntity> {
        public CompatWindmillShaftRenderer(BlockEntityRendererProvider.Context context) { super(context); }
        @Override protected void renderSafe(CompatWindmillShaftBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
            BlockState state = be.getBlockState();
            if (!state.hasProperty(CompatWindmillShaftBlock.FACING)) return;
            Direction facing = state.getValue(CompatWindmillShaftBlock.FACING);
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            float speed = be.getSpeed();
            float time = be.getLevel().getGameTime() + partialTicks;
            float fluidAngle = (time * speed * 1f / 10f) % 360;
            ms.pushPose();
            ms.translate(0.5D, 0.5D, 0.5D);
            if (facing.getAxis() == Direction.Axis.Y) ms.mulPose(Axis.YP.rotationDegrees(-fluidAngle));
            else { ms.mulPose(Axis.YP.rotationDegrees(-facing.toYRot())); ms.mulPose(Axis.ZP.rotationDegrees(fluidAngle)); }
            ms.translate(-0.5D, -0.5D, -0.5D);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer.getBuffer(RenderType.cutout()), state, model, 1.0F, 1.0F, 1.0F, light, overlay, ModelData.EMPTY, null);
            ms.popPose();
        }
    }

    // ==========================================
    // GEARBOX
    // ==========================================

    public static class CompatGearboxBlock extends KineticBlock implements IBE<CompatGearboxBlockEntity> {
        public static final MapCodec<CompatGearboxBlock> CODEC = simpleCodec(CompatGearboxBlock::new);
        public CompatGearboxBlock(Properties properties) { super(properties); }
        @Override protected MapCodec<? extends KineticBlock> codec() { return CODEC; }
        @Override public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }
        @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return true; }
        @Override public Class<CompatGearboxBlockEntity> getBlockEntityClass() { return CompatGearboxBlockEntity.class; }
        @Override public BlockEntityType<? extends CompatGearboxBlockEntity> getBlockEntityType() { return (BlockEntityType<? extends CompatGearboxBlockEntity>) ModBlockEntities.GEARBOX.get(); }
    }

    public static class CompatGearboxBlockEntity extends KineticBlockEntity {
        public CompatGearboxBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.GEARBOX.get(), pos, state); }
    }

    // ==========================================
    // MILLSTONE
    // ==========================================

    public static class CompatMillstoneBlock extends KineticBlock implements IBE<CompatMillstoneBlockEntity> {
        public static final MapCodec<CompatMillstoneBlock> CODEC = simpleCodec(CompatMillstoneBlock::new);
        public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

        public CompatMillstoneBlock(Properties properties) {
            super(properties);
            this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        }
        @Override protected MapCodec<? extends KineticBlock> codec() { return CODEC; }
        @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
        @Nullable @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite()); }
        @Override protected BlockState rotate(BlockState state, Rotation rot) { return state.setValue(FACING, rot.rotate(state.getValue(FACING))); }
        @Override protected BlockState mirror(BlockState state, Mirror mirror) { return state.rotate(mirror.getRotation(state.getValue(FACING))); }

        @Override protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CompatMillstoneBlockEntity millstone) {
                    ItemStack output = millstone.inventory.getStackInSlot(1);
                    ItemStack input = millstone.inventory.getStackInSlot(0);
                    if (!output.isEmpty()) {
                        player.getInventory().add(output.copy());
                        millstone.inventory.setStackInSlot(1, ItemStack.EMPTY);
                        millstone.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        return InteractionResult.SUCCESS;
                    } else if (!input.isEmpty()) {
                        player.getInventory().add(input.copy());
                        millstone.inventory.setStackInSlot(0, ItemStack.EMPTY);
                        millstone.progress = 0;
                        millstone.setChanged();
                        level.sendBlockUpdated(pos, state, state, 3);
                        return InteractionResult.SUCCESS;
                    }
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }

        @Override protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
            if (!level.isClientSide) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CompatMillstoneBlockEntity millstone) {
                    if (!stack.isEmpty() && (stack.is(ModItems.MALT.get()) || stack.is(ModItems.BARLEY.get()))) {
                        ItemStack remainder = millstone.inventory.insertItem(0, stack.copy(), false);
                        if (remainder.getCount() < stack.getCount()) {
                            player.setItemInHand(hand, remainder);
                            millstone.setChanged();
                            level.sendBlockUpdated(pos, state, state, 3);
                            return ItemInteractionResult.SUCCESS;
                        }
                    }
                }
            }
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        @Override public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
            if (state.getBlock() != newState.getBlock()) {
                BlockEntity be = level.getBlockEntity(pos);
                if (be instanceof CompatMillstoneBlockEntity millstone) {
                    for (int i = 0; i < millstone.inventory.getSlots(); i++) Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), millstone.inventory.getStackInSlot(i));
                }
                super.onRemove(state, level, pos, newState, isMoving);
            }
        }

        @Override public Direction.Axis getRotationAxis(BlockState state) { return Direction.Axis.Y; }
        @Override public boolean hasShaftTowards(LevelReader world, BlockPos pos, BlockState state, Direction face) { return face == Direction.DOWN || face == Direction.UP; }
        @Override public Class<CompatMillstoneBlockEntity> getBlockEntityClass() { return CompatMillstoneBlockEntity.class; }
        @Override public BlockEntityType<? extends CompatMillstoneBlockEntity> getBlockEntityType() { return (BlockEntityType<? extends CompatMillstoneBlockEntity>) ModBlockEntities.MILLSTONE.get(); }
    }

    public static class CompatMillstoneBlockEntity extends KineticBlockEntity implements salo2b.beer.block.entity.IMillstoneBE {
        public final ItemStackHandler inventory = new ItemStackHandler(2) { @Override protected void onContentsChanged(int slot) { setChanged(); } };
        @Override public ItemStackHandler getInventory() { return inventory; }
        @Override public BlockPos getBlockPos() { return worldPosition; }
        public float progress = 0;
        public final int MAX_PROGRESS = 100;
        public CompatMillstoneBlockEntity(BlockPos pos, BlockState state) { super(ModBlockEntities.MILLSTONE.get(), pos, state); }
        @Override public void tick() {
            super.tick();
            if (level == null || level.isClientSide) return;
            ItemStack input = inventory.getStackInSlot(0);
            ItemStack output = inventory.getStackInSlot(1);
            float speed = Math.abs(getSpeed());
            if (speed > 0 && !input.isEmpty() && (input.is(ModItems.MALT.get()) || input.is(ModItems.BARLEY.get())) && canInsertResult(output)) {
                progress += speed / 16.0f;
                if (progress >= MAX_PROGRESS) { craftItem(); progress = 0; }
                setChanged();
            } else if (progress > 0) { progress = 0; setChanged(); level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3); }
        }
        private boolean canInsertResult(ItemStack output) { return output.isEmpty() || (output.is(ModItems.CRUSHED_MALT.get()) && output.getCount() < output.getMaxStackSize()); }
        private void craftItem() {
            ItemStack input = inventory.getStackInSlot(0);
            if (input.is(ModItems.MALT.get()) || input.is(ModItems.BARLEY.get())) {
                input.shrink(1);
                inventory.insertItem(1, new ItemStack(ModItems.CRUSHED_MALT.get()), false);
                setChanged();
            }
        }
        @Override protected void write(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) { super.write(tag, registries, clientPacket); tag.put("inventory", inventory.serializeNBT(registries)); tag.putFloat("progress", progress); }
        @Override protected void read(CompoundTag tag, HolderLookup.Provider registries, boolean clientPacket) { super.read(tag, registries, clientPacket); if (tag.contains("inventory")) inventory.deserializeNBT(registries, tag.getCompound("inventory")); if (tag.contains("progress")) progress = tag.getFloat("progress"); }
    }

    public static class CompatMillstoneRenderer extends KineticBlockEntityRenderer<CompatMillstoneBlockEntity> {
        public CompatMillstoneRenderer(BlockEntityRendererProvider.Context context) { super(context); }
        @Override protected void renderSafe(CompatMillstoneBlockEntity be, float partialTicks, PoseStack ms, MultiBufferSource buffer, int light, int overlay) {
            super.renderSafe(be, partialTicks, ms, buffer, light, overlay);
            BlockState state = be.getBlockState();
            BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(state);
            float speed = be.getSpeed();
            float time = be.getLevel().getGameTime() + partialTicks;
            float angle = (time * speed * 1f / 10f) % 360;
            ms.pushPose();
            ms.translate(0.5f, 0.5f, 0.5f);
            ms.mulPose(Axis.YP.rotationDegrees(angle));
            ms.translate(-0.5f, -0.5f, -0.5f);
            Minecraft.getInstance().getBlockRenderer().getModelRenderer().renderModel(ms.last(), buffer.getBuffer(RenderType.cutout()), state, model, 1.0F, 1.0F, 1.0F, light, overlay, ModelData.EMPTY, null);
            ms.popPose();
        }
    }
}
