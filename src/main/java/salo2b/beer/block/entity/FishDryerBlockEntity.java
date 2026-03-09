package salo2b.beer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.item.FishHelper;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModItems;
import java.util.List;

public class FishDryerBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(4) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private final int[] progress = new int[4];
    private static final int DRY_TIME = 2400;
    
    // Анимация X (влево-вправо вдоль веревки/перекладины)
    public final float[] swingAnglesX = new float[4];
    public final float[] swingSpeedsX = new float[4];
    // Анимация Z (вперед-назад)
    public final float[] swingAnglesZ = new float[4];
    public final float[] swingSpeedsZ = new float[4];

    public FishDryerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FISH_DRYER_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FishDryerBlockEntity be) {
        if (level.isClientSide) {
            Direction facing = state.getValue(salo2b.beer.block.FishDryerBlock.FACING);
            double[] ropeXOffsets = {0.09375, 0.71875, 1.34375, 1.90625};

            for (int i = 0; i < 4; i++) {
                // Физика веревки всегда активна, даже если рыбы нет
                // Гравитация и пружина X (более плавная)
                be.swingSpeedsX[i] -= be.swingAnglesX[i] * 0.05f; 
                be.swingSpeedsX[i] *= 0.92f; // Трение
                be.swingAnglesX[i] += be.swingSpeedsX[i];
                
                // Гравитация и пружина Z
                be.swingSpeedsZ[i] -= be.swingAnglesZ[i] * 0.05f; 
                be.swingSpeedsZ[i] *= 0.92f; // Трение
                be.swingAnglesZ[i] += be.swingSpeedsZ[i];
                
                // Легкий ветер (влияет на обе оси по-разному)
                float windX = (float) Math.sin(level.getGameTime() * 0.03f + i * 1.5) * 0.005f;
                float windZ = (float) Math.cos(level.getGameTime() * 0.025f + i * 2) * 0.008f;
                be.swingAnglesX[i] += windX;
                be.swingAnglesZ[i] += windZ;

                // Перевод из локальных координат структуры в глобальные с учетом поворота
                double rad = Math.toRadians(-facing.toYRot() + 180);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);

                // Координаты точки подвеса в глобальном пространстве
                double anchorLocalX = ropeXOffsets[i];
                double anchorLocalZ = 0;
                double anchorGlobalX = pos.getX() + 0.5 + (anchorLocalX * cos + anchorLocalZ * sin);
                double anchorGlobalZ = pos.getZ() + 0.5 + (-anchorLocalX * sin + anchorLocalZ * cos);
                
                // Текущее отклонение веревки в локальном пространстве (на высоте рыбы)
                double ropeLength = 1.16;
                double swingLocalX = Math.sin(be.swingAnglesX[i]) * ropeLength;
                double swingLocalZ = Math.sin(be.swingAnglesZ[i]) * ropeLength;
                
                // Текущее отклонение в глобальном пространстве
                double swingGlobalX = swingLocalX * cos + swingLocalZ * sin;
                double swingGlobalZ = -swingLocalX * sin + swingLocalZ * cos;

                // Текущая реальная позиция висящей рыбы
                double fishX = anchorGlobalX + swingGlobalX;
                double fishZ = anchorGlobalZ + swingGlobalZ;
                double fishY = pos.getY() + 2.94 - ropeLength;

                // Хитбокс для поиска игроков
                AABB fishBox = new AABB(fishX - 0.5, fishY - 0.5, fishZ - 0.5, fishX + 0.5, fishY + 0.8, fishZ + 0.5);
                List<Player> players = level.getEntitiesOfClass(Player.class, fishBox);
                
                if (!players.isEmpty()) {
                    for (Player p : players) {
                        double dx = fishX - p.getX();
                        double dz = fishZ - p.getZ();
                        double dist = Math.sqrt(dx * dx + dz * dz);
                        
                        double targetDist = 0.35; // Уменьшенный радиус (игрок 0.3 + веревка 0.05)
                        if (dist < targetDist && dist > 0.01) {
                            double overlap = targetDist - dist;
                            
                            double dirX = dx / dist;
                            double dirZ = dz / dist;
                            
                            // Мягкая сила выталкивания, чтобы веревка плавно обтекала
                            double force = overlap * 0.12; 
                            
                            double globalForceX = dirX * force;
                            double globalForceZ = dirZ * force;
                            
                            // Легкий эффект увлечения (drag) за игроком
                            double dragX = p.getDeltaMovement().x * 0.05;
                            double dragZ = p.getDeltaMovement().z * 0.05;
                            
                            globalForceX += dragX;
                            globalForceZ += dragZ;

                            // Переводим глобальную силу обратно в локальную
                            double localForceX = globalForceX * cos - globalForceZ * sin;
                            double localForceZ = globalForceX * sin + globalForceZ * cos;

                            be.swingSpeedsX[i] += (float) localForceX;
                            be.swingSpeedsZ[i] += (float) localForceZ;
                            
                            // Гасим скорость, чтобы веревка не скакала, а "липла" к хитбоксу игрока
                            be.swingSpeedsX[i] *= 0.6f;
                            be.swingSpeedsZ[i] *= 0.6f;
                        }
                    }
                }
            }
            return;
        }

        boolean changed = false;
        for (int i = 0; i < 4; i++) {
            ItemStack stack = be.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && FishHelper.isSaltedFish(stack)) {
                be.progress[i]++;
                if (be.progress[i] >= DRY_TIME) {
                    net.minecraft.world.item.Item driedVariant = FishHelper.getDriedVariant(stack.getItem());
                    if (driedVariant != null) {
                        be.inventory.setStackInSlot(i, new ItemStack(driedVariant));
                        be.progress[i] = 0;
                        changed = true;
                    }
                }
            } else {
                be.progress[i] = 0;
            }
        }
        if (changed) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }

    public ItemInteractionResult useItemOnSlot(Player player, InteractionHand hand, ItemStack stack, int slot) {
        ItemStack slotStack = inventory.getStackInSlot(slot);
        if (slotStack.isEmpty()) {
            if (FishHelper.isSaltedFish(stack)) {
                inventory.setStackInSlot(slot, stack.split(1));
                if (level != null && level.isClientSide) {
                    swingSpeedsZ[slot] = 0.05f; // Очень слабый толчок при подвешивании
                    swingSpeedsX[slot] = (level.random.nextFloat() - 0.5f) * 0.02f;
                }
                return ItemInteractionResult.SUCCESS;
            }
        } else {
            if (player.isShiftKeyDown() || stack.isEmpty()) {
                ItemStack taken = inventory.getStackInSlot(slot).copy();
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
                if (!player.getInventory().add(taken)) {
                    player.drop(taken, false);
                }
                return ItemInteractionResult.SUCCESS;
            }
        }
        return ItemInteractionResult.CONSUME;
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putIntArray("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        int[] savedProgress = tag.getIntArray("progress");
        if (savedProgress.length == 4) {
            System.arraycopy(savedProgress, 0, progress, 0, 4);
        }
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        saveAdditional(tag, registries);
        return tag;
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
