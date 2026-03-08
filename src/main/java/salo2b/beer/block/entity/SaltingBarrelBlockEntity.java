package salo2b.beer.block.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import salo2b.beer.registration.ModBlockEntities;
import salo2b.beer.registration.ModItems;

public class SaltingBarrelBlockEntity extends BlockEntity {
    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
            if (level != null) {
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    };

    private int saltCount = 0;
    private final int[] progress = new int[6];
    private static final int SALT_TIME = 1200; // 60 секунд (1 минута)
    private static final int MAX_SALT = 12; // По 2 соли на 6 рыб

    public SaltingBarrelBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SALTING_BARREL_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SaltingBarrelBlockEntity be) {
        boolean finishedAny = false;
        int activeFish = 0;
        
        // Сколько соли "доступно" для текущего тика (чтобы распределить по слотам)
        int availableSalt = be.saltCount;

        for (int i = 0; i < be.inventory.getSlots(); i++) {
            ItemStack stack = be.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(ItemTags.FISHES) && !stack.is(ModItems.SALTED_FISH.get())) {
                // Если есть хотя бы 2 соли для этой рыбы
                if (availableSalt >= 2) {
                    be.progress[i]++;
                    availableSalt -= 2; // Резервируем соль для этой рыбы на время тика
                    activeFish++;

                    if (be.progress[i] >= SALT_TIME) {
                        if (!level.isClientSide) {
                            be.inventory.setStackInSlot(i, new ItemStack(ModItems.SALTED_FISH.get()));
                            be.saltCount -= 2; // Фактически забираем соль, так как рыба приготовилась
                            be.progress[i] = 0;
                            level.playSound(null, pos, SoundEvents.SAND_BREAK, SoundSource.BLOCKS, 1.0f, 1.0f);
                            finishedAny = true;
                        }
                    }
                }
            }
        }

        if (activeFish > 0 && level.isClientSide && level.random.nextInt(10) == 0) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(ModItems.SALT.get())),
                    pos.getX() + 0.2 + level.random.nextDouble() * 0.6,
                    pos.getY() + 0.2, // Понижено для полубочки
                    pos.getZ() + 0.2 + level.random.nextDouble() * 0.6,
                    0, 0.05, 0);
        }
        
        if (finishedAny && !level.isClientSide) {
            be.setChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        } else if (activeFish > 0 && !level.isClientSide) {
            be.setChanged();
        }
    }

    public ItemInteractionResult interact(Player player, InteractionHand hand, ItemStack stack) {
        Level level = this.level;
        if (level == null) return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;

        // Отмена (Shift + ПКМ пустой рукой) - достаем всё обратно
        if (stack.isEmpty() && player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                extractAll(player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Клик пустой рукой (без шифта)
        if (stack.isEmpty()) {
            if (!level.isClientSide) {
                // Сначала пробуем достать только ГОТОВУЮ рыбу
                boolean extractedSalted = false;
                for (int i = 0; i < inventory.getSlots(); i++) {
                    ItemStack slotStack = inventory.getStackInSlot(i);
                    if (!slotStack.isEmpty() && slotStack.is(ModItems.SALTED_FISH.get())) {
                        if (!player.getInventory().add(slotStack.copy())) {
                            player.drop(slotStack.copy(), false);
                        }
                        inventory.setStackInSlot(i, ItemStack.EMPTY);
                        progress[i] = 0;
                        extractedSalted = true;
                    }
                }
                
                if (extractedSalted) {
                    level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                    setChanged();
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    return ItemInteractionResult.SUCCESS;
                }

                // Если готовой рыбы нет, показываем статус в Actionbar
                int rawFish = getRawFishCount();
                int saltedFish = getSaltedFishCount();
                int neededSalt = rawFish * 2;
                
                if (rawFish == 0 && saltedFish == 0 && saltCount == 0) {
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal("§8Бочка пуста"), true);
                } else {
                    String status = "";
                    if (rawFish > 0) {
                        int maxProgress = 0;
                        int simulatedAvailableSalt = saltCount;
                        for (int i = 0; i < inventory.getSlots(); i++) {
                            ItemStack slotStack = inventory.getStackInSlot(i);
                            if (!slotStack.isEmpty() && slotStack.is(ItemTags.FISHES) && !slotStack.is(ModItems.SALTED_FISH.get())) {
                                if (simulatedAvailableSalt >= 2) {
                                    if (progress[i] > maxProgress) maxProgress = progress[i];
                                    simulatedAvailableSalt -= 2;
                                }
                            }
                        }
                        
                        if (saltCount >= 2) {
                            status = "§aСолится... " + (maxProgress * 100 / SALT_TIME) + "%";
                        } else {
                            status = "§cПауза (мало соли)";
                        }
                    } else if (saltedFish > 0) {
                        status = "§eГотово!";
                    } else {
                        status = "§eОжидание рыбы";
                    }
                    
                    player.displayClientMessage(net.minecraft.network.chat.Component.literal(
                        "§bСырой: " + rawFish + " §f| §eСоленой: " + saltedFish + " §f| §fСоли: " + saltCount + "/" + (rawFish > 0 ? neededSalt : MAX_SALT) + " §8- " + status
                    ), true);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }

        // Логика добавления
        if (!stack.isEmpty()) {
            // Добавление сырой рыбы
            if (stack.is(ItemTags.FISHES) && !stack.is(ModItems.SALTED_FISH.get())) {
                for (int i = 0; i < inventory.getSlots(); i++) {
                    if (inventory.getStackInSlot(i).isEmpty()) {
                        if (!level.isClientSide) {
                            inventory.setStackInSlot(i, stack.split(1));
                            progress[i] = 0; // Сбрасываем таймер только для ЭТОГО слота
                            level.playSound(null, worldPosition, SoundEvents.SLIME_BLOCK_PLACE, SoundSource.BLOCKS, 1.0f, 1.0f);
                            this.setChanged();
                            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                        }
                        return ItemInteractionResult.sidedSuccess(level.isClientSide);
                    }
                }
                if (!level.isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cБочка полна рыбы!"), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            
            // Добавление соли
            if (stack.is(ModItems.SALT.get())) {
                if (saltCount < MAX_SALT) {
                    if (!level.isClientSide) {
                        stack.shrink(1);
                        saltCount++;
                        level.playSound(null, worldPosition, SoundEvents.SAND_PLACE, SoundSource.BLOCKS, 1.0f, 1.2f);
                        this.setChanged();
                        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    }
                    return ItemInteractionResult.sidedSuccess(level.isClientSide);
                }
                if (!level.isClientSide) player.displayClientMessage(net.minecraft.network.chat.Component.literal("§cСлишком много соли!"), true);
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    private void extractAll(Player player) {
        boolean extracted = false;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack slotStack = inventory.getStackInSlot(i);
            if (!slotStack.isEmpty()) {
                if (!player.getInventory().add(slotStack.copy())) {
                    player.drop(slotStack.copy(), false);
                }
                inventory.setStackInSlot(i, ItemStack.EMPTY);
                progress[i] = 0;
                extracted = true;
            }
        }
        
        while (saltCount > 0) {
            ItemStack saltStack = new ItemStack(ModItems.SALT.get(), Math.min(saltCount, 64));
            if (!player.getInventory().add(saltStack.copy())) {
                player.drop(saltStack.copy(), false);
            }
            saltCount -= saltStack.getCount();
            extracted = true;
        }

        if (extracted) {
            if (level != null) {
                level.playSound(null, worldPosition, SoundEvents.ITEM_PICKUP, SoundSource.BLOCKS, 0.5f, 1.0f);
                setChanged();
                level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
            }
        }
    }

    public int getRawFishCount() {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(ItemTags.FISHES) && !stack.is(ModItems.SALTED_FISH.get())) count++;
        }
        return count;
    }

    public int getSaltedFishCount() {
        int count = 0;
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack stack = inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.is(ModItems.SALTED_FISH.get())) count++;
        }
        return count;
    }
    
    public int getSaltCount() { return saltCount; }
    public ItemStackHandler getInventory() { return inventory; }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("inventory", inventory.serializeNBT(registries));
        tag.putInt("saltCount", saltCount);
        tag.putIntArray("progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("inventory"));
        }
        this.saltCount = tag.getInt("saltCount");
        int[] savedProgress = tag.getIntArray("progress");
        if (savedProgress.length == 6) {
            System.arraycopy(savedProgress, 0, progress, 0, 6);
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
